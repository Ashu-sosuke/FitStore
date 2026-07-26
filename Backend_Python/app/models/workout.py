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
