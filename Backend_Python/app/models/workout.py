from pydantic import BaseModel, Field, validator
from typing import List, Optional
from datetime import datetime


class Exercise(BaseModel):
    exerciseName: str = Field(..., description="Exercise name is required")
    sets: int = Field(..., ge=1, description="Sets must be at least 1")
    reps: int = Field(..., ge=1, description="Reps must be at least 1")
    weight: float = Field(0, ge=0, description="Weight must be non-negative")


class WorkoutBase(BaseModel):
    deviceId: str = Field(..., description="Device ID is required")
    workoutName: str = Field(..., description="Workout name is required")
    exercises: List[Exercise]
    date: datetime = Field(default_factory=datetime.utcnow)

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
    createdAt: datetime = Field(default_factory=datetime.utcnow)
    updatedAt: datetime = Field(default_factory=datetime.utcnow)

    @validator('totalVolume', pre=True, always=True)
    def calculate_total_volume(cls, v, values):
        exercises = values.get('exercises', [])
        return sum(ex.sets * ex.reps * ex.weight for ex in exercises)

    class Config:
        allow_population_by_field_name = True
        arbitrary_types_allowed = True
        json_encoders = {datetime: lambda v: v.isoformat()}


# --- AI Workout Plan Generator Models ---

class PlanGenerationRequest(BaseModel):
    deviceId: str = Field(..., description="Device ID of the user")
    weightKg: float = Field(..., ge=20.0, le=300.0, description="Weight in kilograms (e.g. 52.0)")
    heightCm: float = Field(..., ge=100.0, le=250.0, description="Height in centimeters (e.g. 173.0 for 5'8\")")
    age: Optional[int] = Field(24, ge=12, le=100)
    gender: Optional[str] = Field("other", description="male, female, or other")
    fitnessGoal: str = Field("bulk_up", description="bulk_up, cut_down, strength, endurance, general_fitness")
    daysPerWeek: int = Field(5, ge=2, le=6, description="Workout days available per week (2 to 6)")
    sessionDurationMinutes: int = Field(60, ge=30, le=90, description="Session time constraint in minutes (30, 45, 60, 90)")
    experienceLevel: str = Field("beginner", description="beginner, intermediate, advanced")
    availableEquipment: Optional[List[str]] = Field(None, description="List of available equipments e.g. barbell, dumbbell, cable, sled machine, body weight")
    focusMuscles: Optional[List[str]] = Field(None, description="Optional target focus areas")


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


class DailyWorkoutRoutine(BaseModel):
    dayNumber: int
    dayName: str
    splitCategory: str
    isRestDay: bool = False
    targetFocus: str
    estimatedDurationMinutes: int
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


class AdoptWorkoutPlanRequest(BaseModel):
    deviceId: str
    plan: GeneratedWorkoutPlan
