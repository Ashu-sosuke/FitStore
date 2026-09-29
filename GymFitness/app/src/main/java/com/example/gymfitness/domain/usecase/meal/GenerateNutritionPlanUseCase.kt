package com.example.gymfitness.domain.usecase.meal

import com.example.gymfitness.domain.models.PersonalizedMealPlan
import com.example.gymfitness.domain.models.PlannedMealItem
import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.utils.NutritionPlanManager
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class GenerateNutritionPlanUseCase @Inject constructor(
    private val planManager: NutritionPlanManager
) {

    operator fun invoke(user: UserProfile): PersonalizedMealPlan {
        val w = max(user.weight, 35.0)
        val h = max(user.height, 120.0)
        val a = max(user.age, 14)
        val genderLower = user.gender.lowercase()

        // 1. Mifflin-St Jeor BMR
        val bmr = if (genderLower.contains("female") || genderLower.contains("woman")) {
            (10 * w) + (6.25 * h) - (5 * a) - 161
        } else {
            (10 * w) + (6.25 * h) - (5 * a) + 5
        }

        // 2. Activity Multiplier
        val act = user.activityLevel.lowercase()
        val multiplier = when {
            act.contains("sedentary") -> 1.2
            act.contains("light") -> 1.375
            act.contains("very") -> 1.725
            act.contains("extra") -> 1.9
            else -> 1.55
        }
        val tdee = bmr * multiplier

        // 3. Goal adjustment & macro percentages
        val goal = user.fitnessGoal.lowercase()
        val targetCal: Double
        val proteinRatio: Double
        val carbsRatio: Double
        val fatsRatio: Double
        val goalTitle: String
        val summary: String

        when {
            goal.contains("loss") || goal.contains("cut") || goal.contains("fat") -> {
                targetCal = max(tdee - 450.0, 1200.0)
                proteinRatio = 0.35
                carbsRatio = 0.35
                fatsRatio = 0.30
                goalTitle = "Fat Loss & Muscle Preservation Plan"
                summary = "Caloric deficit plan aimed at reducing body fat while protecting lean muscle mass. Daily target: ${targetCal.roundToInt()} kcal."
            }
            goal.contains("gain") || goal.contains("bulk") || goal.contains("muscle") -> {
                targetCal = tdee + 350.0
                proteinRatio = 0.30
                carbsRatio = 0.45
                fatsRatio = 0.25
                goalTitle = "Lean Muscle Hypertrophy Fuel Plan"
                summary = "Structured caloric surplus designed to maximize hypertrophy and strength recovery without excess fat gain. Daily target: ${targetCal.roundToInt()} kcal."
            }
            goal.contains("strength") -> {
                targetCal = tdee + 200.0
                proteinRatio = 0.30
                carbsRatio = 0.45
                fatsRatio = 0.25
                goalTitle = "Power & Peak Strength Fuel Plan"
                summary = "Nutrient-dense plan prioritized for central nervous system and muscular power recovery. Daily target: ${targetCal.roundToInt()} kcal."
            }
            else -> {
                targetCal = max(tdee, 1400.0)
                proteinRatio = 0.25
                carbsRatio = 0.50
                fatsRatio = 0.25
                goalTitle = "Metabolic Maintenance & Vitality Plan"
                summary = "Isocaloric balanced nutrition designed for sustained energy, gut health, and daily vitality. Daily target: ${targetCal.roundToInt()} kcal."
            }
        }

        val roundedCalories = targetCal.roundToInt().toDouble()
        val pTarget = ((roundedCalories * proteinRatio) / 4.0).roundToInt().toDouble()
        val cTarget = ((roundedCalories * carbsRatio) / 4.0).roundToInt().toDouble()
        val fTarget = ((roundedCalories * fatsRatio) / 9.0).roundToInt().toDouble()
        val waterLiters = (max(w * 0.04, 2.5) * 10).roundToInt() / 10.0

        // 4. Meal Splits
        val freq = max(min(user.mealFrequency, 5), 3)
        data class SplitConfig(val type: String, val fraction: Double, val defaultTitle: String)

        val splits = when (freq) {
            3 -> listOf(
                SplitConfig("Breakfast", 0.30, "Energizing Breakfast"),
                SplitConfig("Lunch", 0.40, "Core Power Lunch"),
                SplitConfig("Dinner", 0.30, "Recovery & Lean Dinner")
            )
            5 -> listOf(
                SplitConfig("Breakfast", 0.25, "Metabolic Kickstart Breakfast"),
                SplitConfig("Mid-Morning Snack", 0.10, "Brain & Vitality Snack"),
                SplitConfig("Lunch", 0.30, "Balanced Power Lunch"),
                SplitConfig("Evening Snack", 0.15, "Pre/Post Training Fuel"),
                SplitConfig("Dinner", 0.20, "Overnight Repair Dinner")
            )
            else -> listOf(
                SplitConfig("Breakfast", 0.25, "Power Protein Breakfast"),
                SplitConfig("Lunch", 0.35, "Core Performance Lunch"),
                SplitConfig("Evening Snack", 0.15, "Metabolic Booster Snack"),
                SplitConfig("Dinner", 0.25, "Night Recovery Dinner")
            )
        }

        // 5. Food Templates by Diet Preference
        val diet = user.dietPreference.lowercase()
        val isVeg = diet.contains("veg") && !diet.contains("non") && !diet.contains("egg")
        val isVegan = diet.contains("vegan")
        val isEgg = diet.contains("egg")

        data class MealTemplate(val desc: String, val grams: Float, val tip: String)

        val templates: Map<String, MealTemplate> = when {
            isVegan -> mapOf(
                "Breakfast" to MealTemplate("Tofu Scramble (150g) with Turmeric, Bell Peppers & 2 Slices Ezekiel Toast", 290f, "100% plant-based complete protein with anti-inflammatory curcumin."),
                "Mid-Morning Snack" to MealTemplate("Soy Milk (200ml) with 1 Tbsp Chia Seeds & Roasted Almonds", 160f, "Rich in calcium, plant isoflavones, and ALA Omega-3 fatty acids."),
                "Lunch" to MealTemplate("Soya Chunks Masala (60g dry/180g cooked) with Quinoa (140g), Rajma & Mixed Greens", 470f, "Packed with 35g+ pure vegan protein and slow-digesting resistant starch."),
                "Evening Snack" to MealTemplate("Peanut Butter (20g) on 1 Medium Banana with Hemp Seeds", 180f, "Healthy fats and fast potassium to fuel muscular contractions."),
                "Dinner" to MealTemplate("Chickpea & Spinach Medley (180g) with Steamed Millet / 1 Roti & Warm Lentil Stew", 380f, "Nutrient-dense dinner supporting microbiome diversity and restorative sleep.")
            )
            isVeg -> mapOf(
                "Breakfast" to MealTemplate("Sprouted Moong & Paneer Chilla (140g) with Mint Chutney & 1 Cup Warm Skim Milk", 290f, "Complete amino acid profile combining legumes and dairy curd/paneer."),
                "Mid-Morning Snack" to MealTemplate("Roasted Makhana (40g) & Walnuts (15g)", 120f, "Rich in magnesium and plant polyphenols for sustained mental focus."),
                "Lunch" to MealTemplate("Paneer Bhurji (120g) with Dal Tadka (150g), 2 Whole Wheat Phulkas & Cucumber Raita", 460f, "Provides 30g+ plant and dairy protein with low GI dietary fiber for gut health."),
                "Evening Snack" to MealTemplate("Sattu Protein Drink (40g) with Lemon & Roasted Chana (30g)", 180f, "Traditional high-fiber superfood providing clean plant protein and essential electrolytes."),
                "Dinner" to MealTemplate("Palak Paneer (180g) with 1 Jowar/Wheat Roti & Warm Moong Soup", 360f, "High bioavailable calcium and iron with gentle evening digestion.")
            )
            isEgg -> mapOf(
                "Breakfast" to MealTemplate("Masala Scrambled Eggs (3 Eggs) with 2 Multigrain Slices & Sautéed Tomatoes", 290f, "Rich in choline and complete essential amino acids for optimal metabolic drive."),
                "Mid-Morning Snack" to MealTemplate("Mixed Seed Bowl (Pumpkin, Chia, Sunflower) with 1 Fresh Apple", 150f, "Healthy fats and pectin fiber maintaining steady insulin levels."),
                "Lunch" to MealTemplate("Egg Curry (2 Boiled Eggs) with Brown Rice (150g), Toor Dal & Crunchy Salad", 450f, "High-satiety plate combining quality egg protein, complex starches, and lentils."),
                "Evening Snack" to MealTemplate("2 Boiled Egg Whites with Hummus & Carrot Sticks", 160f, "Low calorie, high protein snack perfect before your evening training session."),
                "Dinner" to MealTemplate("Paneer & Veggie Skewers (140g) with 1 Soft Roti and Clear Vegetable Broth", 360f, "Slow-digesting casein from paneer supports continuous amino acid release overnight.")
            )
            else -> mapOf( // Non-Veg
                "Breakfast" to MealTemplate("3 Egg White + 1 Whole Egg Omelette with 2 Multigrain Toasts & Spinach", 280f, "Take with 250ml water. High biological value protein to stimulate muscle protein synthesis early."),
                "Mid-Morning Snack" to MealTemplate("Greek Yogurt (150g) with 10 Crushed Almonds & Fresh Berries", 180f, "Rich in slow-release casein and healthy monounsaturated fats."),
                "Lunch" to MealTemplate("Grilled Chicken Breast (160g) with Steamed Brown Rice (150g), Yellow Dal & Green Salad", 450f, "Balanced carb-protein combo replenishing glycogen stores without causing a post-meal crash."),
                "Evening Snack" to MealTemplate("Whey Protein Isolate (30g) in Water with 1 Medium Banana", 180f, "Rapid leucine absorption to accelerate recovery window."),
                "Dinner" to MealTemplate("Herb Baked Fish / Chicken Tikka (150g) with 2 Soft Phulkas, Sautéed Veggies & Lentil Soup", 380f, "Lean protein with micronutrient-dense vegetables for restorative overnight cellular repair.")
            )
        }

        val plannedMeals = splits.map { split ->
            val mealCal = (roundedCalories * split.fraction).roundToInt().toDouble()
            val mealP = ((pTarget * split.fraction) * 10).roundToInt() / 10.0
            val mealC = ((cTarget * split.fraction) * 10).roundToInt() / 10.0
            val mealF = ((fTarget * split.fraction) * 10).roundToInt() / 10.0

            val tmpl = templates[split.type] ?: templates["Lunch"]!!
            PlannedMealItem(
                mealType = split.type,
                title = split.defaultTitle,
                foodDescription = tmpl.desc,
                portionGrams = tmpl.grams,
                calories = mealCal,
                proteinG = mealP,
                carbsG = mealC,
                fatsG = mealF,
                nutritionistTip = tmpl.tip
            )
        }

        val plan = PersonalizedMealPlan(
            title = goalTitle,
            targetCalories = roundedCalories,
            proteinTargetG = pTarget,
            carbsTargetG = cTarget,
            fatsTargetG = fTarget,
            waterTargetLiters = waterLiters,
            dietSummary = summary,
            nutritionistNotes = "Personalized for ${user.dietPreference.uppercase()} preference with $freq meals/day. Drink $waterLiters L water daily.",
            meals = plannedMeals
        )

        planManager.savePlan(user.deviceId, plan)
        return plan
    }
}
