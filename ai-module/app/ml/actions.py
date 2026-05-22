import logging

from app.features.pipeline import feature_pipeline
from app.ml.detector import detector
from app.schemas.traffic import TrafficEvent
from app.services import redis_client

logger = logging.getLogger(__name__)


async def handle_detection(event: TrafficEvent, features):
    prediction = detector.predict(features)
    if prediction is None:
        return

    # -1 for anomalies
    if prediction == -1:
        logger.warning("Anomaly detected from IP %s on URI %s", event.ip, event.uri)
        await redis_client.ban_ip(event.ip)
