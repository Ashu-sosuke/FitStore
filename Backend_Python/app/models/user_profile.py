from pydantic import BaseModel, Field, validator
from typing import Optional
from datetime import datetime
from enum import Enum

class FitnessGoal(str, Enum):
    weight_loss = "weight_loss"
    muscle_gain = "muscle_gain"
    maintenance = "maintenance"
    endurance = "endurance"
    flexibility = "flexibility"

class ActivityLevel(str, Enum):
    sedentary = "sedentary"
    lightly_active = "lightly_active"
    moderately_active = "moderately_active"
    very_active = "very_active"
    extra_active = "extra_active"

class UserProfileBase(BaseModel):
    deviceId: str = Field(..., description="Device ID is required")
    name: str = Field(..., description="Name is required")
    age: int = Field(..., ge=1, description="Age must be at least 1")
    gender: str = Field("other", description="Gender")
    height: float = Field(..., ge=1, description="Height must be positive")
    weight: float = Field(..., ge=1, description="Weight must be positive")
    fitnessGoal: FitnessGoal
    activityLevel: ActivityLevel = Field(ActivityLevel.moderately_active)
    dailyCalorieTarget: float = Field(..., ge=0, description="Calorie target must be non-negative")
    proteinTarget: float = Field(0.0, description="Protein target in grams")
    carbsTarget: float = Field(0.0, description="Carbs target in grams")
    fatsTarget: float = Field(0.0, description="Fats target in grams")
    activeSplit: Optional[str] = Field(None, description="Active workout split routine")
    currentStreak: int = Field(0, description="Current daily app launch streak")
    highestStreak: int = Field(0, description="Highest daily app launch streak")
    friendCode: Optional[str] = Field(None, description="Shareable friend referral code")
    showOnLeaderboards: bool = Field(True, description="Opt-in to show progress on leaderboards")

class UserProfileCreate(UserProfileBase):
    pass

class UserProfileUpdate(UserProfileBase):
    name: Optional[str] = None
    age: Optional[int] = None
    gender: Optional[str] = None
    height: Optional[float] = None
    weight: Optional[float] = None
    fitnessGoal: Optional[FitnessGoal] = None
    activityLevel: Optional[ActivityLevel] = None
    dailyCalorieTarget: Optional[float] = None
    proteinTarget: Optional[float] = None
    carbsTarget: Optional[float] = None
    fatsTarget: Optional[float] = None
    activeSplit: Optional[str] = None
    currentStreak: Optional[int] = None
    highestStreak: Optional[int] = None
    friendCode: Optional[str] = None
    showOnLeaderboards: Optional[bool] = None


class UserProfile(UserProfileBase):
    id: Optional[str] = Field(None, alias="_id")
    createdAt: datetime = Field(default_factory=datetime.utcnow)
    updatedAt: datetime = Field(default_factory=datetime.utcnow)

    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True
        json_encoders = {datetime: lambda v: v.isoformat()}
