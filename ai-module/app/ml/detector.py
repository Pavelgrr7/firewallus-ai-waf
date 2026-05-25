import logging
from pathlib import Path

import joblib

logger = logging.getLogger(__name__)

DEFAULT_MODEL_PATH = "models/detector.joblib"


class AnomalyDetector:
    def __init__(self):
        self._model = None

    @property
    def is_loaded(self) -> bool:
        return self._model is not None

    def load(self, path: str = DEFAULT_MODEL_PATH):
        if Path(path).exists():
            self._model = joblib.load(path)
            logger.info("Model loaded from %s", path)
        else:
            logger.warning("Model file not found at %s, running without model", path)

    def reload(self, path: str = DEFAULT_MODEL_PATH):
        self.load(path)

    def predict(self, features):
        if not self._model:
            return None
        return self._model.predict(features.reshape(1, -1))[0]


detector = AnomalyDetector()
