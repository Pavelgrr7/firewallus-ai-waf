import logging
from pathlib import Path

import joblib
from sklearn.feature_extraction.text import TfidfVectorizer as SklearnTfidfVectorizer

logger = logging.getLogger(__name__)

DEFAULT_VECTORIZER_PATH = "models/vectorizer.joblib"


class TfidfVectorizer:
    def __init__(self):
        self._vectorizer: SklearnTfidfVectorizer | None = None

    @property
    def is_fitted(self) -> bool:
        return self._vectorizer is not None

    def fit(self, texts: list[str]):
        self._vectorizer = SklearnTfidfVectorizer(
            analyzer="char_wb",
            ngram_range=(3, 5),
            max_features=200,
            sublinear_tf=True,
        )
        self._vectorizer.fit(texts)
        logger.info("TF-IDF vectorizer fitted on %d samples", len(texts))

    def transform(self, text: str):
        if not self._vectorizer:
            return None
        return self._vectorizer.transform([text]).toarray()[0]

    def save(self, path: str = DEFAULT_VECTORIZER_PATH):
        if self._vectorizer:
            Path(path).parent.mkdir(parents=True, exist_ok=True)
            joblib.dump(self._vectorizer, path)
            logger.info("Vectorizer saved to %s", path)

    def load(self, path: str = DEFAULT_VECTORIZER_PATH):
        if Path(path).exists():
            self._vectorizer = joblib.load(path)
            logger.info("Vectorizer loaded from %s", path)
        else:
            logger.warning("Vectorizer file not found at %s", path)


tfidf_vectorizer = TfidfVectorizer()
