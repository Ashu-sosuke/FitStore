from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime
from enum import Enum

class MealType(str, Enum):
    breakfast = "breakfast"
    lunch = "lunch"
    dinner = "dinner"
    snack = "snack"

class MealBase(BaseModel):
    deviceId: str = Field(..., description="Device ID is required")
    mealType: MealType
    foodName: str = Field(..., description="Food name is required")
    calories: float = Field(..., ge=0, description="Calories must be non-negative")
    protein: float = Field(0, ge=0, description="Protein must be non-negative")
    carbs: float = Field(0, ge=0, description="Carbs must be non-negative")
    fats: float = Field(0, ge=0, description="Fats must be non-negative")

class MealCreate(MealBase):
    pass

class Meal(MealBase):
    id: Optional[str] = Field(None, alias="_id")
    createdAt: datetime = Field(default_factory=datetime.utcnow)

    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True
        json_encoders = {datetime: lambda v: v.isoformat()}

class NutrientBase(BaseModel):
    food_name: str = Field(..., description="Food name is required")
    calories: float = Field(..., ge=0, description="Calories must be non-negative")
    protein_g: float = Field(0, ge=0, description="Protein must be non-negative")
    carbs_g: float = Field(0, ge=0, description="Carbs must be non-negative")
    fats_g: float = Field(0, ge=0, description="Fats must be non-negative")
    serving_size: Optional[str] = "100g"

class NutrientCreate(NutrientBase):
    pass

class Nutrient(NutrientBase):
    id: Optional[str] = Field(None, alias="_id")

    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True

