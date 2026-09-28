from fastapi import FastAPI, Depends, HTTPException, Security, status, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from contextlib import asynccontextmanager
from app.routes import profile, workout, meal, auth, leaderboard, food_scanner, nutrition
import logging
from app.database import ping_db, db
from app.services.dataset_loader import seed_exercise_catalog, DATASET_DIR
from app.security import verify_jwt, API_KEY, JWT_SECRET
import uvicorn
import os
from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan: startup and shutdown logic."""
    if not API_KEY:
        logger.warning("API_KEY is not set! Authentication will not work.")
    if not JWT_SECRET:
        logger.warning("JWT_SECRET is not set! Token signing/verification will fail.")
    try:
        await ping_db()
        await db["userprofiles"].create_index("friendCode", unique=True, sparse=True)
        await db["friends"].create_index("userId", unique=True)
        await db["leaderboard_stats"].create_index("userId", unique=True)
        await db["scan_feedback"].create_index("userId")
        await seed_exercise_catalog(db["exercises_catalog"])
        logger.info("Database indexes, feedback collections, and exercises catalog verified/created.")
    except Exception as e:
        logger.warning(f"Database initialization failed: {e}")
        logger.info("Application will continue, but database operations may fail.")
    yield

app = FastAPI(title="FitStore API", description="Python-based Backend for Fitness Tracking App & Food Vision Pipeline", lifespan=lifespan)

# Request Logger Middleware
@app.middleware("http")
async def log_requests(request, call_next):
    logger.info(f"Incoming Request: {request.method} {request.url}")
    try:
        response = await call_next(request)
        logger.info(f"Response Status: {response.status_code}")
        if response.status_code == 422:
            logger.warning(f"Validation Error occurred for {request.method} {request.url}")
        return response
    except Exception as e:
        logger.error(f"Request failed: {str(e)}")
        raise e

# CORS setup — credentials disabled with wildcard origins for security
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
@app.head("/")
async def root():
    return {"status": "healthy", "service": "Pulse API", "version": "2.0"}

@app.get("/health")
async def health_check():
    return {"status": "ok"}

# Static Files for Exercise Demonstration GIFs
gifs_path = os.path.join(DATASET_DIR, "gifs_360x360")
if not os.path.exists(gifs_path):
    gifs_path = os.path.join(DATASET_DIR, "gifs_180x180")

if os.path.exists(gifs_path):
    app.mount("/static/exercise-gifs", StaticFiles(directory=gifs_path), name="exercise_gifs")
    logger.info(f"Mounted static exercise GIFs from {gifs_path}")


# Include Routers with Security
app.include_router(auth.router, prefix="/api/auth", tags=["Auth"])
app.include_router(profile.router, prefix="/api/profile", tags=["Profile"], dependencies=[Depends(verify_jwt)])
app.include_router(workout.router, prefix="/api/workouts", tags=["Workouts"], dependencies=[Depends(verify_jwt)])
app.include_router(meal.router, prefix="/api/meals", tags=["Meals"], dependencies=[Depends(verify_jwt)])
app.include_router(leaderboard.router, prefix="/api/leaderboard", tags=["Leaderboard"], dependencies=[Depends(verify_jwt)])
app.include_router(nutrition.router, prefix="/api/nutrition", tags=["Nutrition Engine"], dependencies=[Depends(verify_jwt)])
app.include_router(food_scanner.router, tags=["Food Vision Scanner"], dependencies=[Depends(verify_jwt)])

if __name__ == "__main__":
    port = int(os.getenv("PORT", 10000))
    uvicorn.run("app.main:app", host="0.0.0.0", port=port, reload=True)
