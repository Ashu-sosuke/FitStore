from fastapi import APIRouter, HTTPException, Depends
from typing import List
from datetime import datetime, timedelta
import time
import random
import string
from app.database import db
from app.models.leaderboard import UserProfile, WorkoutPoints, AddFriendRequest, LeaderboardEntry

router = APIRouter()

# Scoring weights
SCORE_STEP_WEIGHT = 0.05      # 20 steps = 1 point
SCORE_WORKOUT_WEIGHT = 100.0   # 1 completed workout = 100 points

# In-memory leaderboard cache: { (userId, period): (timestamp, data) }
LEADERBOARD_CACHE = {}
CACHE_TTL = 60  # Cache duration of 60 seconds

@router.get("/code/{device_id}")
async def get_or_generate_code(device_id: str):
    # Find user profile
    profile = await db["userprofiles"].find_one({"deviceId": device_id})
    if not profile:
        profile = await db["userprofiles"].find_one({"userId": device_id})
        if not profile:
            raise HTTPException(status_code=404, detail="User profile not found")
    
    friend_code = profile.get("friendCode")
    if not friend_code or friend_code == "------":
        # Generate a unique 6-character code
        while True:
            code = "".join(random.choices(string.ascii_uppercase + string.digits, k=6))
            existing = await db["userprofiles"].find_one({"friendCode": code})
            if not existing:
                friend_code = code
                break
        await db["userprofiles"].update_one(
            {"_id": profile["_id"]},
            {"$set": {"friendCode": friend_code}}
        )
        # Mirror on userId just in case
        userId = profile.get("userId")
        if userId:
            await db["userprofiles"].update_one(
                {"userId": userId},
                {"$set": {"friendCode": friend_code}}
            )
            
    return {"friendCode": friend_code}

@router.post("/register")
async def register_user(profile: UserProfile):
    # Register the user
    update_doc = profile.model_dump()
    update_doc["deviceId"] = profile.userId
    await db["userprofiles"].update_one(
        {"userId": profile.userId},
        {"$set": update_doc},
        upsert=True
    )

    # Also initialize stats if not exists
    await db["leaderboard_stats"].update_one(
        {"userId": profile.userId},
        {"$setOnInsert": {
            "weeklyPoints": 0,
            "workoutsThisWeek": 0,
            "currentStreak": 0,
            "weeklySteps": 0,
            "allTimeSteps": 0,
            "stepsToday": 0,
            "lastStepsUpdate": datetime.utcnow().strftime("%Y-%m-%d")
        }},
        upsert=True
    )
    
    # Invalidate caches
    keys_to_delete = [k for k in LEADERBOARD_CACHE if k[0] == profile.userId]
    for k in keys_to_delete:
        LEADERBOARD_CACHE.pop(k, None)

    return {"message": "User registered successfully"}

@router.post("/points/update")
async def update_points(points_req: WorkoutPoints):
    user_id = points_req.userId
    steps = points_req.steps or 0

    # Invalidate cache for this user
    keys_to_delete = [k for k in LEADERBOARD_CACHE if k[0] == user_id]
    for k in keys_to_delete:
        LEADERBOARD_CACHE.pop(k, None)

    today_str = datetime.utcnow().strftime("%Y-%m-%d")
    stats = await db["leaderboard_stats"].find_one({"userId": user_id})
    if not stats:
        stats = {
            "userId": user_id,
            "stepsToday": 0,
            "lastStepsUpdate": today_str,
            "weeklySteps": 0,
            "allTimeSteps": 0,
            "workoutsThisWeek": 0,
            "allTimeWorkouts": 0
        }

    old_steps_today = stats.get("stepsToday", 0)
    last_update = stats.get("lastStepsUpdate", "")

    # Reset weekly steps if ISO week has changed
    try:
        if last_update:
            last_dt = datetime.strptime(last_update, "%Y-%m-%d")
            if last_dt.isocalendar()[1] != datetime.utcnow().isocalendar()[1]:
                stats["weeklySteps"] = 0
    except Exception:
        pass

    if last_update != today_str:
        # New day: accumulate full new steps
        diff = steps
        stats["weeklySteps"] += diff
        stats["allTimeSteps"] += diff
        stats["stepsToday"] = steps
        stats["lastStepsUpdate"] = today_str
    else:
        # Same day: accumulate delta
        diff = steps - old_steps_today
        if diff > 0:
            stats["weeklySteps"] += diff
            stats["allTimeSteps"] += diff
            stats["stepsToday"] = steps

    # Calculate actual workouts count
    all_time_workouts = await db["workouts"].count_documents({"deviceId": user_id})
    start_of_week = datetime.utcnow() - timedelta(days=7)
    weekly_workouts = await db["workouts"].count_documents({"deviceId": user_id, "createdAt": {"$gte": start_of_week}})

    stats["workoutsThisWeek"] = weekly_workouts
    stats["allTimeWorkouts"] = all_time_workouts

    await db["leaderboard_stats"].update_one(
        {"userId": user_id},
        {"$set": {
            "stepsToday": stats["stepsToday"],
            "lastStepsUpdate": stats["lastStepsUpdate"],
            "weeklySteps": stats["weeklySteps"],
            "allTimeSteps": stats["allTimeSteps"],
            "workoutsThisWeek": stats["workoutsThisWeek"],
            "allTimeWorkouts": stats["allTimeWorkouts"]
        }},
        upsert=True
    )

    return {"message": "Stats updated"}

@router.post("/add-friend")
async def add_friend(req: AddFriendRequest):
    friend_profile = await db["userprofiles"].find_one({"friendCode": req.friendCode})
    if not friend_profile:
        raise HTTPException(status_code=404, detail="Friend code not found")
        
    friend_id = friend_profile.get("userId") or friend_profile.get("deviceId")
    caller_id = req.userId

    if friend_id == caller_id:
        raise HTTPException(status_code=400, detail="Cannot add yourself")

    # Add bidirectional link in friends collection
    await db["friends"].update_one(
        {"userId": caller_id},
        {"$addToSet": {"friendIds": friend_id}},
        upsert=True
    )
    await db["friends"].update_one(
        {"userId": friend_id},
        {"$addToSet": {"friendIds": caller_id}},
        upsert=True
    )

    # Invalidate cache
    LEADERBOARD_CACHE.pop((caller_id, "weekly"), None)
    LEADERBOARD_CACHE.pop((caller_id, "all_time"), None)
    LEADERBOARD_CACHE.pop((friend_id, "weekly"), None)
    LEADERBOARD_CACHE.pop((friend_id, "all_time"), None)
    
    return {"message": "Friend added successfully"}

@router.get("/friends/{userId}", response_model=List[LeaderboardEntry])
async def get_friends_leaderboard(userId: str, period: str = "weekly"):
    period = period.lower()
    now = time.time()
    cache_key = (userId, period)

    if cache_key in LEADERBOARD_CACHE:
        cached_time, cached_data = LEADERBOARD_CACHE[cache_key]
        if now - cached_time < CACHE_TTL:
            return cached_data

    friends_doc = await db["friends"].find_one({"userId": userId})
    friend_ids = friends_doc["friendIds"] if friends_doc else []
    
    user_ids = [userId] + friend_ids
    
    # Query profiles while respecting privacy configurations
    profiles_cursor = db["userprofiles"].find({
        "$or": [
            {"userId": {"$in": user_ids}},
            {"deviceId": {"$in": user_ids}}
        ],
        "$or": [
            {"userId": userId},
            {"deviceId": userId},
            {"showOnLeaderboards": {"$ne": False}}
        ]
    })
    profiles = await profiles_cursor.to_list(length=None)

    # De-duplicate profiles
    profiles_dict = {}
    for p in profiles:
        uid = p.get("userId") or p.get("deviceId")
        if uid:
            profiles_dict[uid] = p

    # Fetch stats
    stats_cursor = db["leaderboard_stats"].find({"userId": {"$in": list(profiles_dict.keys())}})
    stats_list = await stats_cursor.to_list(length=None)
    stats_dict = {stat["userId"]: stat for stat in stats_list}

    entries = []
    for uid, p in profiles_dict.items():
        stat = stats_dict.get(uid, {})
        
        if period == "all_time":
            steps = stat.get("allTimeSteps", 0)
            workouts = stat.get("allTimeWorkouts", 0)
        else:
            steps = stat.get("weeklySteps", 0)
            workouts = stat.get("workoutsThisWeek", 0)

        # Computed leaderboard score
        score = int(steps * SCORE_STEP_WEIGHT + workouts * SCORE_WORKOUT_WEIGHT)

        entries.append(LeaderboardEntry(
            userId=uid,
            friendCode=p.get("friendCode", ""),
            displayName=p.get("name") or p.get("displayName", "User"),
            avatarInitials=p.get("name", "U")[0].upper() if p.get("name") else p.get("avatarInitials", "U"),
            weeklyPoints=score,
            workoutsThisWeek=workouts,
            currentStreak=p.get("currentStreak", 0),
            steps=steps
        ))

    # Sort descending by score
    entries.sort(key=lambda x: x.weeklyPoints, reverse=True)

    LEADERBOARD_CACHE[cache_key] = (now, entries)

    return entries

