import asyncio
import os
import sys
from dotenv import load_dotenv
from app.database import client, db, user_profiles_collection, workouts_collection, meals_collection, exercises_catalog_collection

# Force UTF-8 stdout
if sys.stdout.encoding != 'utf-8':
    sys.stdout.reconfigure(encoding='utf-8')

load_dotenv()

async def inspect_database():
    print("=" * 60)
    print("FitStore MongoDB Inspector")
    print("=" * 60)

    try:
        # 1. List Collections and counts
        collections = await db.list_collection_names()
        print(f"\n[Database Collections] ({len(collections)} total):")
        for coll_name in sorted(collections):
            count = await db[coll_name].count_documents({})
            print(f"  - {coll_name:<20}: {count:>5} documents")

        # 2. View User Profiles
        print("\n[User Profiles in MongoDB]:")
        users = await user_profiles_collection.find({}).to_list(10)
        if not users:
            print("  (Empty - No user profiles found in MongoDB)")
        else:
            for idx, u in enumerate(users, 1):
                print(f"  [{idx}] Name: {u.get('name', 'N/A')} | DeviceID: {u.get('deviceId', 'N/A')}")
                print(f"      Goal: {u.get('fitnessGoal')} | Active Split: {u.get('activeSplit', 'None')}")
                print(f"      Daily Calories: {u.get('dailyCalorieTarget')} kcal | Streak: {u.get('currentStreak')}")

        # 3. View Workouts Count by Device
        print("\n[Workouts in MongoDB]:")
        workouts = await workouts_collection.find({}).to_list(10)
        if not workouts:
            print("  (Empty - No workouts found in MongoDB)")
        else:
            for w in workouts:
                ex_count = len(w.get('exercises', []))
                print(f"  - Device {w.get('deviceId')}: '{w.get('workoutName')}' ({ex_count} exercises)")

    except Exception as e:
        print(f"\n[ERROR] Database check failed: {e}")
    finally:
        client.close()

if __name__ == "__main__":
    asyncio.run(inspect_database())
