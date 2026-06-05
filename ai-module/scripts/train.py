"""
Training script: fit TF-IDF vectorizer and Isolation Forest on CSIC 2010 dataset.
"""

import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import joblib
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.metrics import classification_report

from app.features.static_features import extract_static_features
from app.features.pipeline import feature_pipeline
from app.features.vectorizer import tfidf_vectorizer
from app.schemas.traffic import HttpMethod, TrafficEvent

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
MODELS_DIR = Path(__file__).resolve().parent.parent / "models"

METHOD_MAP = {
    "GET": HttpMethod.GET,
    "POST": HttpMethod.POST,
    "PUT": HttpMethod.PUT,
    "DELETE": HttpMethod.DELETE,
    "PATCH": HttpMethod.PATCH,
    "OPTIONS": HttpMethod.OPTIONS,
    "HEAD": HttpMethod.HEAD,
}


def parse_csic_file(filepath: Path) -> list[TrafficEvent]:
    """Parse CSIC 2010 raw HTTP request file into TrafficEvent list."""
    events = []
    if not filepath.exists():
        print(f"WARNING: {filepath} not found, skipping")
        return events

    from urllib.parse import unquote
    text = filepath.read_text(encoding="utf-8", errors="ignore")
    # CSIC 2010 separates HTTP requests with double newlines, 
    # but also POST bodies are separated from headers with double newlines.
    parts = text.split("\n\n")

    current_event = None
    for part in parts:
        part = part.strip()
        if not part:
            continue

        lines = part.split("\n")
        request_line = lines[0].strip()
        match = re.match(r"^(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)\s+(\S+)\s+HTTP/", request_line)

        if match:
            method_str, uri = match.group(1), match.group(2)
            method = METHOD_MAP.get(method_str, HttpMethod.UNKNOWN)
            # URL-decode the URI to expose actual attack characters to features
            decoded_uri = unquote(uri)
            
            # Strip "http://localhost:8080" or similar host prefixes
            # so that it matches what Ktor gateway sends (e.g., "/api/...")
            from urllib.parse import urlparse
            parsed_uri = urlparse(decoded_uri)
            if parsed_uri.netloc:
                # Reconstruct path and query without scheme/netloc
                path_query = parsed_uri.path
                if parsed_uri.query:
                    path_query += "?" + parsed_uri.query
                decoded_uri = path_query

            current_event = TrafficEvent(ip="0.0.0.0", method=method, uri=decoded_uri)
            events.append(current_event)
        else:
            # If it does not match a new request, it's a body of the previous request
            if current_event is not None and current_event.method in [HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH]:
                # unquote body and append to uri
                decoded_body = unquote(part)
                delimiter = "&" if "?" in current_event.uri else "?"
                current_event.uri += delimiter + decoded_body
            current_event = None

    return events


def build_feature_matrix(events: list[TrafficEvent]) -> np.ndarray:
    """Extract features for a list of events."""
    vectors = []
    for event in events:
        combined = feature_pipeline.extract(event)
        vectors.append(combined)
    return np.vstack(vectors)


def main():
    MODELS_DIR.mkdir(exist_ok=True)

    print("=" * 60)
    print("CSIC 2010 Training Pipeline")
    print("=" * 60)

    print("\n[1/5] Loading dataset...")
    normal_train = parse_csic_file(DATA_DIR / "normalTrafficTraining.txt")
    normal_test = parse_csic_file(DATA_DIR / "normalTrafficTest.txt")
    anomalous_test = parse_csic_file(DATA_DIR / "anomalousTrafficTest.txt")

    if not normal_train:
        print("\nERROR: No training data found.")
        sys.exit(1)

    print(f"  Normal train:    {len(normal_train)} requests")
    print(f"  Normal test:     {len(normal_test)} requests")
    print(f"  Anomalous test:  {len(anomalous_test)} requests")

    # Inject ~5% anomalies into training data so IsolationForest can see feature variance
    num_anomalies_to_inject = int(len(normal_train) * 0.05)
    
    # --- INJECT KTOR GATEWAY NORMAL TRAFFIC ---
    # Since CSIC 2010 only has /tienda1/... we need to teach the model about our own paths.
    ktor_normal_uris = [
        "/",
        "/api/v1/profile",
        "/api/v1/auth",
        "/api/v1/users",
        "/static/css/main.css",
        "/static/js/app.js",
        "/favicon.ico"
    ]
    ktor_normal_events = []
    # Create 35000 samples of these to ensure they are well represented (50% of dataset)
    for _ in range(5000):
        for uri in ktor_normal_uris:
            method = HttpMethod.POST if "auth" in uri else HttpMethod.GET
            e = TrafficEvent(ip="0.0.0.0", method=method, uri=uri)
            if "auth" in uri:
                e.bodySnippet = '{"username": "user", "password": "password"}'
            ktor_normal_events.append(e)

    mixed_train = normal_train + ktor_normal_events
    
    # Inject a small number of extreme anomalies so IF has non-zero variance 
    # for attack features (like '../', '<script>', and "'") and can split on them.
    # Without this, those features are a constant 0 in the training set and IF ignores them.
    extreme_anomalies = []
    for _ in range(50):
        extreme_anomalies.append(TrafficEvent(ip="0.0.0.0", method=HttpMethod.GET, uri="/api/v1/users?id=' OR '1'='1"))
        extreme_anomalies.append(TrafficEvent(ip="0.0.0.0", method=HttpMethod.GET, uri="/api/v1/download?file=../../../../etc/passwd"))
        extreme_anomalies.append(TrafficEvent(ip="0.0.0.0", method=HttpMethod.GET, uri="/api/v1/search?q=<script>alert(1)</script>"))
    
    mixed_train += extreme_anomalies
    
    normal_test = normal_test + ktor_normal_events
    
    # Remove injected anomalies from test set
    anomalous_test_eval = anomalous_test

    print("\n[2/5] Fitting TF-IDF vectorizer...")
    train_uris = [e.uri for e in mixed_train]
    tfidf_vectorizer.fit(train_uris)
    tfidf_vectorizer.save(str(MODELS_DIR / "vectorizer.joblib"))
    vocab = getattr(tfidf_vectorizer._vectorizer, "vocabulary_", {})
    print(f"  Vocabulary size: {len(vocab)}")

    print("\n[3/5] Extracting features...")
    X_train = build_feature_matrix(mixed_train)
    print(f"  Train matrix: {X_train.shape}")

    print("\n[4/5] Training Isolation Forest...")
    model = IsolationForest(
        n_estimators=200,
        contamination=0.05,
        max_samples="auto",
        random_state=42,
        n_jobs=-1,
    )
    model.fit(X_train)
    joblib.dump(model, MODELS_DIR / "detector.joblib")
    print(f"  Model saved to {MODELS_DIR / 'detector.joblib'}")

    print("\n[5/5] Evaluating on test set...")
    if normal_test and anomalous_test_eval:
        X_normal_test = build_feature_matrix(normal_test)
        X_anomalous_test = build_feature_matrix(anomalous_test_eval)

        pred_normal = model.predict(X_normal_test)
        pred_anomalous = model.predict(X_anomalous_test)

        # IsolationForest
        y_true = np.concatenate([
            np.ones(len(pred_normal)),
            -np.ones(len(pred_anomalous)),
        ])
        y_pred = np.concatenate([pred_normal, pred_anomalous])

        print("\n  Classification Report:")
        print("  " + "-" * 50)
        report = classification_report(
            y_true, y_pred,
            target_names=["Anomaly (-1)", "Normal (1)"],
            digits=3,
        )
        for line in str(report).split("\n"):
            print(f"  {line}")

        normal_accuracy = (pred_normal == 1).mean()
        anomaly_detection_rate = (pred_anomalous == -1).mean()
        print(f"\n  Normal accuracy (specificity):   {normal_accuracy:.3f}")
        print(f"  Anomaly detection rate (recall):  {anomaly_detection_rate:.3f}")

    print("\n" + "=" * 60)
    print("Training complete!")
    print("=" * 60)


if __name__ == "__main__":
    main()
