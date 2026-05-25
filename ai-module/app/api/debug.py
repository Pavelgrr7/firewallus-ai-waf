from fastapi import APIRouter

from app.features.pipeline import feature_pipeline
from app.schemas.traffic import HttpMethod, TrafficEvent

router = APIRouter(prefix="/debug", tags=["debug"])


@router.get("/features")
async def debug_features(uri: str, method: HttpMethod = HttpMethod.GET):
    event = TrafficEvent(ip="0.0.0.0", method=method, uri=uri)
    features = feature_pipeline.extract(event)
    return {
        "uri": uri,
        "method": method.value,
        "feature_vector_length": len(features),
        "features": features.tolist(),
    }
