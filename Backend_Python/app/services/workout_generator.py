import uuid
import re
from typing import List, Dict, Any, Optional
from app.services.dataset_loader import get_cached_exercises

# --- Injury Exclusion Rules ---
INJURY_EXCLUSION_KEYWORDS: Dict[str, List[str]] = {
    "shoulder": ["behind neck", "upright row", "deep dip", "military press behind", "snatch"],
    "knee": ["sissy squat", "hack squat", "jump lunge", "box jump", "deep pistol squat"],
    "lower_back": ["good morning", "stiff leg deadlift", "heavy deadlift", "bent over barbell row", "jefferson curl"],
    "wrist": ["straight bar curl", "heavy clean and press", "reverse barbell curl"],
    "elbow": ["skull crusher", "close grip bench press", "french press"],
    "ankle": ["depth jump", "box jump", "heavy calf jump"],
    "neck": ["behind neck", "neck bridge", "wrestler bridge"]
}

# --- Standard Warmup & Cooldown Protocols ---
WARMUP_PROTOCOLS = {
    "upper": ["Arm circles (30s forward, 30s backward)", "Band pull-aparts / Scapular retractions (15 reps)", "Thoracic rotations (10 each side)", "Light progressive warmup set on primary compound"],
    "lower": ["Hip 90/90 mobility drill (10 reps)", "Bodyweight air squats with pause (12 reps)", "Leg swings front/back & side/side (10 each)", "Glute bridges (15 reps)"],
    "full_body": ["Jumping jacks / Light jump rope (2 mins)", "World's greatest stretch (5 each side)", "Deep squat to stand (8 reps)", "Inchworm with pushup (6 reps)"]
}

COOLDOWN_PROTOCOLS = [
    "Diaphragmatic box breathing (2 minutes)",
    "Pigeon pose / Glute stretch (45s each side)",
    "Doorway chest & shoulder stretch (45s each side)",
    "Hamstring & calf static stretch (45s each side)"
]


def _filter_exercises_by_criteria(
    catalog: List[Dict[str, Any]],
    body_parts: Optional[List[str]] = None,
    target_muscles: Optional[List[str]] = None,
    allowed_equipments: Optional[List[str]] = None,
    exclude_ids: Optional[List[str]] = None,
    disliked_names: Optional[List[str]] = None,
    physical_limitations: Optional[List[str]] = None
) -> List[Dict[str, Any]]:
    exclude_set = set(exclude_ids or [])
    disliked_set = {d.lower().strip() for d in (disliked_names or []) if d}
    limitations = [lim.lower().strip() for lim in (physical_limitations or []) if lim and lim != "none"]

    # Gather keywords to exclude based on injuries
    injury_keywords = []
    for lim in limitations:
        for key, kw_list in INJURY_EXCLUSION_KEYWORDS.items():
            if key in lim:
                injury_keywords.extend(kw_list)

    results = []

    for ex in catalog:
        ex_id = ex.get("exerciseId")
        ex_name = ex.get("name", "").lower()

        if ex_id in exclude_set:
            continue

        # Check disliked exercises
        if any(dis in ex_name for dis in disliked_set):
            continue

        # Check injury exclusions
        if any(ik in ex_name for ik in injury_keywords):
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
            # Normalizing equipment terms (e.g. "dumbbell" vs "dumbbells")
            norm_allowed = [ae.lower().rstrip("s") for ae in allowed_equipments]
            if not any(any(na in ee for na in norm_allowed) for ee in ex_equip):
                continue

        results.append(ex)

    return results


def _determine_split_structure(days_per_week: int, goal: str, focus_muscles: Optional[List[str]] = None) -> List[Dict[str, Any]]:
    """Defines weekly split templates based on frequency (2 to 6 days) and prioritizes focus muscle groups."""
    focus = [f.lower() for f in (focus_muscles or [])]

    if days_per_week <= 2:
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Full Body Foundation (Push & Quad Focus)",
                "splitCategory": "Full Body",
                "warmupType": "full_body",
                "bodyParts": ["chest", "shoulders", "upper legs", "lower legs", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "quadriceps", "triceps", "calves"],
                "focus": "Full Body Strength & Hypertrophy"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Full Body Posterior (Pull, Hams & Core)",
                "splitCategory": "Full Body",
                "warmupType": "full_body",
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
                "warmupType": "full_body",
                "bodyParts": ["chest", "back", "upper legs", "upper arms"],
                "targetMuscles": ["pectorals", "lats", "quadriceps", "triceps"],
                "focus": "Compound Push, Pull & Legs"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Full Body B (Shoulders, Hamstrings & Arms)",
                "splitCategory": "Full Body",
                "warmupType": "full_body",
                "bodyParts": ["shoulders", "upper legs", "upper arms", "waist"],
                "targetMuscles": ["delts", "hamstrings", "glutes", "biceps", "abs"],
                "focus": "Shoulders, Posterior Chain & Arms"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Full Body C (Hypertrophy & Core Power)",
                "splitCategory": "Full Body",
                "warmupType": "full_body",
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
                "warmupType": "upper",
                "bodyParts": ["chest", "back", "shoulders"],
                "targetMuscles": ["pectorals", "lats", "delts"],
                "focus": "Upper Body Heavy Compounds"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Lower Body & Core (Quads, Glutes & Abs)",
                "splitCategory": "Lower",
                "warmupType": "lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["quadriceps", "glutes", "hamstrings", "calves", "abs"],
                "focus": "Lower Body Foundation"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Upper Body Hypertrophy (Shoulders & Arms Focus)",
                "splitCategory": "Upper",
                "warmupType": "upper",
                "bodyParts": ["shoulders", "upper arms", "lower arms", "chest"],
                "targetMuscles": ["delts", "biceps", "triceps", "forearms", "pectorals"],
                "focus": "Upper Isolation & Arm Growth"
            },
            {
                "dayNumber": 4,
                "dayName": "Day 4: Lower Body Posterior & Calves",
                "splitCategory": "Lower",
                "warmupType": "lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["hamstrings", "glutes", "calves", "abs"],
                "focus": "Posterior Chain Volume"
            }
        ]
    elif days_per_week == 5:
        # 5-Day Push/Pull/Legs + Upper/Lower
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Push (Chest, Front/Side Delts & Triceps)",
                "splitCategory": "Push",
                "warmupType": "upper",
                "bodyParts": ["chest", "shoulders", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "triceps"],
                "focus": "Horizontal & Incline Push Power"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Pull (Upper Back, Lats & Biceps)",
                "splitCategory": "Pull",
                "warmupType": "upper",
                "bodyParts": ["back", "upper arms", "lower arms"],
                "targetMuscles": ["lats", "upper back", "biceps", "forearms"],
                "focus": "Vertical & Horizontal Pulling"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Legs & Core (Quads, Hamstrings & Glutes)",
                "splitCategory": "Legs",
                "warmupType": "lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["quadriceps", "hamstrings", "glutes", "calves", "abs"],
                "focus": "Leg Hypertrophy & Volume"
            },
            {
                "dayNumber": 4,
                "dayName": "Day 4: Upper Body Hypertrophy (Shoulders & Arms)",
                "splitCategory": "Upper",
                "warmupType": "upper",
                "bodyParts": ["chest", "shoulders", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "biceps", "triceps"],
                "focus": "Upper Mass Builder"
            },
            {
                "dayNumber": 5,
                "dayName": "Day 5: Lower Body & Abs (Posterior Chain Focus)",
                "splitCategory": "Lower",
                "warmupType": "lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["hamstrings", "glutes", "calves", "abs"],
                "focus": "Posterior Chain & Core Conditioning"
            }
        ]
    else: # 6 days Push/Pull/Legs x2
        return [
            {
                "dayNumber": 1,
                "dayName": "Day 1: Push A (Chest Dominant & Triceps)",
                "splitCategory": "Push",
                "warmupType": "upper",
                "bodyParts": ["chest", "shoulders", "upper arms"],
                "targetMuscles": ["pectorals", "delts", "triceps"],
                "focus": "Chest & Triceps Hypertrophy"
            },
            {
                "dayNumber": 2,
                "dayName": "Day 2: Pull A (Back Width & Biceps)",
                "splitCategory": "Pull",
                "warmupType": "upper",
                "bodyParts": ["back", "upper arms"],
                "targetMuscles": ["lats", "biceps", "forearms"],
                "focus": "Lats & Biceps Peak"
            },
            {
                "dayNumber": 3,
                "dayName": "Day 3: Legs A (Quad Dominant & Calves)",
                "splitCategory": "Legs",
                "warmupType": "lower",
                "bodyParts": ["upper legs", "lower legs", "waist"],
                "targetMuscles": ["quadriceps", "calves", "abs"],
                "focus": "Quad Strength & Calves"
            },
            {
                "dayNumber": 4,
                "dayName": "Day 4: Push B (Shoulders & Upper Chest Focus)",
                "splitCategory": "Push",
                "warmupType": "upper",
                "bodyParts": ["shoulders", "chest", "upper arms"],
                "targetMuscles": ["delts", "pectorals", "triceps"],
                "focus": "Shoulder Caps & Incline Chest"
            },
            {
                "dayNumber": 5,
                "dayName": "Day 5: Pull B (Back Thickness & Arms)",
                "splitCategory": "Pull",
                "warmupType": "upper",
                "bodyParts": ["back", "upper arms"],
                "targetMuscles": ["upper back", "biceps"],
                "focus": "Back Thickness & Arm Density"
            },
            {
                "dayNumber": 6,
                "dayName": "Day 6: Legs B (Glutes, Hamstrings & Core)",
                "splitCategory": "Legs",
                "warmupType": "lower",
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
    days_per_week: int = 4,
    session_duration_minutes: int = 60,
    experience_level: str = "beginner",
    available_equipment: Optional[List[str]] = None,
    goal_priority: Optional[str] = "balanced",
    focus_muscles: Optional[List[str]] = None,
    avoid_muscles: Optional[List[str]] = None,
    preferred_exercises: Optional[List[str]] = None,
    disliked_exercises: Optional[List[str]] = None,
    physical_limitations: Optional[List[str]] = None,
    training_style: Optional[str] = "bodybuilding",
    intensity_preference: Optional[str] = "moderate",
    sleep_hours: Optional[str] = "7_8h",
    stress_level: Optional[str] = "moderate",
    training_location: Optional[str] = "commercial_gym",
    warmup_included: bool = True,
    progression_model: Optional[str] = "progressive_overload",
) -> Dict[str, Any]:
    """Generates an intelligent, multi-dimensional weekly workout routine factoring in biometrics, recovery, injuries, and preferences."""

    catalog = get_cached_exercises()
    days_per_week = max(2, min(6, days_per_week))
    session_duration_minutes = max(20, min(120, session_duration_minutes))

    # Normalize goal
    norm_goal = fitness_goal.lower().replace(" ", "_")
    training_style_norm = (training_style or "bodybuilding").lower()

    # Determine rep ranges, sets, rest, and RIR based on style and goal
    if "strength" in norm_goal or "strength" in training_style_norm:
        goal_title = "Maximum Strength & Neurological Power"
        target_reps = "4-6 reps"
        default_sets = 4 if experience_level.lower() == "advanced" else 3
        rest_seconds = 150
        rep_duration_seconds = 30
        rir_target = 2
        caloric_guidance = "Maintenance to slight surplus (+200 kcal)"
        nutrition_tip = "Prioritize complex carbs 2 hours before heavy training and 3-5 minute recovery on primary compound lifts."
    elif "cut" in norm_goal or "fat" in norm_goal or "loss" in norm_goal or "circuit" in training_style_norm:
        goal_title = "Fat Loss & Metabolic Density"
        target_reps = "12-15 reps"
        default_sets = 3
        rest_seconds = 60
        rep_duration_seconds = 40
        rir_target = 1
        caloric_guidance = "-300 to -500 kcal deficit (High protein retention)"
        nutrition_tip = f"Target {round(weight_kg * 2.2, 1)}g of protein daily to protect lean mass during your caloric deficit."
    elif "recomposition" in norm_goal:
        goal_title = "Body Recomposition & Lean Muscle"
        target_reps = "8-12 reps"
        default_sets = 3
        rest_seconds = 75
        rep_duration_seconds = 40
        rir_target = 2
        caloric_guidance = "Caloric Maintenance with high protein (+2.0g/kg)"
        nutrition_tip = "Eat at maintenance calories while progressively overloading compound lifts."
    else:  # Hypertrophy / Muscle Gain default
        goal_title = "Mass Builder & Hypertrophy"
        target_reps = "8-12 reps"
        default_sets = 4 if experience_level.lower() in ("intermediate", "advanced") else 3
        rest_seconds = 90
        rep_duration_seconds = 45
        rir_target = 2
        caloric_guidance = "+300 to +500 kcal surplus (Hypertrophy fueling)"
        nutrition_tip = f"At {weight_kg:.1f}kg, aim for {int(weight_kg * 2.0)}g of protein and consistent hydration for optimal muscle repair."

    # Factor in recovery & stress: adjust volume if high fatigue
    recovery_advisory = "Recovery capacity is optimal. Full training volume allocated."
    if sleep_hours in ("under_5h", "5_6h") and stress_level == "high":
        default_sets = max(2, default_sets - 1)
        recovery_advisory = "Low sleep & elevated stress detected: Total set volume adjusted down by 20% to prevent systemic overtraining and optimize CNS recovery."

    # Calculate time budget
    warmup_mins = 6 if warmup_included else 0
    cooldown_mins = 4 if warmup_included else 0
    available_exercise_time = max(15.0, session_duration_minutes - (warmup_mins + cooldown_mins))
    time_per_exercise_mins = (default_sets * (rep_duration_seconds + rest_seconds)) / 60.0
    target_exercise_count = max(3, min(7, int(available_exercise_time // time_per_exercise_mins)))

    split_templates = _determine_split_structure(days_per_week, norm_goal, focus_muscles)
    daily_routines = []
    used_exercise_ids = []

    preferred_set = {p.lower().strip() for p in (preferred_exercises or []) if p}

    for template in split_templates:
        day_body_parts = template["bodyParts"]
        day_target_muscles = template["targetMuscles"]

        # 1. First pool: Target matching criteria with injury & dislike filters
        matched_pool = _filter_exercises_by_criteria(
            catalog=catalog,
            body_parts=day_body_parts,
            target_muscles=day_target_muscles,
            allowed_equipments=available_equipment,
            exclude_ids=used_exercise_ids,
            disliked_names=disliked_exercises,
            physical_limitations=physical_limitations
        )

        # Sort preferred exercises to the top
        matched_pool.sort(key=lambda ex: any(p in ex.get("name", "").lower() for p in preferred_set), reverse=True)

        # Deduplicate pool
        seen_ids = set()
        deduped_pool = []
        for ex in matched_pool:
            eid = ex.get("exerciseId")
            if eid and eid not in seen_ids:
                seen_ids.add(eid)
                deduped_pool.append(ex)

        # Fallback if filtered pool is too small
        if len(deduped_pool) < target_exercise_count:
            fallback_pool = _filter_exercises_by_criteria(
                catalog=catalog,
                body_parts=day_body_parts,
                allowed_equipments=available_equipment,
                exclude_ids=list(seen_ids),
                disliked_names=disliked_exercises,
                physical_limitations=physical_limitations
            )
            for ex in fallback_pool:
                eid = ex.get("exerciseId")
                if eid and eid not in seen_ids:
                    seen_ids.add(eid)
                    deduped_pool.append(ex)

        if len(deduped_pool) < target_exercise_count:
            # Broaden without restriction if pool still dry
            for ex in catalog:
                eid = ex.get("exerciseId")
                if eid and eid not in seen_ids and not any(ik in ex.get("name", "").lower() for ik in INJURY_EXCLUSION_KEYWORDS.get("shoulder", [])):
                    seen_ids.add(eid)
                    deduped_pool.append(ex)

        selected_for_day = deduped_pool[:target_exercise_count]
        for ex in selected_for_day:
            used_exercise_ids.append(ex.get("exerciseId"))

        generated_exercises = []
        for ex in selected_for_day:
            gif_filename = ex.get("gifUrl", "")
            if gif_filename and not gif_filename.startswith("http"):
                gif_url = f"/static/exercise-gifs/{gif_filename}"
            else:
                gif_url = gif_filename

            # Calculate sets: give +1 extra set for focus muscles
            ex_muscles = [m.lower() for m in ex.get("targetMuscles", [])]
            is_focus = any(any(fm.lower() in m for fm in (focus_muscles or [])) for m in ex_muscles)
            is_avoid = any(any(am.lower() in m for am in (avoid_muscles or [])) for m in ex_muscles)

            ex_sets = default_sets
            if is_focus:
                ex_sets = min(5, ex_sets + 1)
            elif is_avoid:
                ex_sets = max(2, ex_sets - 1)

            exercise_duration = round((ex_sets * (rep_duration_seconds + rest_seconds)) / 60.0, 1)

            # Progression Protocol rule
            if progression_model == "reps_first":
                progression_rule = f"Aim for top rep target ({target_reps.split('-')[-1].strip()}) on all sets. When achieved, increase weight by 2.5kg."
            elif progression_model == "weight_first":
                progression_rule = "Maintain base reps and prioritize adding 1.25-2.5kg per week when RIR ≥ 2."
            else:
                progression_rule = "Double progression: hit upper rep ceiling with RIR 2 across all sets before adding load."

            generated_exercises.append({
                "exerciseId": ex.get("exerciseId", str(uuid.uuid4())[:8]),
                "name": ex.get("name", "Exercise").title(),
                "targetMuscles": ex.get("targetMuscles", []),
                "bodyParts": ex.get("bodyParts", []),
                "equipments": ex.get("equipments", ["body weight"]),
                "secondaryMuscles": ex.get("secondaryMuscles", []),
                "instructions": ex.get("instructions", []),
                "gifUrl": gif_url,
                "targetSets": ex_sets,
                "targetReps": target_reps,
                "suggestedWeightKg": None,
                "restSeconds": rest_seconds,
                "estimatedMinutes": exercise_duration,
                "rirTarget": rir_target,
                "progressionProtocol": progression_rule
            })

        total_day_minutes = int(sum(e["estimatedMinutes"] for e in generated_exercises) + warmup_mins + cooldown_mins)
        warmup_key = template.get("warmupType", "full_body")
        warmup_list = WARMUP_PROTOCOLS.get(warmup_key, WARMUP_PROTOCOLS["full_body"]) if warmup_included else []
        cooldown_list = COOLDOWN_PROTOCOLS if warmup_included else []

        daily_routines.append({
            "dayNumber": template["dayNumber"],
            "dayName": template["dayName"],
            "splitCategory": template["splitCategory"],
            "isRestDay": False,
            "targetFocus": template["focus"],
            "estimatedDurationMinutes": total_day_minutes,
            "warmupMinutes": warmup_mins,
            "warmupNotes": warmup_list,
            "cooldownMinutes": cooldown_mins,
            "cooldownNotes": cooldown_list,
            "exercises": generated_exercises
        })

    # Add recovery rest days up to 7 days
    current_day = len(daily_routines) + 1
    while current_day <= 7:
        daily_routines.append({
            "dayNumber": current_day,
            "dayName": f"Day {current_day}: Active Recovery & Mobility",
            "splitCategory": "Rest & Recovery",
            "isRestDay": True,
            "targetFocus": "Light walking, 10-min mobility, hydration, and sleep prioritization",
            "estimatedDurationMinutes": 20,
            "warmupMinutes": 0,
            "warmupNotes": [],
            "cooldownMinutes": 0,
            "cooldownNotes": [],
            "exercises": []
        })
        current_day += 1

    weekly_volume_score = sum(
        sum(ex["targetSets"] for ex in routine["exercises"])
        for routine in daily_routines if not routine["isRestDay"]
    )

    plan_title = f"{days_per_week}-Day {goal_title} Split"

    # Injury safety notes
    safety_notes = []
    if physical_limitations:
        for lim in physical_limitations:
            if lim and lim != "none":
                safety_notes.append(f"Excluded contraindicated movements for {lim.replace('_', ' ').title()} protection.")

    return {
        "planId": str(uuid.uuid4()),
        "title": plan_title,
        "description": f"AI-optimized {days_per_week}-day routine for {experience_level.title()} lifters aiming for {goal_title.lower()}. Tuned for {session_duration_minutes}-min sessions with {progression_model.replace('_', ' ').title()} overload protocol.",
        "goal": fitness_goal,
        "daysPerWeek": days_per_week,
        "sessionDurationMinutes": session_duration_minutes,
        "experienceLevel": experience_level,
        "weeklyVolumeScore": float(weekly_volume_score),
        "dailyRoutines": daily_routines,
        "recommendedCaloricSurplusOrDeficit": caloric_guidance,
        "nutritionTip": nutrition_tip,
        "progressionOverview": f"Target RIR {rir_target} on working sets. Apply {progression_model.replace('_', ' ').title()} weekly.",
        "injurySafetyNotes": safety_notes,
        "recoveryAdvisory": recovery_advisory
    }
