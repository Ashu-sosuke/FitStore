import logging
from typing import List, Optional
from datetime import datetime, timezone
from fastapi import APIRouter, Depends, HTTPException, Query, Security
from pydantic import BaseModel, Field
from app.services.nutrition_engine import nutrition_engine, NutritionResult
from app.security import verify_jwt
from app.database import db

logger = logging.getLogger(__name__)

router = APIRouter()

@router.get("/", response_model=NutritionResult)
async def get_food_nutrition(
    food: str = Query(..., min_length=1, description="Food name, alias, or Indian dish query"),
    grams: float = Query(default=100.0, ge=1.0, le=5000.0, description="Portion weight in grams"),
    user_id: str = Depends(verify_jwt)
):
    """
    Get scientifically accurate macronutrient breakdown from IFCT 2017 (ICMR-NIN) 
    and composite Indian dish recipes.
    """
    result = nutrition_engine.get_nutrition(food_query=food, grams=grams)
    return result

class PlannedMealItem(BaseModel):
    mealType: str          # Breakfast, Lunch, Evening Snack, Dinner
    title: str             # e.g., "Power Egg Omelette & Multigrain Toast"
    foodDescription: str   # Specific ingredients and gram portions
    portionGrams: float    # Total portion weight in grams
    calories: float
    proteinG: float
    carbsG: float
    fatsG: float
    nutritionistTip: str   # Certified nutritionist pro-tip

class PersonalizedMealPlanRequest(BaseModel):
    deviceId: str
    weightKg: float
    heightCm: float
    age: int
    gender: str = "male"
    fitnessGoal: str = "muscle_gain"  # muscle_gain, weight_loss, maintenance, strength, recomposition
    activityLevel: str = "moderately_active"
    dietPreference: str = "non_veg"   # vegetarian, vegan, non_veg, eggetarian
    mealFrequency: int = 4            # 2, 3, 4, 5
    nutritionPriority: str = "high_protein"

class PersonalizedMealPlanResponse(BaseModel):
    title: str
    targetCalories: float
    proteinTargetG: float
    carbsTargetG: float
    fatsTargetG: float
    waterTargetLiters: float
    dietSummary: str
    nutritionistNotes: str
    meals: List[PlannedMealItem]


def generate_nutritionist_plan(req: PersonalizedMealPlanRequest) -> PersonalizedMealPlanResponse:
    w = max(req.weightKg, 35.0)
    h = max(req.heightCm, 120.0)
    a = max(req.age, 14)
    gender_lower = req.gender.lower()

    # 1. Mifflin-St Jeor BMR
    if "female" in gender_lower or "woman" in gender_lower:
        bmr = (10 * w) + (6.25 * h) - (5 * a) - 161
    else:
        bmr = (10 * w) + (6.25 * h) - (5 * a) + 5

    # 2. Activity multiplier
    act = req.activityLevel.lower()
    if "sedentary" in act:
        multiplier = 1.2
    elif "light" in act:
        multiplier = 1.375
    elif "very" in act:
        multiplier = 1.725
    elif "extra" in act:
        multiplier = 1.9
    else:
        multiplier = 1.55
    tdee = bmr * multiplier

    # 3. Goal adjustment
    goal = req.fitnessGoal.lower()
    if "loss" in goal or "cut" in goal or "fat" in goal:
        target_cal = round(tdee - 450)
        protein_ratio = 0.35
        carbs_ratio = 0.35
        fats_ratio = 0.30
        goal_title = "Fat Loss & Muscle Preservation Plan"
        summary = f"Caloric deficit plan aimed at dropping body fat while protecting lean muscle mass. Daily target: {target_cal} kcal."
    elif "gain" in goal or "bulk" in goal or "muscle" in goal:
        target_cal = round(tdee + 350)
        protein_ratio = 0.30
        carbs_ratio = 0.45
        fats_ratio = 0.25
        goal_title = "Lean Muscle Hypertrophy Fuel Plan"
        summary = f"Structured caloric surplus designed to maximize hypertrophy and strength recovery without excessive fat gain. Daily target: {target_cal} kcal."
    elif "strength" in goal:
        target_cal = round(tdee + 200)
        protein_ratio = 0.30
        carbs_ratio = 0.45
        fats_ratio = 0.25
        goal_title = "Power & Peak Strength Fuel Plan"
        summary = f"Nutrient-dense plan prioritized for central nervous system and muscular power recovery. Daily target: {target_cal} kcal."
    else:
        target_cal = round(tdee)
        protein_ratio = 0.25
        carbs_ratio = 0.50
        fats_ratio = 0.25
        goal_title = "Metabolic Maintenance & Vitality Plan"
        summary = f"Isocaloric balanced nutrition designed for sustained energy, gut health, and daily vitality. Daily target: {target_cal} kcal."

    target_cal = max(target_cal, 1200)
    p_target = round((target_cal * protein_ratio) / 4.0)
    c_target = round((target_cal * carbs_ratio) / 4.0)
    f_target = round((target_cal * fats_ratio) / 9.0)
    water_liters = round(max(w * 0.04, 2.5), 1)

    # 4. Meal Split Ratios
    freq = max(min(req.mealFrequency, 5), 3)
    if freq == 3:
        splits = [
            ("Breakfast", 0.30, "Energizing Breakfast"),
            ("Lunch", 0.40, "Core Power Lunch"),
            ("Dinner", 0.30, "Recovery & Lean Dinner")
        ]
    elif freq == 5:
        splits = [
            ("Breakfast", 0.25, "Metabolic Kickstart Breakfast"),
            ("Mid-Morning Snack", 0.10, "Brain & Vitality Snack"),
            ("Lunch", 0.30, "Balanced Power Lunch"),
            ("Evening Snack", 0.15, "Pre/Post Training Fuel"),
            ("Dinner", 0.20, "Overnight Repair Dinner")
        ]
    else: # 4 meals default
        splits = [
            ("Breakfast", 0.25, "Power Protein Breakfast"),
            ("Lunch", 0.35, "Core Performance Lunch"),
            ("Evening Snack", 0.15, "Metabolic Booster Snack"),
            ("Dinner", 0.25, "Night Recovery Dinner")
        ]

    # 5. Food Templates by Diet Preference
    diet = req.dietPreference.lower()
    is_veg = "veg" in diet and "non" not in diet and "egg" not in diet
    is_vegan = "vegan" in diet
    is_egg = "egg" in diet

    meal_catalog = {
        "non_veg": {
            "Breakfast": ("3 Egg White + 1 Whole Egg Omelette with 2 Multigrain Toasts & Spinach", 280, "Take with 250ml water. High biological value protein to stimulate muscle protein synthesis early."),
            "Mid-Morning Snack": ("Greek Yogurt (150g) with 10 Crushed Almonds & Fresh Berries", 180, "Rich in slow-release casein and healthy monounsaturated fats."),
            "Lunch": ("Grilled Chicken Breast (160g) with Steamed Brown Rice (150g), Yellow Dal & Green Salad", 450, "Balanced carb-protein combo replenishing glycogen stores without causing a post-meal crash."),
            "Evening Snack": ("Whey Protein Isolate (30g) in Water with 1 Medium Banana", 180, "Rapid leucine absorption to accelerate recovery window."),
            "Dinner": ("Herb Baked Fish / Chicken Tikka (150g) with 2 Soft Phulkas, Sautéed Veggies & Lentil Soup", 380, "Lean protein with micronutrient-dense vegetables for restorative overnight cellular repair.")
        },
        "vegetarian": {
            "Breakfast": ("Sprouted Moong & Paneer Chilla (140g) with Mint Chutney & 1 Cup Warm Skim Milk", 290, "Complete amino acid profile combining legumes and dairy curd/paneer."),
            "Mid-Morning Snack": ("Roasted Makhana (40g) & Walnuts (15g)", 120, "Rich in magnesium and plant polyphenols for sustained mental focus."),
            "Lunch": ("Paneer Bhurji (120g) with Dal Tadka (150g), 2 Whole Wheat Phulkas & Cucumber Raita", 460, "Provides 30g+ plant and dairy protein with low GI dietary fiber for gut health."),
            "Evening Snack": ("Sattu Protein Drink (40g) with Lemon & Roasted Chana (30g)", 180, "Traditional high-fiber superfood providing clean plant protein and essential electrolytes."),
            "Dinner": ("Palak Paneer (180g) with 1 Jowar/Wheat Roti & Warm Moong Soup", 360, "High bioavailable calcium and iron with gentle evening digestion.")
        },
        "eggetarian": {
            "Breakfast": ("Masala Scrambled Eggs (3 Eggs) with 2 Multigrain Slices & Sautéed Tomatoes", 290, "Rich in choline and complete essential amino acids for optimal metabolic drive."),
            "Mid-Morning Snack": ("Mixed Seed Bowl (Pumpkin, Chia, Sunflower) with 1 Fresh Apple", 150, "Healthy fats and pectin fiber maintaining steady insulin levels."),
            "Lunch": ("Egg Curry (2 Boiled Eggs) with Brown Rice (150g), Toor Dal & Crunchy Salad", 450, "High-satiety plate combining quality egg protein, complex starches, and lentils."),
            "Evening Snack": ("2 Boiled Egg Whites with Hummus & Carrot Sticks", 160, "Low calorie, high protein snack perfect before your evening training session."),
            "Dinner": ("Paneer & Veggie Skewers (140g) with 1 Soft Roti and Clear Vegetable Broth", 360, "Slow-digesting casein from paneer supports continuous amino acid release overnight.")
        },
        "vegan": {
            "Breakfast": ("Tofu Scramble (150g) with Turmeric, Bell Peppers & 2 Slices Ezekiel Toast", 290, "100% plant-based complete protein with anti-inflammatory curcumin."),
            "Mid-Morning Snack": ("Soy Milk (200ml) with 1 Tbsp Chia Seeds & Roasted Almonds", 160, "Rich in calcium, plant isoflavones, and ALA Omega-3 fatty acids."),
            "Lunch": ("Soya Chunks Masala (60g dry/180g cooked) with Quinoa (140g), Rajma & Mixed Greens", 470, "Packed with 35g+ pure vegan protein and slow-digesting resistant starch."),
            "Evening Snack": ("Peanut Butter (20g) on 1 Medium Banana with Hemp Seeds", 180, "Healthy fats and fast potassium to fuel muscular contractions."),
            "Dinner": ("Chickpea & Spinach Medley (180g) with Steamed Millet / 1 Roti & Warm Lentil Stew", 380, "Nutrient-dense dinner supporting microbiome diversity and restorative sleep.")
        }
    }

    if is_vegan:
        selected_cat = meal_catalog["vegan"]
    elif is_veg:
        selected_cat = meal_catalog["vegetarian"]
    elif is_egg:
        selected_cat = meal_catalog["eggetarian"]
    else:
        selected_cat = meal_catalog["non_veg"]

    meals: List[PlannedMealItem] = []
    for meal_type, frac, default_title in splits:
        m_cal = round(target_cal * frac)
        m_p = round(p_target * frac, 1)
        m_c = round(c_target * frac, 1)
        m_f = round(f_target * frac, 1)

        cat_entry = selected_cat.get(meal_type) or selected_cat.get("Lunch")
        food_desc, grams_approx, tip = cat_entry

        meals.append(PlannedMealItem(
            mealType=meal_type,
            title=f"{default_title}",
            foodDescription=food_desc,
            portionGrams=float(grams_approx),
            calories=float(m_cal),
            proteinG=m_p,
            carbsG=m_c,
            fatsG=m_f,
            nutritionistTip=tip
        ))

    return PersonalizedMealPlanResponse(
        title=goal_title,
        targetCalories=float(target_cal),
        proteinTargetG=float(p_target),
        carbsTargetG=float(c_target),
        fatsTargetG=float(f_target),
        waterTargetLiters=water_liters,
        dietSummary=summary,
        nutritionistNotes=f"Tailored for {req.dietPreference.upper()} diet with {freq} meals/day. Maintain a minimum of {water_liters}L hydration and aim for consistency over perfection.",
        meals=meals
    )


@router.post("/personalized-plan", response_model=PersonalizedMealPlanResponse)
async def create_personalized_meal_plan(
    request: PersonalizedMealPlanRequest,
    user_id: str = Depends(verify_jwt)
):
    """
    Generates a personalized nutritionist-crafted daily meal plan tailored 
    to biometrics, goal, diet preference, and meal frequency.
    """
    try:
        plan = generate_nutritionist_plan(request)
        return plan
    except Exception as e:
        logger.error(f"Failed to generate meal plan: {e}")
        raise HTTPException(status_code=500, detail="Meal plan generation failed.")

