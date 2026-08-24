import uuid
from typing import List, Dict, Any, Optional
from app.services.dataset_loader import get_cached_exercises


def _filter_exercises_by_criteria(
    catalog: List[Dict[str, Any]],
    body_parts: Optional[List[str]] = None,
    target_muscles: Optional[List[str]] = None,
    allowed_equipments: Optional[List[str]] = None,
    exclude_ids: Optional[List[str]] = None,
) -> List[Dict[str, Any]]:
    exclude_set = set(exclude_ids or [])
    results = []

    for ex in catalog:
        if ex.get("exerciseId") in exclude_set:
            continue

        # Check body parts
        if body_parts:
            ex_body = [b.lower() for b in ex.get("bodyParts", [])]
            if not any(bp.lower() in ex_body for bp in body_parts):
                continue

        # Check target muscles
        if target_muscles:
            ex_muscles = [m.lower() for m in ex.get("targetMuscles", [])] + [m.lower() for m in ex.get("secondaryMuscles", [])]
            if not any(tm.lower() in ex_muscles for tm in target_muscles):
                continue

        # Check equipments
        if allowed_equipments:
            ex_equip = [e.lower() for e in ex.get("equipments", [])]
            # If exercise equipment intersects with allowed equipment
            if not any(ae.lower() in ex_equip for ae in allowed_equipments):
                continue

        results.append(ex)

    return results


def _determine_split_structure(days_per_week: int, goal: str) -> List[Dict[str, Any]]:
    """Defines weekly split templates based on frequency (2 to 6 days)."""
    if days_per_week <= 2:
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Full Body Foundation (Push & Quad Focus)",
                "splitCategory": "Full Body",
                "bodyParts": ["chest", "shoulders", "upper legs", "lower legs", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "quadriceps", "triceps", "calves"],
                "focus": "Full Body Strength & Hypertrophy"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Full Body Posterior (Pull, Hams & Core)",
                "splitCategory": "Full Body",
                "bodyParts": ["back", "upper legs", "upper arms", "waist", "lower arms"],
                "targetMuscles": ["lats", "upper back", "glutes", "hamstrings", "biceps", "abs"],
                "focus": "Back, Posterior Chain & Arms"
            }
        ]
    elif days_per_week == 3:
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Full Body A (Chest, Upper Back & Quads)",
                "splitCategory": "Full Body",
                "bodyParts": ["chest", "back", "upper legs", "upper arms"],
                "targetMuscles": ["pectorals", "lats", "quadriceps", "triceps"],
                "focus": "Compound Push, Pull & Legs"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Full Body B (Shoulders, Hamstrings & Arms)",
                "splitCategory": "Full Body",
                "bodyParts": ["shoulders", "upper legs", "upper arms", "waist"],
                "targetMuscles": ["delts", "hamstrings", "glutes", "biceps", "abs"],
                "focus": "Shoulders, Posterior Chain & Arms"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Full Body C (Hypertrophy & Core Power)",
                "splitCategory": "Full Body",
                "bodyParts": ["chest", "back", "upper legs", "waist", "lower legs"],
                "targetMuscles": ["pectorals", "upper back", "quadriceps", "calves", "abs"],
                "focus": "Hypertrophy Density & Core"
            }
        ]
    elif days_per_week == 4:
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Upper Body Power (Chest & Back)",
                "splitCategory": "Upper",
                "bodyParts": ["chest", "back", "shoulders"],
                "targetMuscles": ["pectorals", "lats", "delts"],
                "focus": "Upper Body Heavy Compounds"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Lower Body & Core (Quads, Glutes & Abs)",
                "splitCategory": "Lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["quadriceps", "glutes", "hamstrings", "calves", "abs"],
                "focus": "Lower Body Foundation"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Upper Body Hypertrophy (Shoulders & Arms Focus)",
                "splitCategory": "Upper",
                "bodyParts": ["shoulders", "upper arms", "lower arms", "chest"],
                "targetMuscles": ["delts", "biceps", "triceps", "forearms", "pectorals"],
                "focus": "Upper Isolation & Arm Growth"
            },
            {
                "dayNumber": 4,
                "dayName": "Day 4: Lower Body Posterior & Calves",
                "splitCategory": "Lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["hamstrings", "glutes", "calves", "abs"],
                "focus": "Posterior Chain Volume"
            }
        ]
    elif days_per_week == 5:
        # 5-Day Push/Pull/Legs + Upper/Lower or PPL + Arms/Weak point
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Push (Chest, Front/Side Delts & Triceps)",
                "splitCategory": "Push",
                "bodyParts": ["chest", "shoulders", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "triceps"],
                "focus": "Horizontal & Incline Push Power"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Pull (Upper Back, Lats & Biceps)",
                "splitCategory": "Pull",
                "bodyParts": ["back", "upper arms", "lower arms"],
                "targetMuscles": ["lats", "upper back", "biceps", "forearms"],
                "focus": "Vertical & Horizontal Pulling"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Legs & Core (Quads, Hamstrings, Glutes & Calves)",
                "splitCategory": "Legs",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["quadriceps", "hamstrings", "glutes", "calves", "abs"],
                "focus": "Leg Hypertrophy & Volume"
            },
            {
                "dayNumber": 4,
                "dayName": "Day 4: Upper Body Hypertrophy (Shoulders, Chest & Arms)",
                "splitCategory": "Upper",
                "bodyParts": ["chest", "shoulders", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "biceps", "triceps"],
                "focus": "Upper Mass Builder"
            },
            {
                "dayNumber": 5,
                "dayName": "Day 5: Lower Body & Abs (Posterior Chain & Core)",
                "splitCategory": "Lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["hamstrings", "glutes", "calves", "abs"],
                "focus": "Posterior Chain & Core Conditioning"
            }
        ]
    else: # 6 days
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Push A (Chest Dominant & Triceps)",
                "splitCategory": "Push",
                "bodyParts": ["chest", "shoulders", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "triceps"],
                "focus": "Chest & Triceps Hypertrophy"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Pull A (Back Width & Biceps)",
                "splitCategory": "Pull",
                "bodyParts": ["back", "upper arms"],
                "targetMuscles": ["lats", "biceps", "forearms"],
                "focus": "Lats & Biceps Peak"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Legs A (Quad Dominant & Calves)",
                "splitCategory": "Legs",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["quadriceps", "calves", "abs"],
                "focus": "Quad Strength & Calves"
            },
            {
                "dayNumber": 4,
                "dayName": "Day 4: Push B (Shoulders & Upper Chest Focus)",
                "splitCategory": "Push",
                "bodyParts": ["shoulders", "chest", "upper arms"],
                "targetMuscles": ["delts", "pectorals", "triceps"],
                "focus": "Shoulder Caps & Incline Chest"
            },
            {
                "dayNumber": 5,
                "dayName": "Day 5: Pull B (Back Thickness & Arms)",
                "splitCategory": "Pull",
                "bodyParts": ["back", "upper arms"],
                "targetMuscles": ["upper back", "biceps"],
                "focus": "Back Thickness & Arm Density"
            },
            {
                "dayNumber": 6,
                "dayName": "Day 6: Legs B (Glutes, Hamstrings & Core)",
                "splitCategory": "Legs",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["hamstrings", "glutes", "calves", "abs"],
                "focus": "Posterior Chain & Core"
            }
        ]


def generate_personalized_workout_plan(
    device_id: str,
    weight_kg: float,
    height_cm: float,
    age: int = 24,
    gender: str = "male",
    fitness_goal: str = "bulk_up",
    days_per_week: int = 5,
    session_duration_minutes: int = 60,
    experience_level: str = "beginner",
    available_equipment: Optional[List[str]] = None,
    focus_muscles: Optional[List[str]] = None,
) -> Dict[str, Any]:
    """Generates a complete personalized weekly workout plan based on user biometrics and goals."""
    
    catalog = get_cached_exercises()
    days_per_week = max(2, min(6, days_per_week))
    session_duration_minutes = max(30, min(90, session_duration_minutes))

    # Goal specific configurations
    norm_goal = fitness_goal.lower().replace(" ", "_")
    if "bulk" in norm_goal or "muscle" in norm_goal or "hypertrophy" in norm_goal:
        goal_title = "Mass Builder & Hypertrophy"
        target_reps = "8-12 reps"
        default_sets = 4 if experience_level.lower() == "advanced" else 3
        rest_seconds = 90
        rep_duration_seconds = 45  # 45s set time + 90s rest = 135s per set (~2.25 mins)
        caloric_guidance = "+300 to +500 kcal surplus (Eat 1.6-2.2g protein per kg of bodyweight)"
        nutrition_tip = f"At {weight_kg:.1f}kg, target {int(weight_kg * 2.0)}g of protein daily. Prioritize progressive overload on compound lifts."
    elif "cut" in norm_goal or "loss" in norm_goal:
        goal_title = "Fat Loss & Metabolic Conditioning"
        target_reps = "12-15 reps"
        default_sets = 3
        rest_seconds = 60
        rep_duration_seconds = 40  # 40s set + 60s rest = 100s per set (~1.6 mins)
        caloric_guidance = "-300 to -500 kcal deficit (High protein retention)"
        nutrition_tip = "Maintain high protein intake and keep rest times tight to maximize caloric burn and muscle preservation."
    elif "strength" in norm_goal:
        goal_title = "Pure Strength & Neural Drive"
        target_reps = "4-6 reps"
        default_sets = 4
        rest_seconds = 150
        rep_duration_seconds = 30  # 30s set + 150s rest = 180s per set (3.0 mins)
        caloric_guidance = "Maintenance to slight surplus (+200 kcal)"
        nutrition_tip = "Focus on 3-5 minute rest intervals on your primary heavy compound movements for maximum neurological recovery."
    else:
        goal_title = "Balanced Fitness & Longevity"
        target_reps = "10-12 reps"
        default_sets = 3
        rest_seconds = 60
        rep_duration_seconds = 40
        caloric_guidance = "Caloric Maintenance"
        nutrition_tip = "Stay consistent with daily hydration and prioritize good movement form."

    # Compute target number of exercises based on session duration budget
    time_per_exercise_mins = (default_sets * (rep_duration_seconds + rest_seconds)) / 60.0
    warmup_cooldown_mins = 6.0
    available_exercise_time = max(20.0, session_duration_minutes - warmup_cooldown_mins)
    target_exercise_count = max(3, min(7, int(available_exercise_time // time_per_exercise_mins)))

    split_templates = _determine_split_structure(days_per_week, norm_goal)
    daily_routines = []
    used_exercise_ids = []

    for template in split_templates:
        day_body_parts = template["bodyParts"]
        day_target_muscles = template["targetMuscles"]

        # Prioritize matching exercises
        matched_pool = _filter_exercises_by_criteria(
            catalog=catalog,
            body_parts=day_body_parts,
            target_muscles=day_target_muscles,
            allowed_equipments=available_equipment,
            exclude_ids=used_exercise_ids
        )

        # Fallback if filtered pool is too small
        if len(matched_pool) < target_exercise_count:
            matched_pool += _filter_exercises_by_criteria(
                catalog=catalog,
                body_parts=day_body_parts,
                allowed_equipments=available_equipment,
                exclude_ids=used_exercise_ids
            )

        if len(matched_pool) < target_exercise_count:
            # Add general exercises from catalog without equipment restriction if needed
            matched_pool += [ex for ex in catalog if ex.get("exerciseId") not in used_exercise_ids]

        selected_for_day = matched_pool[:target_exercise_count]
        for ex in selected_for_day:
            used_exercise_ids.append(ex.get("exerciseId"))

        generated_exercises = []
        for ex in selected_for_day:
            gif_filename = ex.get("gifUrl", "")
            if gif_filename and not gif_filename.startswith("http"):
                gif_url = f"/static/exercise-gifs/{gif_filename}"
            else:
                gif_url = gif_filename

            # Calculate estimated exercise time
            exercise_duration = round((default_sets * (rep_duration_seconds + rest_seconds)) / 60.0, 1)

            generated_exercises.append({
                "exerciseId": ex.get("exerciseId", str(uuid.uuid4())[:8]),
                "name": ex.get("name", "Exercise").title(),
                "targetMuscles": ex.get("targetMuscles", []),
                "bodyParts": ex.get("bodyParts", []),
                "equipments": ex.get("equipments", ["body weight"]),
                "secondaryMuscles": ex.get("secondaryMuscles", []),
                "instructions": ex.get("instructions", []),
                "gifUrl": gif_url,
                "targetSets": default_sets,
                "targetReps": target_reps,
                "suggestedWeightKg": None,
                "restSeconds": rest_seconds,
                "estimatedMinutes": exercise_duration
            })

        total_day_minutes = int(sum(e["estimatedMinutes"] for e in generated_exercises) + warmup_cooldown_mins)

        daily_routines.append({
            "dayNumber": template["dayNumber"],
            "dayName": template["dayName"],
            "splitCategory": template["splitCategory"],
            "isRestDay": False,
            "targetFocus": template["focus"],
            "estimatedDurationMinutes": total_day_minutes,
            "exercises": generated_exercises
        })

    # Add rest days up to 7 days in the weekly overview
    current_day = len(daily_routines) + 1
    while current_day <= 7:
        daily_routines.append({
            "dayNumber": current_day,
            "dayName": f"Day {current_day}: Active Recovery & Mobility",
            "splitCategory": "Rest & Recovery",
            "isRestDay": True,
            "targetFocus": "Light stretching, walking, hydration and muscle recovery",
            "estimatedDurationMinutes": 20,
            "exercises": []
        })
        current_day += 1

    weekly_volume_score = sum(
        len(routine["exercises"]) * default_sets
        for routine in daily_routines if not routine["isRestDay"]
    )

    plan_title = f"{days_per_week}-Day {goal_title} Split"

    return {
        "planId": str(uuid.uuid4()),
        "title": plan_title,
        "description": f"Scientifically optimized {days_per_week}-day routine for {experience_level.title()} lifters aiming for {goal_title.lower()}. Calibrated for a {session_duration_minutes}-minute session time limit.",
        "goal": fitness_goal,
        "daysPerWeek": days_per_week,
        "sessionDurationMinutes": session_duration_minutes,
        "experienceLevel": experience_level,
        "weeklyVolumeScore": float(weekly_volume_score),
        "dailyRoutines": daily_routines,
        "recommendedCaloricSurplusOrDeficit": caloric_guidance,
        "nutritionTip": nutrition_tip
    }
