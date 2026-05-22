from datetime import datetime
from uuid import UUID

from pydantic import BaseModel


class IncidentEvent(BaseModel):
    incident_id: UUID
    incident_type: str
    timestamp: datetime
    attacker_ip: str
    confidence_score: float
    payload_dump: dict
