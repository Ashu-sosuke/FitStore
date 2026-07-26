"""
FastAPI application factory.

Wires up:
  • CORS (allow Android client from any origin during development)
  • MongoDB lifecycle (connect on startup, close on shutdown)
  • /scan-food router
  • Health-check root endpoint
  • Structured JSON logging
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.database import close_db, connect_db
from app.routes import router as food_router

# ── Logging ──────────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s │ %(levelname)-8s │ %(name)s │ %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)


# ── Lifespan (startup / shutdown) ────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    """Manage resources that live for the entire server lifetime."""
    await connect_db()
    logger.info("[OK] Food Analyser API is starting up")

    # Pre-warm the model so the first request isn't slow
    from app.model import _load_model  # noqa: delayed import
    _load_model()
    logger.info("[OK] ML model loaded and ready")

    yield  # ← server is running

    await close_db()
    logger.info("[OK] Food Analyser API has shut down")


# ── App ──────────────────────────────────────────────────────────────────────
app = FastAPI(
    title="FitStore Food Analyser",
    description=(
        "AI-powered food identification micro-service for the FitStore Android app. "
        "Upload a photo and get back nutritional information instantly."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

# Allow the Android client (and browser testing) to hit the API
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # tighten in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routes
app.include_router(food_router, tags=["Food Scanner"])


# ── Health check ─────────────────────────────────────────────────────────────
@app.get("/", tags=["Health"])
async def health_check():
    """Simple liveness probe."""
    return {
        "success": True,
        "service": "food-analyser",
        "message": "Food Analyser API is running",
    }
