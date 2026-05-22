import logging

import redis.asyncio as redis

from app.config import settings

logger = logging.getLogger(__name__)

_redis: redis.Redis | None = None


async def get_redis() -> redis.Redis:
    global _redis
    if _redis is None:
        _redis = redis.from_url(settings.redis_url, decode_responses=True)
    return _redis


async def ban_ip(ip: str, ttl: int | None = None):
    r = await get_redis()
    key = f"waf:ban:ip:{ip}"
    await r.set(key, "1", ex=ttl or settings.redis_ban_ttl)
    logger.info("Banned IP %s for %ds", ip, ttl or settings.redis_ban_ttl)


async def close():
    global _redis
    if _redis:
        await _redis.close()
        _redis = None
