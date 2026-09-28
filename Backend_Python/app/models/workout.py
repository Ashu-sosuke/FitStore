from pydantic import BaseModel, Field, validator
from typing import List, Optional
from datetime import datetime, timezone


class Exercise(BaseModel):
    exerciseName: str = Field(..., description="Exercise name is required")
    sets: int = Field(..., ge=1, description="Sets must be at least 1")
    reps: int = Field(..., ge=1, description="Reps must be at least 1")
    weight: float = Field(0, ge=0, description="Weight must be non-negative")


class WorkoutBase(BaseModel):
    deviceId: str = Field(..., description="Device ID is required")
    workoutName: str = Field(..., description="Workout name is required")
    exercises: List[Exercise]
    date: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))

    @validator('exercises')
    def exercises_not_empty(cls, v):
        if len(v) == 0:
            raise ValueError('At least one exercise is required')
        return v


class WorkoutCreate(WorkoutBase):
    pass


class Workout(WorkoutBase):
    id: Optional[str] = Field(None, alias="_id")
    totalVolume: float = 0
    createdAt: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    updatedAt: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))

    @validator('totalVolume', pre=True, always=True)
    def calculate_total_volume(cls, v, values):
        exercises = values.get('exercises', [])
        return sum(ex.sets * ex.reps * ex.weight for ex in exercises)

    class Config:
        populate_by_name = True
        arbitrary_types_allowed = True
        json_encoders = {datetime: lambda v: v.isoformat()}


# --- AI Multi-Dimensional Workout Plan Generator Models ---

class PlanGenerationRequest(BaseModel):
    deviceId: str = Field(..., description="Device ID of the user")
    weightKg: float = Field(..., ge=20.0, le=300.0, description="Weight in kilograms")
    heightCm: float = Field(..., ge=100.0, le=250.0, description="Height in centimeters")
    age: Optional[int] = Field(24, ge=12, le=100)
    gender: Optional[str] = Field("other", description="male, female, or other")
    fitnessGoal: str = Field("bulk_up", description="bulk_up, cut_down, strength, endurance, general_fitness, recomposition")
    daysPerWeek: int = Field(4, ge=2, le=6, description="Workout days available per week (2 to 6)")
    sessionDurationMinutes: int = Field(60, ge=20, le=120, description="Session time constraint in minutes")
    experienceLevel: str = Field("beginner", description="beginner, intermediate, advanced")
    availableEquipment: Optional[List[str]] = Field(None, description="List of available equipments")

    # Multi-dimensional factors
    goalPriority: Optional[str] = Field("balanced", description="max_muscle, max_strength, balanced, fat_loss_retention")
    focusMuscles: Optional[List[str]] = Field(default_factory=list, description="Target muscles to allocate higher volume")
    avoidMuscles: Optional[List[str]] = Field(default_factory=list, description="Muscles to de-prioritize")
    preferredExercises: Optional[List[str]] = Field(default_factory=list, description="Favorite movements to prioritize")
    dislikedExercises: Optional[List[str]] = Field(default_factory=list, description="Movements to exclude")
    physicalLimitations: Optional[List[str]] = Field(default_factory=list, description="Reported injuries/limitations: shoulder, knee, lower_back, wrist, elbow, ankle, neck, none")
    trainingStyle: Optional[str] = Field("bodybuilding", description="bodybuilding, high_intensity, circuit, strength, mobility, athletic, ai_decide")
    intensityPreference: Optional[str] = Field("moderate", description="easy, moderate, hard, very_hard")
    sleepHours: Optional[str] = Field("7_8h", description="under_5h, 5_6h, 6_7h, 7_8h, 8h_plus")
    stressLevel: Optional[str] = Field("moderate", description="low, moderate, high")
    trainingLocation: Optional[str] = Field("commercial_gym", description="commercial_gym, home_gym, outdoor, studio")
    warmupIncluded: bool = Field(True, description="Allocate dedicated warmup/cooldown in routine budget")
    progressionModel: Optional[str] = Field("progressive_overload", description="progressive_overload, reps_first, weight_first, ai_decides")


class GeneratedExercise(BaseModel):
    exerciseId: str
    name: str
    targetMuscles: List[str]
    bodyParts: List[str]
    equipments: List[str]
    secondaryMuscles: List[str] = []
    instructions: List[str] = []
    gifUrl: str
    targetSets: int
    targetReps: str
    suggestedWeightKg: Optional[float] = None
    restSeconds: int
    estimatedMinutes: float
    rirTarget: Optional[int] = 2
    progressionProtocol: Optional[str] = None


class DailyWorkoutRoutine(BaseModel):
    dayNumber: int
    dayName: str
    splitCategory: str
    isRestDay: bool = False
    targetFocus: str
    estimatedDurationMinutes: int
    warmupMinutes: int = 5
    warmupNotes: List[str] = []
    cooldownMinutes: int = 5
    cooldownNotes: List[str] = []
    exercises: List[GeneratedExercise] = []


class GeneratedWorkoutPlan(BaseModel):
    planId: str
    title: str
    description: str
    goal: str
    daysPerWeek: int
    sessionDurationMinutes: int
    experienceLevel: str
    weeklyVolumeScore: float
    dailyRoutines: List[DailyWorkoutRoutine]
    recommendedCaloricSurplusOrDeficit: str
    nutritionTip: str
    progressionOverview: Optional[str] = None
    injurySafetyNotes: Optional[List[str]] = None
    recoveryAdvisory: Optional[str] = None


class AdoptWorkoutPlanRequest(BaseModel):
    deviceId: str
    plan: GeneratedWorkoutPlan
