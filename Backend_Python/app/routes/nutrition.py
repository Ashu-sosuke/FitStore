import logging
from fastapi import APIRouter, Depends, HTTPException, Query, Security
from app.services.nutrition_engine import nutrition_engine, NutritionResult
from app.security import verify_jwt
from app.database import db
from datetime import datetime, timezone

logger = logging.getLogger(__name__)

router = APIRouter()

@router.get("/", response_model=NutritionResult)
async def get_food_nutrition(
    food: str = Query(..., min_length=1, description="Food name, alias, or Indian dish query"),
    grams: float = Query(default=100.0, ge=1.0, le=5000.0, description="Portion weight in grams"),
    user_id: str = Depends(verify_jwt)
):
    """
    Get scientifically accurate macronutrient breakdown from IFCT 2017 (ICMR-NIN) 
    and composite Indian dish recipes.
    """
    result = nutrition_engine.get_nutrition(food_query=food, grams=grams)

    # Log query and unmatched requests for continuous dataset tuning
    try:
        now_iso = datetime.now(timezone.utc).isoformat()
        if not result.success or result.confidence in ["LOW", "NONE"]:
            logger.info(f"[NUTRITION_UNMATCHED] User '{user_id}' searched: '{food}' (Source: {result.source})")
            await db["unmatched_foods"].insert_one({
                "userId": user_id,
                "query": food,
                "grams": grams,
                "matchedName": result.matched_name,
                "source": result.source,
                "timestamp": now_iso
            })
    except Exception as e:
        logger.warning(f"Failed to log nutrition telemetry: {e}")

    return result
