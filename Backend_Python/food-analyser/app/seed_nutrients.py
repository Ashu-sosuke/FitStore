"""
Seed script – populates the `nutrients` collection in MongoDB with
nutritional data for the six supported food categories.

Run once:
    python -m app.seed_nutrients

Values are approximate per 100 g serving (sourced from USDA FoodData Central).
"""

import asyncio

import certifi
from motor.motor_asyncio import AsyncIOMotorClient

from app.config import MONGO_URI, DATABASE_NAME

NUTRIENT_DATA = [
    {
        "food_name": "Egg",
        "protein_g": 13.0,
        "carbs_g": 1.1,
        "fats_g": 11.0,
        "calories": 155,
        "serving_size": "100g (≈ 2 large eggs)",
    },
    {
        "food_name": "Chicken",
        "protein_g": 31.0,
        "carbs_g": 0.0,
        "fats_g": 3.6,
        "calories": 165,
        "serving_size": "100g cooked breast",
    },
    {
        "food_name": "Milk",
        "protein_g": 3.4,
        "carbs_g": 5.0,
        "fats_g": 3.3,
        "calories": 61,
        "serving_size": "100ml whole milk",
    },
    {
        "food_name": "Broccoli",
        "protein_g": 2.8,
        "carbs_g": 7.0,
        "fats_g": 0.4,
        "calories": 34,
        "serving_size": "100g raw",
    },
    {
        "food_name": "Avocado",
        "protein_g": 2.0,
        "carbs_g": 8.5,
        "fats_g": 14.7,
        "calories": 160,
        "serving_size": "100g flesh",
    },
    {
        "food_name": "Salmon",
        "protein_g": 20.0,
        "carbs_g": 0.0,
        "fats_g": 13.0,
        "calories": 208,
        "serving_size": "100g cooked fillet",
    },
]


async def seed():
    """Upsert all nutrient documents (idempotent)."""
    client = AsyncIOMotorClient(MONGO_URI)
    db = client[DATABASE_NAME]
    collection = db.nutrients

    for item in NUTRIENT_DATA:
        result = await collection.update_one(
            {"food_name": item["food_name"]},
            {"$set": item},
            upsert=True,
        )
        action = "inserted" if result.upserted_id else "updated"
        print(f"  [OK] {action}: {item['food_name']}")

    # Create an index on food_name for fast lookups
    await collection.create_index("food_name", unique=True)
    print("\n[DONE] Nutrient seed complete!")

    client.close()


if __name__ == "__main__":
    asyncio.run(seed())
