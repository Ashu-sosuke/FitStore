from fastapi import APIRouter, HTTPException, Body, status
from app.models.workout import Workout, WorkoutCreate
from app.database import workouts_collection, user_profiles_collection
from typing import List, Optional
from datetime import datetime
from bson import ObjectId

router = APIRouter()

@router.post("/", response_description="Create a new workout", response_model=Workout, status_code=status.HTTP_201_CREATED)
async def create_workout(workout: WorkoutCreate = Body(...)):
    # Verify profile exists
    profile = await user_profiles_collection.find_one({"deviceId": workout.deviceId})
    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found. Please register first.")

    new_workout = workout.dict()
    # Calculate total volume
    new_workout["totalVolume"] = sum(ex["sets"] * ex["reps"] * ex["weight"] for ex in new_workout["exercises"])
    new_workout["createdAt"] = datetime.utcnow()
    new_workout["updatedAt"] = datetime.utcnow()
    
    result = await workouts_collection.insert_one(new_workout)
    created_workout = await workouts_collection.find_one({"_id": result.inserted_id})
    created_workout["_id"] = str(created_workout["_id"])
    return created_workout

@router.get("/{device_id}", response_description="List all workouts for a device", response_model=List[Workout])
async def list_workouts(device_id: str, limit: int = 20, skip: int = 0):
    workouts = await workouts_collection.find({"deviceId": device_id})\
        .sort("date", -1)\
        .skip(skip)\
        .limit(limit)\
        .to_list(limit)
    for w in workouts:
        w["_id"] = str(w["_id"])
    return workouts

@router.get("/detail/{workout_id}", response_description="Get a single workout by ID", response_model=Workout)
async def get_workout(workout_id: str):
    if not ObjectId.is_valid(workout_id):
        raise HTTPException(status_code=400, detail="Invalid workout ID")
        
    workout = await workouts_collection.find_one({"_id": ObjectId(workout_id)})
    if workout:
        workout["_id"] = str(workout["_id"])
        return workout
    raise HTTPException(status_code=404, detail=f"Workout with ID {workout_id} not found")
