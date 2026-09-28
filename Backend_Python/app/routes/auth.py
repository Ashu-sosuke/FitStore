from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel
import jwt
import os
import datetime
import logging

logger = logging.getLogger(__name__)

router = APIRouter()

JWT_SECRET = os.getenv("JWT_SECRET") or os.getenv("API_KEY") or "FitStore_JWT_Signing_Key_2026_Change_Me"

class AuthRequest(BaseModel):
    deviceId: str

class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"

@router.post("/token", response_model=AuthResponse)
async def login_for_access_token(req: AuthRequest):
    if not req.deviceId:
        raise HTTPException(status_code=400, detail="deviceId is required")
        
    payload = {
        "sub": req.deviceId,
        "exp": datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=30)
    }
    token = jwt.encode(payload, JWT_SECRET, algorithm="HS256")
    return AuthResponse(access_token=token)
