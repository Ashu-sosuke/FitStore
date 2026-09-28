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
    val dailyStepTarget: Int = 10000,
    val experienceLevel: String = "BEGINNER",
    val daysPerWeekAvailable: Int = 4,

    // Multi-dimensional onboarding extensions
    val goalPriority: String = "balanced",
    val trainingHistory: String = "just_starting",
    val currentRoutine: String = "no_routine",
    val trainingLocation: String = "commercial_gym",
    val warmupIncluded: Boolean = true,
    val sessionDurationMinutes: Int = 60,
    val focusMuscles: List<String> = emptyList(),
    val avoidMuscles: List<String> = emptyList(),
    val preferredExercises: List<String> = emptyList(),
    val dislikedExercises: List<String> = emptyList(),
    val physicalLimitations: List<String> = emptyList(),
    val trainingStyle: String = "bodybuilding",
    val intensityPreference: String = "moderate",
    val sleepHours: String = "7_8h",
    val stressLevel: String = "moderate",
    val dietPreference: String = "non_veg",
    val mealFrequency: Int = 3,
    val nutritionPriority: String = "high_protein",
    val planAdaptability: String = "hybrid",
    val progressionModel: String = "progressive_overload"
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
