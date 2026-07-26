from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel
import jwt
import os
import datetime

router = APIRouter()

API_KEY = os.getenv("API_KEY", "FitStore_Secret_Key_2026_Secure")

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
        "exp": datetime.datetime.utcnow() + datetime.timedelta(days=365)
    }
    token = jwt.encode(payload, API_KEY, algorithm="HS256")
    return AuthResponse(access_token=token)
