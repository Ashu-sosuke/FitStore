"""
Pydantic response / request schemas for the /scan-food endpoint.

Using strict models to guarantee the Android client always receives a
predictable JSON shape.
"""

from __future__ import annotations

from datetime import datetime

from pydantic import BaseModel, Field


# ── Nested models ────────────────────────────────────────────────────────────

class Macros(BaseModel):
    """Macronutrient breakdown per 100 g serving."""
    protein_g: float = Field(..., description="Protein in grams")
    carbs_g: float = Field(..., description="Carbohydrates in grams")
    fats_g: float = Field(..., description="Total fat in grams")
    calories: float = Field(..., description="Energy in kcal")


class DailyLogEntry(BaseModel):
    """Shape of a document written to the `daily_logs` collection."""
    user_id: str
    food_name: str
    nutrients: Macros
    timestamp: str = Field(
        ...,
        description="ISO-8601 timestamp of when the food was logged",
    )


# ── API response ─────────────────────────────────────────────────────────────

class ScanFoodResponse(BaseModel):
    """Top-level JSON returned to the Android client."""
    success: bool
    food_name: str
    calories: float
    macros: Macros
    confidence: float = Field(
        ...,
        description="Model confidence (0-1) for the predicted label",
    )
    logged_at: str = Field(
        ...,
        description="ISO-8601 timestamp of the created daily_log entry",
    )
    message: str = "Food identified and logged successfully"


class ErrorResponse(BaseModel):
    """Standard error envelope."""
    success: bool = False
    message: str
    detail: str | None = None
