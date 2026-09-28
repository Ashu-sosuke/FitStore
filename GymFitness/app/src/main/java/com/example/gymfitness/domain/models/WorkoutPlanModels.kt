package com.example.gymfitness.domain.models

data class PlanGenerationPreferences(
    val deviceId: String,
    val weightKg: Float = 52.0f,
    val heightCm: Float = 173.0f,
    val age: Int = 24,
    val gender: String = "male",
    val fitnessGoal: String = "bulk_up", // bulk_up, cut_down, strength, endurance, general_fitness, recomposition
    val daysPerWeek: Int = 4,
    val sessionDurationMinutes: Int = 60,
    val experienceLevel: String = "beginner", // beginner, intermediate, advanced
    val availableEquipment: List<String> = listOf("barbell", "dumbbell", "cable", "sled machine", "body weight"),

    // Multi-factor personalization extensions
    val goalPriority: String = "balanced",
    val focusMuscles: List<String> = emptyList(),
    val avoidMuscles: List<String> = emptyList(),
    val preferredExercises: List<String> = emptyList(),
    val dislikedExercises: List<String> = emptyList(),
    val physicalLimitations: List<String> = emptyList(),
    val trainingStyle: String = "bodybuilding",
    val intensityPreference: String = "moderate",
    val sleepHours: String = "7_8h",
    val stressLevel: String = "moderate",
    val trainingLocation: String = "commercial_gym",
    val warmupIncluded: Boolean = true,
    val progressionModel: String = "progressive_overload"
)

data class GeneratedExercise(
    val exerciseId: String,
    val name: String,
    val targetMuscles: List<String>,
    val bodyParts: List<String>,
    val equipments: List<String>,
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val gifUrl: String = "",
    val targetSets: Int = 3,
    val targetReps: String = "8-12 reps",
    val suggestedWeightKg: Float? = null,
    val restSeconds: Int = 90,
    val estimatedMinutes: Float = 6.0f,
    val rirTarget: Int = 2,
    val progressionProtocol: String? = null
)

data class DailyWorkoutRoutine(
    val dayNumber: Int,
    val dayName: String,
    val splitCategory: String,
    val isRestDay: Boolean = false,
    val targetFocus: String,
    val estimatedDurationMinutes: Int,
    val warmupMinutes: Int = 5,
    val warmupNotes: List<String> = emptyList(),
    val cooldownMinutes: Int = 5,
    val cooldownNotes: List<String> = emptyList(),
    val exercises: List<GeneratedExercise> = emptyList()
)

data class GeneratedWorkoutPlan(
    val planId: String,
    val title: String,
    val description: String,
    val goal: String,
    val daysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val experienceLevel: String,
    val weeklyVolumeScore: Float,
    val dailyRoutines: List<DailyWorkoutRoutine>,
    val recommendedCaloricSurplusOrDeficit: String,
    val nutritionTip: String,
    val progressionOverview: String? = null,
    val injurySafetyNotes: List<String>? = null,
    val recoveryAdvisory: String? = null
)
