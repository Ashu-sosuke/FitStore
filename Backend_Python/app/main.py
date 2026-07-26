from fastapi import FastAPI, Depends, HTTPException, Security, status, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.middleware.cors import CORSMiddleware
from app.routes import profile, workout, meal, auth, leaderboard
import jwt
from app.database import ping_db, db
import uvicorn
import os
from dotenv import load_dotenv

load_dotenv()

API_KEY = os.getenv("API_KEY", "FitStore_Secret_Key_2026_Secure")
security = HTTPBearer()

async def verify_jwt(request: Request, credentials: HTTPAuthorizationCredentials = Security(security)):
    token = credentials.credentials
    try:
        payload = jwt.decode(token, API_KEY, algorithms=["HS256"])
        device_id = payload.get("sub")
        # Validate path param if it exists
        path_device_id = request.path_params.get("device_id")
        if path_device_id and path_device_id != device_id:
            raise HTTPException(status_code=403, detail="Not authorized to access this user's data")
        
        # Optionally validate body deviceId if present in JSON
        # Doing that via dependencies is harder, we'll rely on path params for now
        return device_id
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid credentials")

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

# Database connection check
@app.on_event("startup")
async def startup_db_client():
    try:
        await ping_db()
        # Create indexes for leaderboard
        await db["userprofiles"].create_index("friendCode", unique=True, sparse=True)
        await db["friends"].create_index("userId", unique=True)
        await db["leaderboard_stats"].create_index("userId", unique=True)
        print("[OK] Database indexes verified/created.")
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
