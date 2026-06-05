import logging
import math
from pathlib import Path

import joblib

logger = logging.getLogger(__name__)

DEFAULT_MODEL_PATH = "models/detector.joblib"

# Steepness of the sigmoid mapping IsolationForest.decision_function (~[-0.5, 0.5])
# onto a [0, 1] anomaly-confidence score.
# TODO: Провести калибровку на валидационном наборе, чтобы выбрать оптимальное значение.
_ANOMALY_SCALE = 10.0


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

    def score(self, features):
        """Return (prediction, confidence) for a single feature vector.

        Нормализует decision_function к интервалу [0, 1] с помощью сигмоиды, так что более высокое значение означает более аномальный образец.
        """
        if not self._model:
            return None

        row = features.reshape(1, -1)
        prediction = int(self._model.predict(row)[0])
        # decision_function: positive => normal, negative => anomaly.
        # Negate so the sigmoid grows as the sample looks more anomalous.
        raw = float(self._model.decision_function(row)[0])
        confidence = 1.0 / (1.0 + math.exp(_ANOMALY_SCALE * raw))
        return prediction, confidence


detector = AnomalyDetector()
