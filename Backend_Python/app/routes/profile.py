from fastapi import APIRouter, HTTPException, Body, status
from app.models.user_profile import UserProfile, UserProfileCreate, UserProfileUpdate
from app.database import user_profiles_collection
from typing import List
from datetime import datetime, timezone
from bson import ObjectId
import random
import string

router = APIRouter()

def generate_unique_code():
    return "".join(random.choices(string.ascii_uppercase + string.digits, k=6))

@router.post("/", response_description="Create or update a user profile", response_model=UserProfile, status_code=status.HTTP_201_CREATED)
async def create_profile(profile: UserProfileCreate = Body(...)):
    new_profile = profile.dict()
    new_profile["userId"] = profile.deviceId  # Keep userId synced with deviceId
    new_profile["updatedAt"] = datetime.now(timezone.utc)
    
    # Check if profile already exists for deviceId or userId
    existing = await user_profiles_collection.find_one({
        "$or": [{"deviceId": profile.deviceId}, {"userId": profile.deviceId}]
    })
    
    if existing:
        # Preserve existing friendCode if new one not provided or placeholder
        if not new_profile.get("friendCode") or new_profile["friendCode"] == "------":
            new_profile["friendCode"] = existing.get("friendCode")

        if not new_profile.get("friendCode") or new_profile["friendCode"] == "------":
            for _attempt in range(100):
                code = generate_unique_code()
                code_exists = await user_profiles_collection.find_one({"friendCode": code})
                if not code_exists:
                    new_profile["friendCode"] = code
                    break

        await user_profiles_collection.update_one(
            {"_id": existing["_id"]},
            {"$set": new_profile}
        )
        updated_profile = await user_profiles_collection.find_one({"_id": existing["_id"]})
        updated_profile["_id"] = str(updated_profile["_id"])
        return updated_profile

    # New profile creation
    new_profile["createdAt"] = datetime.now(timezone.utc)
    
    # Generate friendCode if missing or placeholder
    if not new_profile.get("friendCode") or new_profile["friendCode"] == "------":
        for _attempt in range(100):
            code = generate_unique_code()
            code_exists = await user_profiles_collection.find_one({"friendCode": code})
            if not code_exists:
                new_profile["friendCode"] = code
                break
        else:
            raise HTTPException(status_code=500, detail="Could not generate a unique friend code")
                
    result = await user_profiles_collection.insert_one(new_profile)
    created_profile = await user_profiles_collection.find_one({"_id": result.inserted_id})
    created_profile["_id"] = str(created_profile["_id"])
    return created_profile

@router.get("/{device_id}", response_description="Get a user profile by deviceId", response_model=UserProfile)
async def get_profile(device_id: str):
    profile = await user_profiles_collection.find_one({
        "$or": [{"deviceId": device_id}, {"userId": device_id}]
    })
    if profile:
        # Generate and save friendCode on the fly if missing or placeholder
        if not profile.get("friendCode") or profile["friendCode"] == "------":
            for _attempt in range(100):
                code = generate_unique_code()
                code_exists = await user_profiles_collection.find_one({"friendCode": code})
                if not code_exists:
                    await user_profiles_collection.update_one(
                        {"_id": profile["_id"]},
                        {"$set": {"friendCode": code}}
                    )
                    profile["friendCode"] = code
                    break
            else:
                raise HTTPException(status_code=500, detail="Could not generate a unique friend code")
        profile["_id"] = str(profile["_id"])
        return profile
    raise HTTPException(status_code=404, detail=f"Profile with deviceId {device_id} not found")


@router.put("/{device_id}", response_description="Update a user profile", response_model=UserProfile)
async def update_profile(device_id: str, profile: UserProfileUpdate = Body(...)):
    update_data = {k: v for k, v in profile.dict().items() if v is not None}
    update_data["updatedAt"] = datetime.now(timezone.utc)
    
    if len(update_data) >= 1:
        update_result = await user_profiles_collection.update_one(
            {"$or": [{"deviceId": device_id}, {"userId": device_id}]}, 
            {"$set": update_data}
        )
        if update_result.modified_count == 1 or update_result.matched_count == 1:
            updated_profile = await user_profiles_collection.find_one({
                "$or": [{"deviceId": device_id}, {"userId": device_id}]
            })
            if updated_profile:
                updated_profile["_id"] = str(updated_profile["_id"])
                return updated_profile
    
    existing_profile = await user_profiles_collection.find_one({
        "$or": [{"deviceId": device_id}, {"userId": device_id}]
    })
    if existing_profile:
        existing_profile["_id"] = str(existing_profile["_id"])
        return existing_profile
        
    raise HTTPException(status_code=404, detail=f"Profile with deviceId {device_id} not found")


@router.delete("/{device_id}", response_description="Delete user profile and all associated data")
async def delete_profile(device_id: str):
    await user_profiles_collection.delete_many({
        "$or": [{"deviceId": device_id}, {"userId": device_id}]
    })
    try:
        from app.database import db, workouts_collection, meals_collection
        await workouts_collection.delete_many({"$or": [{"deviceId": device_id}, {"userId": device_id}]})
        await meals_collection.delete_many({"$or": [{"deviceId": device_id}, {"userId": device_id}]})
        await db["daily_logs"].delete_many({"user_id": device_id})
        await db["friends"].delete_many({"$or": [{"userId": device_id}, {"friendId": device_id}]})
        await db["leaderboard_stats"].delete_many({"userId": device_id})
        await db["scan_feedback"].delete_many({"userId": device_id})
    except Exception as e:
        pass
    return {"status": "success", "message": f"All data for {device_id} deleted."}


