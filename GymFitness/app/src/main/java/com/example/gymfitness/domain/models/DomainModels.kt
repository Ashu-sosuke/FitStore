package com.example.gymfitness.domain.models

data class UserProfile(
    val deviceId: String,
    val name: String,
    val age: Int,
    val gender: String,
    val height: Double,
    val weight: Double,
    val fitnessGoal: String,
    val activityLevel: String,
    val dailyCalorieTarget: Double,
    val proteinTarget: Double = 0.0,
    val carbsTarget: Double = 0.0,
    val fatsTarget: Double = 0.0,
    val activeSplit: String? = null,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val friendCode: String? = null,
    val showOnLeaderboards: Boolean = true,
    val experienceLevel: String = "BEGINNER",
    val daysPerWeekAvailable: Int = 4
)

data class Workout(
    val id: String? = null,
    val deviceId: String,
    val name: String,
    val exercises: List<Exercise>,
    val totalVolume: Double,
    val date: String,
    val splitType: SplitType = SplitType.FULL_BODY
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val primaryMuscle: MuscleGroup = MuscleGroup.FULL_BODY
)

data class Meal(
    val id: String? = null,
    val deviceId: String,
    val type: String,
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double
)

data class WeightEntry(
    val id: Long = 0,
    val weightKg: Float,
    val timestampMs: Long = System.currentTimeMillis()
)
