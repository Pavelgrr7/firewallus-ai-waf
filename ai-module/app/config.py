from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    kafka_bootstrap_servers: str = "kafka:29092"
    kafka_consumer_group: str = "ai-brain-group"
    kafka_topic_traffic: str = "traffic-logs"
    kafka_topic_incidents: str = "incidents"

    redis_url: str = "redis://redis:6379"
    redis_ban_ttl: int = 600

    model_dir: str = "models"

    host: str = "0.0.0.0"
    port: int = 8001

    model_config = {"env_prefix": "AI_"}


settings = Settings()
