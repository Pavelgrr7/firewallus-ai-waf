import logging
from datetime import datetime, timezone
from uuid import uuid4

from app.config import settings
from app.ml.detector import detector
from app.schemas.incident import IncidentEvent
from app.schemas.traffic import TrafficEvent
from app.services import kafka_producer, redis_client

logger = logging.getLogger(__name__)


async def handle_detection(event: TrafficEvent, features):
    result = detector.score(features)
    if result is None:
        return

    prediction, confidence = result

    # IsolationForest convention: -1 marks an anomaly.
    if prediction != -1:
        return

    logger.warning(
        "Anomaly detected from IP %s on URI %s (confidence=%.3f)",
        event.ip, event.uri, confidence,
    )

    await redis_client.ban_ip(event.ip)

    incident = IncidentEvent(
        incident_id=uuid4(),
        incident_type="ML_ANOMALY",
        timestamp=datetime.now(timezone.utc),
        attacker_ip=event.ip,
        confidence_score=round(confidence, 4),
        payload_dump={
            "uri": event.uri,
            "method": event.method.value,
            "ban_ttl": settings.redis_ban_ttl,
        },
    )
    await kafka_producer.send_incident(incident)
