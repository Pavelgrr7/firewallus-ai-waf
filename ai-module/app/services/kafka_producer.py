import json
import logging

from aiokafka import AIOKafkaProducer

from app.config import settings
from app.schemas.incident import IncidentEvent

logger = logging.getLogger(__name__)

_producer: AIOKafkaProducer | None = None


async def start():
    global _producer
    _producer = AIOKafkaProducer(
        bootstrap_servers=settings.kafka_bootstrap_servers,
        value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    )
    await _producer.start()
    logger.info("Kafka producer started")


async def send_incident(incident: IncidentEvent):
    if not _producer:
        logger.warning("Producer not started, skipping incident")
        return
    await _producer.send_and_wait(
        settings.kafka_topic_incidents,
        key=incident.attacker_ip.encode("utf-8"),
        value=incident.model_dump(mode="json"),
    )
    logger.info("Incident sent: %s", incident.incident_id)


async def stop():
    global _producer
    if _producer:
        await _producer.stop()
        _producer = None
