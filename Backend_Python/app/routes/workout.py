from fastapi import APIRouter, HTTPException, Body, status, Query
from app.models.workout import (
    Workout,
    WorkoutCreate,
    PlanGenerationRequest,
    GeneratedWorkoutPlan,
    AdoptWorkoutPlanRequest
)
from app.database import (
    workouts_collection,
    user_profiles_collection,
    exercises_catalog_collection
)
from app.services.workout_generator import generate_personalized_workout_plan
from app.services.dataset_loader import seed_exercise_catalog, get_cached_exercises
from typing import List, Optional
from datetime import datetime
from bson import ObjectId

router = APIRouter()


@router.post("/generate-plan", response_description="Generate a personalized workout plan", response_model=GeneratedWorkoutPlan)
async def generate_plan(request: PlanGenerationRequest = Body(...)):
    """Generates an intelligent personalized weekly routine based on user biometrics, schedule, and goals."""
    try:
        plan = generate_personalized_workout_plan(
            device_id=request.deviceId,
            weight_kg=request.weightKg,
            height_cm=request.heightCm,
            age=request.age or 24,
            gender=request.gender or "other",
            fitness_goal=request.fitnessGoal,
            days_per_week=request.daysPerWeek,
            session_duration_minutes=request.sessionDurationMinutes,
            experience_level=request.experienceLevel,
            available_equipment=request.availableEquipment,
            focus_muscles=request.focusMuscles
        )
        return plan
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Plan generation failed: {str(e)}")


@router.post("/adopt-plan", response_description="Adopt and schedule a generated workout plan")
async def adopt_plan(request: AdoptWorkoutPlanRequest = Body(...)):
    """Saves generated plan to user profile active split and queues daily workout templates."""
    profile = await user_profiles_collection.find_one({"deviceId": request.deviceId})
    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found")

    # Update activeSplit on user profile
    await user_profiles_collection.update_one(
        {"deviceId": request.deviceId},
        {
            "$set": {
                "activeSplit": request.plan.title,
                "updatedAt": datetime.utcnow()
            }
        }
    )

    # Save initial daily workouts into workouts collection
    created_ids = []
    for routine in request.plan.dailyRoutines:
        if routine.isRestDay or not routine.exercises:
            continue

        workout_exercises = []
        for ex in routine.exercises:
            # Parse reps integer from string range like "8-12 reps" -> 10
            reps_val = 10
            if "-" in ex.targetReps:
                parts = ex.targetReps.split("-")
                try:
                    reps_val = int(parts[0].strip())
                except Exception:
                    reps_val = 10

            workout_exercises.append({
                "exerciseName": ex.name,
                "sets": ex.targetSets,
                "reps": reps_val,
                "weight": ex.suggestedWeightKg or 0.0
            })

        workout_doc = {
            "deviceId": request.deviceId,
            "workoutName": routine.dayName,
            "exercises": workout_exercises,
            "totalVolume": 0.0,
            "date": datetime.utcnow(),
            "createdAt": datetime.utcnow(),
            "updatedAt": datetime.utcnow()
        }
        res = await workouts_collection.insert_one(workout_doc)
        created_ids.append(str(res.inserted_id))

    return {
        "success": True,
        "message": f"Successfully adopted plan '{request.plan.title}'",
        "createdWorkoutIds": created_ids
    }


@router.get("/catalog/exercises", response_description="List or search exercise catalog")
async def get_exercise_catalog(
    search: Optional[str] = None,
    body_part: Optional[str] = None,
    target_muscle: Optional[str] = None,
    equipment: Optional[str] = None,
    limit: int = 50,
    skip: int = 0
):
    """Retrieves exercises from catalog with optional filters."""
    query = {}
    if search:
        query["name"] = {"$regex": search, "$options": "i"}
    if body_part:
        query["bodyParts"] = {"$in": [body_part.lower()]}
    if target_muscle:
        query["targetMuscles"] = {"$in": [target_muscle.lower()]}
    if equipment:
        query["equipments"] = {"$in": [equipment.lower()]}

    cursor = exercises_catalog_collection.find(query).skip(skip).limit(limit)
    exercises = await cursor.to_list(limit)

    # Fallback to local cache if DB has no entries
    if not exercises:
        all_cached = get_cached_exercises()
        filtered = all_cached
        if search:
            filtered = [e for e in filtered if search.lower() in e.get("name", "").lower()]
        if body_part:
            filtered = [e for e in filtered if any(bp.lower() == body_part.lower() for bp in e.get("bodyParts", []))]
        if target_muscle:
            filtered = [e for e in filtered if any(tm.lower() == target_muscle.lower() for tm in e.get("targetMuscles", []))]
        if equipment:
            filtered = [e for e in filtered if any(eq.lower() == equipment.lower() for eq in e.get("equipments", []))]
        exercises = filtered[skip : skip + limit]

    for ex in exercises:
        if "_id" in ex:
            ex["_id"] = str(ex["_id"])
        gif_file = ex.get("gifUrl", "")
        if gif_file and not gif_file.startswith("http"):
            ex["gifUrl"] = f"/static/exercise-gifs/{gif_file}"

    return exercises


@router.post("/catalog/seed", response_description="Manually seed exercise catalog")
async def seed_catalog():
    """Seeds the MongoDB exercise catalog from local dataset files."""
    try:
        count = await seed_exercise_catalog(exercises_catalog_collection)
        return {"success": True, "count": count, "message": "Catalog seeded successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to seed catalog: {str(e)}")


@router.post("/", response_description="Create a new workout", response_model=Workout, status_code=status.HTTP_201_CREATED)
async def create_workout(workout: WorkoutCreate = Body(...)):
    profile = await user_profiles_collection.find_one({"deviceId": workout.deviceId})
    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found. Please register first.")

    new_workout = workout.dict()
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
