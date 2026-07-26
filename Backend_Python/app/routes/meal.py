from fastapi import APIRouter, HTTPException, Body, status, Query
from app.models.meal import Meal, MealCreate, Nutrient, NutrientCreate
from app.database import meals_collection, user_profiles_collection, nutrients_collection
from typing import List, Optional
from datetime import datetime
from bson import ObjectId

router = APIRouter()

@router.post("/", response_description="Add a new meal", response_model=Meal, status_code=status.HTTP_201_CREATED)
async def add_meal(meal: MealCreate = Body(...)):
    # Verify profile exists
    profile = await user_profiles_collection.find_one({"deviceId": meal.deviceId})
    if not profile:
        raise HTTPException(status_code=404, detail="User profile not found. Please register first.")
        
    new_meal = meal.dict()
    new_meal["createdAt"] = datetime.utcnow()
    
    result = await meals_collection.insert_one(new_meal)
    created_meal = await meals_collection.find_one({"_id": result.inserted_id})
    created_meal["_id"] = str(created_meal["_id"])
    return created_meal

@router.get("/{device_id}", response_description="List all meals for a device", response_model=List[Meal])
async def list_meals(device_id: str, limit: int = 20, skip: int = 0):
    meals = await meals_collection.find({"deviceId": device_id})\
        .sort("createdAt", -1)\
        .skip(skip)\
        .limit(limit)\
        .to_list(limit)
    for m in meals:
        m["_id"] = str(m["_id"])
    return meals

@router.get("/summary/{device_id}", response_description="Get daily nutrition summary")
async def get_daily_summary(device_id: str):
    # Today's range
    today = datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    
    pipeline = [
        {"$match": {"deviceId": device_id, "createdAt": {"$gte": today}}},
        {"$group": {
            "_id": None,
            "totalCalories": {"$sum": "$calories"},
            "totalProtein": {"$sum": "$protein"},
            "totalCarbs": {"$sum": "$carbs"},
            "totalFats": {"$sum": "$fats"},
            "mealCount": {"$sum": 1}
        }}
    ]
    
    result = await meals_collection.aggregate(pipeline).to_list(1)
    if not result:
        return {
            "totalCalories": 0,
            "totalProtein": 0,
            "totalCarbs": 0,
            "totalFats": 0,
            "mealCount": 0
        }
    return result[0]

@router.get("/search-food", response_description="Search for food items in the database", response_model=List[Nutrient])
async def search_food(query: str = Query(..., min_length=1)):
    # Case-insensitive regex search in nutrients collection
    foods = await nutrients_collection.find({"food_name": {"$regex": query, "$options": "i"}}).to_list(10)
    for f in foods:
        f["_id"] = str(f["_id"])
    return foods

@router.post("/add-food", response_description="Add a new custom food item to the database", response_model=Nutrient, status_code=status.HTTP_201_CREATED)
async def add_custom_food(food: NutrientCreate = Body(...)):
    # Check if a food with the same name exists (case-insensitive)
    existing = await nutrients_collection.find_one({"food_name": {"$regex": f"^{food.food_name}$", "$options": "i"}})
    if existing:
        existing["_id"] = str(existing["_id"])
        return existing

    new_food = food.dict()
    result = await nutrients_collection.insert_one(new_food)
    created = await nutrients_collection.find_one({"_id": result.inserted_id})
    created["_id"] = str(created["_id"])
    return created
