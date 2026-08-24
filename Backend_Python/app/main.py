from fastapi import FastAPI, Depends, HTTPException, Security, status, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from app.routes import profile, workout, meal, auth, leaderboard
import jwt
from app.database import ping_db, db
from app.services.dataset_loader import seed_exercise_catalog, DATASET_DIR
import uvicorn
import os
from dotenv import load_dotenv

load_dotenv()

from typing import Optional

API_KEY = os.getenv("API_KEY", "FitStore_Secret_Key_2026_Secure")
security = HTTPBearer(auto_error=False)

async def verify_jwt(request: Request, credentials: Optional[HTTPAuthorizationCredentials] = Security(security)):
    # 1. Check for X-API-KEY header first
    api_key_header = request.headers.get("X-API-KEY") or request.headers.get("x-api-key")
    if api_key_header and api_key_header == API_KEY:
        return "api_key_authorized"

    # 2. Check for Authorization Bearer token
    if credentials and credentials.credentials:
        token = credentials.credentials
        if token == API_KEY:
            return "api_key_authorized"

        try:
            payload = jwt.decode(token, API_KEY, algorithms=["HS256"])
            device_id = payload.get("sub")
            path_device_id = request.path_params.get("device_id") or request.path_params.get("deviceId") or request.path_params.get("userId")
            if path_device_id and device_id and path_device_id != device_id:
                # Log but permit if authenticated
                pass
            return device_id or "authorized_user"
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token expired")
        except jwt.PyJWTError:
            # Check if token matches raw API key
            if token == API_KEY:
                return "api_key_authorized"
            raise HTTPException(status_code=401, detail="Invalid credentials")

    # If neither Bearer nor X-API-KEY was provided
    raise HTTPException(status_code=401, detail="Authentication credentials were not provided (Bearer token or X-API-KEY required)")

app = FastAPI(title="FitStore API", description="Python-based Backend for Fitness Tracking App")

# Request Logger Middleware
@app.middleware("http")
async def log_requests(request, call_next):
    print(f"Incoming Request: {request.method} {request.url}")
    try:
        response = await call_next(request)
        print(f"Response Status: {response.status_code}")
        if response.status_code == 422:
            print(f"Validation Error occurred for {request.method} {request.url}")
        return response
    except Exception as e:
        print(f"Request failed: {str(e)}")
        raise e

# CORS setup
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Static Files for Exercise Demonstration GIFs
gifs_path = os.path.join(DATASET_DIR, "gifs_360x360")
if not os.path.exists(gifs_path):
    gifs_path = os.path.join(DATASET_DIR, "gifs_180x180")

if os.path.exists(gifs_path):
    app.mount("/static/exercise-gifs", StaticFiles(directory=gifs_path), name="exercise_gifs")
    print(f"[OK] Mounted static exercise GIFs from {gifs_path}")

# Database connection and startup check
@app.on_event("startup")
async def startup_db_client():
    try:
        await ping_db()
        # Create indexes for leaderboard
        await db["userprofiles"].create_index("friendCode", unique=True, sparse=True)
        await db["friends"].create_index("userId", unique=True)
        await db["leaderboard_stats"].create_index("userId", unique=True)
        
        # Seed exercise catalog
        await seed_exercise_catalog(db["exercises_catalog"])
        print("[OK] Database indexes and exercises catalog verified/created.")
    except Exception as e:
        print(f"[WARNING] Database initialization failed: {e}")
        print("[INFO] Application will continue, but database operations may fail.")

# Include Routers with Security
app.include_router(auth.router, prefix="/api/auth", tags=["Auth"])
app.include_router(profile.router, prefix="/api/profile", tags=["Profile"], dependencies=[Depends(verify_jwt)])
app.include_router(workout.router, prefix="/api/workouts", tags=["Workouts"], dependencies=[Depends(verify_jwt)])
app.include_router(meal.router, prefix="/api/meals", tags=["Meals"], dependencies=[Depends(verify_jwt)])
app.include_router(leaderboard.router, prefix="/api/leaderboard", tags=["Leaderboard"], dependencies=[Depends(verify_jwt)])

@app.get("/")
async def root():
    return {"success": True, "message": "FitStore Python API is running"}

if __name__ == "__main__":
    port = int(os.getenv("PORT", 5000))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)
