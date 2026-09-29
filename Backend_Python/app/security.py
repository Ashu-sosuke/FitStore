import os
import logging
from typing import Optional
import jwt
from fastapi import HTTPException, Request, Security
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

API_KEY = os.getenv("API_KEY", "FitStore_Secret_Key_2026_Secure")
JWT_SECRET = os.getenv("JWT_SECRET") or os.getenv("API_KEY") or "FitStore_JWT_Signing_Key_2026_Change_Me"
KNOWN_API_KEYS = {k for k in [API_KEY, "FitStore_Secret_Key_2026_Secure"] if k}

security_bearer = HTTPBearer(auto_error=False)

async def verify_jwt(request: Request, credentials: Optional[HTTPAuthorizationCredentials] = Security(security_bearer)):
    """
    Verifies either an X-API-KEY header or Bearer JWT token.
    Extracts and validates user device/subject ID.
    """
    # 1. Check for X-API-KEY header first
    api_key_header = request.headers.get("X-API-KEY") or request.headers.get("x-api-key")
    if api_key_header and (api_key_header in KNOWN_API_KEYS or api_key_header == API_KEY):
        user_id_hdr = request.headers.get("X-User-Id") or request.headers.get("x-user-id")
        return user_id_hdr or "api_key_authorized"

    # 2. Check for Authorization Bearer token
    if credentials and credentials.credentials:
        token = credentials.credentials
        if token in KNOWN_API_KEYS or token == API_KEY:
            user_id_hdr = request.headers.get("X-User-Id") or request.headers.get("x-user-id")
            return user_id_hdr or "api_key_authorized"

        try:
            payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"])
            device_id = payload.get("sub")
            path_device_id = (
                request.path_params.get("device_id") 
                or request.path_params.get("deviceId") 
                or request.path_params.get("userId")
            )
            if path_device_id and device_id and path_device_id != device_id:
                raise HTTPException(status_code=403, detail="Access denied: token does not match requested resource")
            return device_id or "authorized_user"
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token expired")
        except jwt.PyJWTError:
            if token in KNOWN_API_KEYS or token == API_KEY:
                user_id_hdr = request.headers.get("X-User-Id") or request.headers.get("x-user-id")
                return user_id_hdr or "api_key_authorized"
            raise HTTPException(status_code=401, detail="Invalid credentials")

    raise HTTPException(status_code=401, detail="Authentication credentials were not provided (Bearer token or X-API-KEY required)")

async def verify_jwt_optional(request: Request, credentials: Optional[HTTPAuthorizationCredentials] = Security(security_bearer)) -> str:
    """
    Optional auth: verifies token or API key if provided, otherwise gracefully falls back
    to X-User-Id header or 'anonymous' (ideal for food scanning).
    """
    try:
        return await verify_jwt(request, credentials)
    except HTTPException:
        user_id_hdr = request.headers.get("X-User-Id") or request.headers.get("x-user-id")
        return user_id_hdr or "anonymous"
