from fastapi import APIRouter

from app.ml.detector import detector

router = APIRouter(prefix="/model", tags=["model"])


@router.post("/reload")
async def reload_model():
    detector.reload()
    return {"status": "reloaded", "loaded": detector.is_loaded}
