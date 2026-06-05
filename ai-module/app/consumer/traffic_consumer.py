import asyncio
import json
import logging

from aiokafka import AIOKafkaConsumer

from app.config import settings
from app.features.pipeline import feature_pipeline
from app.ml.actions import handle_detection
from app.schemas.traffic import TrafficEvent

logger = logging.getLogger(__name__)


class TrafficConsumer:
    def __init__(self):
        self._consumer: AIOKafkaConsumer | None = None
        self._task: asyncio.Task | None = None

    async def start(self):
        self._consumer = AIOKafkaConsumer(
            settings.kafka_topic_traffic,
            bootstrap_servers=settings.kafka_bootstrap_servers,
            group_id=settings.kafka_consumer_group,
            value_deserializer=lambda v: json.loads(v.decode("utf-8")),
            auto_offset_reset="latest",
        )
        await self._consumer.start()
        self._task = asyncio.create_task(self._consume_loop())
        logger.info("Kafka consumer started on topic '%s'", settings.kafka_topic_traffic)

    async def _consume_loop(self):
        try:
            async for msg in self._consumer:
                try:
                    event = TrafficEvent(**msg.value)
                    features = feature_pipeline.extract(event)
                    logger.info(
                        "Processed event: ip=%s uri=%s features_dim=%d",
                        event.ip, event.uri, len(features),
                    )
                    await handle_detection(event, features)
                except Exception:
                    logger.exception("Failed to process message: %s", msg.value)
        except asyncio.CancelledError:
            pass

    async def stop(self):
        if self._task:
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        if self._consumer:
            await self._consumer.stop()
        logger.info("Kafka consumer stopped")


traffic_consumer = TrafficConsumer()
