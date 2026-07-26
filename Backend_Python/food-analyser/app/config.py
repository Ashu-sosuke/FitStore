"""
Centralised configuration loaded from environment variables.
"""

import os
from dotenv import load_dotenv

load_dotenv()

# ── MongoDB ──────────────────────────────────────────────────────────────────
MONGO_URI: str = os.getenv(
    "MONGO_URI",
    "mongodb://localhost:27017/fitness-tracker",
)
DATABASE_NAME: str = "fitness-tracker"

# ── Server ───────────────────────────────────────────────────────────────────
PORT: int = int(os.getenv("PORT", "8000"))

# ── ML Model ─────────────────────────────────────────────────────────────────
MODEL_NAME: str = os.getenv("MODEL_NAME", "mobilenet_v2")
CONFIDENCE_THRESHOLD: float = float(os.getenv("CONFIDENCE_THRESHOLD", "0.60"))

# Supported food labels the model can predict
FOOD_LABELS: list[str] = [
    "Egg",
    "Chicken",
    "Milk",
    "Broccoli",
    "Avocado",
    "Salmon",
]
