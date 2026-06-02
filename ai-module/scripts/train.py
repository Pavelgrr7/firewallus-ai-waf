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

    text = filepath.read_text(encoding="utf-8", errors="ignore")
    requests = text.split("\n\n")

    for raw_request in requests:
        raw_request = raw_request.strip()
        if not raw_request:
            continue

        lines = raw_request.split("\n")
        if not lines:
            continue

        request_line = lines[0].strip()
        match = re.match(r"(GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD)\s+(\S+)\s+HTTP/", request_line)
        if not match:
            continue

        method_str, uri = match.group(1), match.group(2)
        method = METHOD_MAP.get(method_str, HttpMethod.UNKNOWN)

        events.append(TrafficEvent(ip="0.0.0.0", method=method, uri=uri))

    return events


def build_feature_matrix(events: list[TrafficEvent]) -> np.ndarray:
    """Extract features for a list of events."""
    vectors = []
    for event in events:
        static = extract_static_features(event)
        tfidf = tfidf_vectorizer.transform(event.uri)
        combined = np.concatenate([static, tfidf]) if tfidf is not None else static
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

    print("\n[2/5] Fitting TF-IDF vectorizer...")
    train_uris = [e.uri for e in normal_train]
    tfidf_vectorizer.fit(train_uris)
    tfidf_vectorizer.save(str(MODELS_DIR / "vectorizer.joblib"))
    vocab = getattr(tfidf_vectorizer._vectorizer, "vocabulary_", {})
    print(f"  Vocabulary size: {len(vocab)}")

    print("\n[3/5] Extracting features...")
    X_train = build_feature_matrix(normal_train)
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
    if normal_test and anomalous_test:
        X_normal_test = build_feature_matrix(normal_test)
        X_anomalous_test = build_feature_matrix(anomalous_test)

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
