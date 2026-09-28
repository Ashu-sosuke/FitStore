from pydantic import BaseModel, Field, validator
from typing import Optional, List
from datetime import datetime, timezone
from enum import Enum

class FitnessGoal(str, Enum):
    weight_loss = "weight_loss"
    muscle_gain = "muscle_gain"
    maintenance = "maintenance"
    endurance = "endurance"
    flexibility = "flexibility"
    strength = "strength"
    recomposition = "recomposition"

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
    dailyStepTarget: int = Field(10000, ge=1000, le=100000, description="Custom daily step target")

    # --- Comprehensive Onboarding Attributes ---
    goalPriority: Optional[str] = Field("balanced", description="max_muscle, max_strength, balanced, fat_loss_retention")
    trainingHistory: Optional[str] = Field("just_starting", description="just_starting, under_3m, 3_6m, 6_12m, 1_2y, 2y_plus")
    currentRoutine: Optional[str] = Field("no_routine", description="full_body, upper_lower, ppl, bro_split, other, no_routine")
    trainingLocation: Optional[str] = Field("commercial_gym", description="commercial_gym, home_gym, outdoor, studio")
    warmupIncluded: bool = Field(True, description="Whether warm-up and cool-down are budgeted in session time")
    daysPerWeek: int = Field(4, ge=2, le=6, description="Workout days per week (2-6)")
    sessionDurationMinutes: int = Field(60, ge=20, le=120, description="Session time constraint in minutes")
    focusMuscles: List[str] = Field(default_factory=list, description="Target focus muscles to increase weekly volume")
    avoidMuscles: List[str] = Field(default_factory=list, description="Muscles to de-prioritize")
    preferredExercises: List[str] = Field(default_factory=list, description="Favorite exercises prioritized by AI ranking")
    dislikedExercises: List[str] = Field(default_factory=list, description="Exercises to filter out/avoid")
    physicalLimitations: List[str] = Field(default_factory=list, description="Injuries/limitations: shoulder, knee, lower_back, wrist, elbow, ankle, neck, none")
    trainingStyle: Optional[str] = Field("bodybuilding", description="bodybuilding, high_intensity, circuit, strength, mobility, athletic, ai_decide")
    intensityPreference: Optional[str] = Field("moderate", description="easy, moderate, hard, very_hard")
    sleepHours: Optional[str] = Field("7_8h", description="under_5h, 5_6h, 6_7h, 7_8h, 8h_plus")
    stressLevel: Optional[str] = Field("moderate", description="low, moderate, high")
    dietPreference: Optional[str] = Field("non_veg", description="vegetarian, vegan, non_veg, eggetarian, custom")
    mealFrequency: int = Field(3, ge=1, le=8, description="Target daily meals")
    nutritionPriority: Optional[str] = Field("high_protein", description="high_protein, balanced, low_calorie, flexible")
    planAdaptability: Optional[str] = Field("hybrid", description="adaptive, fixed, hybrid")
    progressionModel: Optional[str] = Field("progressive_overload", description="progressive_overload, reps_first, weight_first, ai_decides")

class UserProfileCreate(UserProfileBase):
    pass

class UserProfileUpdate(BaseModel):
    deviceId: Optional[str] = None
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
    dailyStepTarget: Optional[int] = None

    goalPriority: Optional[str] = None
    trainingHistory: Optional[str] = None
    currentRoutine: Optional[str] = None
    trainingLocation: Optional[str] = None
    warmupIncluded: Optional[bool] = None
    daysPerWeek: Optional[int] = None
    sessionDurationMinutes: Optional[int] = None
    focusMuscles: Optional[List[str]] = None
    avoidMuscles: Optional[List[str]] = None
    preferredExercises: Optional[List[str]] = None
    dislikedExercises: Optional[List[str]] = None
    physicalLimitations: Optional[List[str]] = None
    trainingStyle: Optional[str] = None
    intensityPreference: Optional[str] = None
    sleepHours: Optional[str] = None
    stressLevel: Optional[str] = None
    dietPreference: Optional[str] = None
    mealFrequency: Optional[int] = None
    nutritionPriority: Optional[str] = None
    planAdaptability: Optional[str] = None
    progressionModel: Optional[str] = None

class UserProfile(UserProfileBase):
    id: Optional[str] = Field(None, alias="_id")
    createdAt: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    updatedAt: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))

    class Config:
        populate_by_name = True
        arbitrary_types_allowed = True
        json_encoders = {datetime: lambda v: v.isoformat()}
