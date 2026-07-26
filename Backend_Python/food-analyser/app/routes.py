"""
/scan-food route – the single endpoint consumed by the Android client.

Flow:
    1. Receive multipart JPEG image + optional user_id header.
    2. Run predict_food() to classify the image.
    3. Look up nutritional data from the `nutrients` collection (fallback to defaults if DB fails).
    4. Insert a log entry into the `daily_logs` collection (skip if DB fails).
    5. Return a structured JSON response.
"""

from __future__ import annotations

import logging
from datetime import datetime, timezone

from fastapi import APIRouter, File, Header, HTTPException, UploadFile

from app.database import get_database, is_database_online
from app.model import predict_food
from app.schemas import DailyLogEntry, ErrorResponse, Macros, ScanFoodResponse

logger = logging.getLogger(__name__)

router = APIRouter()

# Maximum file size: 10 MB (guard against accidental uploads)
_MAX_FILE_SIZE = 10 * 1024 * 1024

# Fallback nutritional data in case MongoDB is unreachable during testing
_FALLBACK_NUTRIENTS = {
    "Egg": {"calories": 155, "protein_g": 13.0, "carbs_g": 1.1, "fats_g": 11.0},
    "Chicken": {"calories": 165, "protein_g": 31.0, "carbs_g": 0.0, "fats_g": 3.6},
    "Milk": {"calories": 42, "protein_g": 3.4, "carbs_g": 5.0, "fats_g": 1.0},
    "Broccoli": {"calories": 34, "protein_g": 2.8, "carbs_g": 6.6, "fats_g": 0.4},
    "Avocado": {"calories": 160, "protein_g": 2.0, "carbs_g": 8.5, "fats_g": 14.7},
    "Salmon": {"calories": 208, "protein_g": 20.0, "carbs_g": 0.0, "fats_g": 13.0},
}


@router.post(
    "/scan-food",
    response_model=ScanFoodResponse,
    responses={
        400: {"model": ErrorResponse, "description": "Invalid image or unsupported format"},
        404: {"model": ErrorResponse, "description": "Food not found in nutrient database"},
        413: {"model": ErrorResponse, "description": "Image file too large"},
        500: {"model": ErrorResponse, "description": "Internal server error"},
    },
    summary="Scan a food image and return nutritional info",
    description=(
        "Upload a JPEG/PNG photo of a food item. The server classifies it, "
        "fetches macros from MongoDB (or fallbacks), creates a daily_log entry, "
        "and returns the result to the Android client."
    ),
)
async def scan_food(
    file: UploadFile = File(
        ...,
        description="JPEG or PNG image of the food item",
    ),
    x_user_id: str = Header(
        default="anonymous",
        alias="X-User-Id",
        description="UID of the logged-in Android user",
    ),
):
    """Identify food from an image, log it, and return nutrition data."""

    # ── 1. Validate the uploaded file ────────────────────────────────────
    if file.content_type not in ("image/jpeg", "image/png", "image/jpg"):
        # Some clients might not send correct content type, allow if extension looks okay
        if not file.filename.lower().endswith(('.jpg', '.jpeg', '.png')):
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported content type '{file.content_type}'. Send JPEG or PNG.",
            )

    image_bytes = await file.read()

    if len(image_bytes) > _MAX_FILE_SIZE:
        raise HTTPException(status_code=413, detail="Image exceeds 10 MB limit.")

    if len(image_bytes) == 0:
        raise HTTPException(status_code=400, detail="Uploaded file is empty.")

    # ── 2. Run the ML model ──────────────────────────────────────────────
    try:
        prediction = predict_food(image_bytes)
    except Exception as exc:
        logger.exception("Model inference failed")
        raise HTTPException(status_code=500, detail=f"Model error: {exc}") from exc

    food_label: str = prediction["food_label"]
    confidence: float = prediction["confidence"]

    if food_label == "Unknown":
        raise HTTPException(
            status_code=404,
            detail=(
                "Could not confidently identify the food item. "
                f"Best guess confidence was {confidence:.1%} which is below "
                "the acceptance threshold."
            ),
        )

    # ── 3. Look up nutrients in MongoDB ──────────────────────────────────
    nutrient_doc = None
    if is_database_online():
        try:
            db = get_database()
            nutrient_doc = await db.nutrients.find_one(
                {"food_name": {"$regex": f"^{food_label}$", "$options": "i"}},
            )
        except Exception as db_exc:
            logger.warning("Database lookup failed, using fallback: %s", db_exc)
    else:
        logger.info("Database is offline; bypassing MongoDB nutrients lookup.")

    if nutrient_doc:
        macros = Macros(
            protein_g=nutrient_doc["protein_g"],
            carbs_g=nutrient_doc["carbs_g"],
            fats_g=nutrient_doc["fats_g"],
            calories=nutrient_doc["calories"],
        )
    else:
        # Use fallback if DB failed or item not found
        data = _FALLBACK_NUTRIENTS.get(food_label)
        if not data:
            raise HTTPException(
                status_code=404,
                detail=f"Nutritional data for '{food_label}' not found.",
            )
        macros = Macros(
            protein_g=data["protein_g"],
            carbs_g=data["carbs_g"],
            fats_g=data["fats_g"],
            calories=data["calories"],
        )

    # ── 4. Create daily log entry ────────────────────────────────────────
    now = datetime.now(timezone.utc)
    timestamp_iso = now.isoformat()

    log_entry = DailyLogEntry(
        user_id=x_user_id,
        food_name=food_label,
        nutrients=macros,
        timestamp=timestamp_iso,
    )

    if is_database_online():
        try:
            db = get_database()
            await db.daily_logs.insert_one(log_entry.model_dump())
            logger.info(
                "Logged %s for user %s at %s",
                food_label,
                x_user_id,
                timestamp_iso,
            )
        except Exception as db_exc:
            logger.warning("Failed to log entry to database: %s", db_exc)
    else:
        logger.info("Database is offline; skipping daily_logs insert.")

    # ── 5. Return response ───────────────────────────────────────────────
    return ScanFoodResponse(
        success=True,
        food_name=food_label,
        calories=macros.calories,
        macros=macros,
        confidence=confidence,
        logged_at=timestamp_iso,
    )
