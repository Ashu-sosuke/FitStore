import io
import os
import logging
from datetime import datetime, timezone
from pathlib import Path
from typing import List, Optional

from fastapi import APIRouter, Depends, File, Form, HTTPException, Query, UploadFile
from pydantic import BaseModel, Field
from app.database import db
from app.services.nutrition_engine import nutrition_engine
from app.services.vision_service import vision_service
from app.security import verify_jwt, verify_jwt_optional

logger = logging.getLogger(__name__)

router = APIRouter()

class MacrosResponse(BaseModel):
    protein_g: float
    carbs_g: float
    fats_g: float
    calories: float

class FoodItemBreakdown(BaseModel):
    name: str
    matched_name: str
    grams: float
    calories: float
    protein_g: float
    carbs_g: float
    fats_g: float
    source: str = "IFCT_2017"

class ScanFoodResponse(BaseModel):
    success: bool
    is_food: bool = True
    food_name: str
    cuisine: str = "Indian"
    estimated_grams: float
    calories: float
    macros: MacrosResponse
    items: List[FoodItemBreakdown] = Field(default_factory=list)
    top_alternatives: List[str] = Field(default_factory=list)
    confidence: float
    source: str = "IFCT_2017"
    vision_provider: str = "local_classifier"
    scan_id: Optional[str] = None
    logged_at: str

class ScanFeedbackRequest(BaseModel):
    scan_id: Optional[str] = None
    predicted_food: str
    corrected_food: str
    rating: Optional[int] = Field(default=None, ge=1, le=5)
    comments: Optional[str] = None


@router.post("/scan-food", response_model=ScanFoodResponse)
async def scan_food(
    file: UploadFile = File(...),
    quantity_override_grams: Optional[float] = Form(None),
    user_id: str = Depends(verify_jwt_optional)
):
    """
    Analyzes uploaded food image, classifies food items (single or multi-dish plate),
    and strictly derives certified nutritional data from the IFCT 2017 Nutrition Engine.
    """
    try:
        image_bytes = await file.read()
        if len(image_bytes) == 0:
            raise HTTPException(status_code=400, detail="Empty image uploaded")
        if len(image_bytes) > 10 * 1024 * 1024:  # 10MB limit
            raise HTTPException(status_code=413, detail="Image too large. Maximum size is 10MB.")

        # 1. Run Vision Analysis (Preprocessing, Hash Caching, VLM / Classifier)
        vision_res = await vision_service.analyze_image(image_bytes)
        now_iso = datetime.now(timezone.utc).isoformat()

        if not vision_res.is_food:
            return ScanFoodResponse(
                success=True,
                is_food=False,
                food_name="No Food Detected",
                cuisine="Unknown",
                estimated_grams=0.0,
                calories=0.0,
                macros=MacrosResponse(protein_g=0.0, carbs_g=0.0, fats_g=0.0, calories=0.0),
                items=[],
                top_alternatives=[],
                confidence=vision_res.confidence,
                source="IFCT_2017",
                vision_provider=vision_res.vision_provider,
                logged_at=now_iso
            )

        # 2. Process Detected Items & Retrieve Certified IFCT 2017 Nutrition
        item_breakdowns: List[FoodItemBreakdown] = []
        total_calories = 0.0
        total_protein = 0.0
        total_carbs = 0.0
        total_fats = 0.0

        # If quantity_override_grams provided by user and single item, apply override
        items_to_process = vision_res.items or [{"name": vision_res.primary_food_name, "estimated_grams": vision_res.estimated_grams}]
        if quantity_override_grams is not None and quantity_override_grams > 0:
            if len(items_to_process) == 1:
                items_to_process[0].estimated_grams = quantity_override_grams

        for raw_item in items_to_process:
            grams = raw_item.estimated_grams if hasattr(raw_item, "estimated_grams") else raw_item.get("estimated_grams", 100.0)
            item_name = raw_item.name if hasattr(raw_item, "name") else raw_item.get("name", vision_res.primary_food_name)
            
            # Authoritative Nutrition Lookup strictly from IFCT 2017
            nut_res = nutrition_engine.get_nutrition(food_query=item_name, grams=float(grams))
            
            breakdown = FoodItemBreakdown(
                name=item_name,
                matched_name=nut_res.matched_name if nut_res.success else item_name,
                grams=round(grams, 1),
                calories=nut_res.calories,
                protein_g=nut_res.macros.protein_g,
                carbs_g=nut_res.macros.carbs_g,
                fats_g=nut_res.macros.fats_g,
                source=nut_res.source
            )
            item_breakdowns.append(breakdown)
            total_calories += nut_res.calories
            total_protein += nut_res.macros.protein_g
            total_carbs += nut_res.macros.carbs_g
            total_fats += nut_res.macros.fats_g

        primary_food = vision_res.primary_food_name
        if len(item_breakdowns) == 1 and item_breakdowns[0].matched_name and item_breakdowns[0].matched_name != "Not Found":
            primary_food = item_breakdowns[0].matched_name

        # 3. Log Entry in daily_logs with verified user_id
        inserted_id_str = None
        try:
            log_doc = {
                "userId": user_id,
                "foodName": primary_food,
                "cuisine": vision_res.cuisine,
                "calories": round(total_calories, 1),
                "protein_g": round(total_protein, 1),
                "carbs_g": round(total_carbs, 1),
                "fats_g": round(total_fats, 1),
                "items": [item.model_dump() for item in item_breakdowns],
                "source": "IFCT_2017",
                "visionProvider": vision_res.vision_provider,
                "confidence": vision_res.confidence,
                "timestamp": now_iso
            }
            insert_result = await db["daily_logs"].insert_one(log_doc)
            inserted_id_str = str(insert_result.inserted_id)
        except Exception as e:
            logger.warning(f"Failed to log scan entry: {e}")

        return ScanFoodResponse(
            success=True,
            is_food=True,
            food_name=primary_food,
            cuisine=vision_res.cuisine,
            estimated_grams=round(sum(b.grams for b in item_breakdowns), 1),
            calories=round(total_calories, 1),
            macros=MacrosResponse(
                protein_g=round(total_protein, 1),
                carbs_g=round(total_carbs, 1),
                fats_g=round(total_fats, 1),
                calories=round(total_calories, 1)
            ),
            items=item_breakdowns,
            top_alternatives=vision_res.top_alternatives,
            confidence=round(vision_res.confidence, 2),
            source="IFCT_2017",
            vision_provider=vision_res.vision_provider,
            scan_id=inserted_id_str,
            logged_at=now_iso
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in scan-food: {e}")
        raise HTTPException(status_code=500, detail="Food scanning failed. Please try again.")


@router.post("/api/scan/feedback")
@router.post("/api/scan-feedback")
async def record_scan_feedback(
    feedback: ScanFeedbackRequest,
    user_id: str = Depends(verify_jwt)
):
    """
    Stores user corrections and feedback for continuous vision and nutrition refinement.
    """
    try:
        feedback_doc = {
            "userId": user_id,
            "scanId": feedback.scan_id,
            "predictedFood": feedback.predicted_food,
            "correctedFood": feedback.corrected_food,
            "rating": feedback.rating,
            "comments": feedback.comments,
            "timestamp": datetime.now(timezone.utc).isoformat()
        }
        try:
            await db["scan_feedback"].insert_one(feedback_doc)
        except Exception as db_err:
            logger.warning(f"Failed to insert scan feedback to MongoDB: {db_err}")

        return {"success": True, "message": "Feedback recorded successfully. Thank you!"}
    except Exception as e:
        logger.error(f"Error recording feedback: {e}")
        raise HTTPException(status_code=500, detail="Failed to save scan feedback.")
