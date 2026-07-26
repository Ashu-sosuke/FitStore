from fastapi import APIRouter, HTTPException, Body, status
from app.models.user_profile import UserProfile, UserProfileCreate, UserProfileUpdate
from app.database import user_profiles_collection
from typing import List
from datetime import datetime
from bson import ObjectId
import random
import string

router = APIRouter()

def generate_unique_code():
    return "".join(random.choices(string.ascii_uppercase + string.digits, k=6))

@router.post("/", response_description="Create a new user profile", response_model=UserProfile, status_code=status.HTTP_201_CREATED)
async def create_profile(profile: UserProfileCreate = Body(...)):
    # Check if profile already exists for deviceId or userId
    existing = await user_profiles_collection.find_one({
        "$or": [{"deviceId": profile.deviceId}, {"userId": profile.deviceId}]
    })
    if existing:
        raise HTTPException(status_code=400, detail="Profile already exists for this device")
    
    new_profile = profile.dict()
    new_profile["userId"] = profile.deviceId  # Keep userId synced with deviceId
    new_profile["createdAt"] = datetime.utcnow()
    new_profile["updatedAt"] = datetime.utcnow()
    
    # Generate friendCode if missing or placeholder
    if not new_profile.get("friendCode") or new_profile["friendCode"] == "------":
        while True:
            code = generate_unique_code()
            code_exists = await user_profiles_collection.find_one({"friendCode": code})
            if not code_exists:
                new_profile["friendCode"] = code
                break
                
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
            while True:
                code = generate_unique_code()
                code_exists = await user_profiles_collection.find_one({"friendCode": code})
                if not code_exists:
                    await user_profiles_collection.update_one(
                        {"_id": profile["_id"]},
                        {"$set": {"friendCode": code}}
                    )
                    profile["friendCode"] = code
                    break
        profile["_id"] = str(profile["_id"])
        return profile
    raise HTTPException(status_code=404, detail=f"Profile with deviceId {device_id} not found")


@router.put("/{device_id}", response_description="Update a user profile", response_model=UserProfile)
async def update_profile(device_id: str, profile: UserProfileUpdate = Body(...)):
    update_data = {k: v for k, v in profile.dict().items() if v is not None}
    update_data["updatedAt"] = datetime.utcnow()
    
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

