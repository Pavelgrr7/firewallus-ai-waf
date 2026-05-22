import logging
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI

from app.api import debug, health, model
from app.config import settings
from app.consumer.traffic_consumer import traffic_consumer
from app.features.vectorizer import tfidf_vectorizer
from app.ml.detector import detector

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    tfidf_vectorizer.load()
    detector.load()
    await traffic_consumer.start()
    logger.info("AI Brain started")
    yield
    await traffic_consumer.stop()
    from app.services import redis_client
    await redis_client.close()
    logger.info("AI Brain stopped")


app = FastAPI(title="Firewallus AI Brain", lifespan=lifespan)
app.include_router(health.router)
app.include_router(debug.router)
app.include_router(model.router)

if __name__ == "__main__":
    uvicorn.run("app.main:app", host=settings.host, port=settings.port, reload=True)
