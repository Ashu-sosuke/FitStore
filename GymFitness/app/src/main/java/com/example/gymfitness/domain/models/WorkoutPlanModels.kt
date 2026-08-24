package com.example.gymfitness.domain.models

data class PlanGenerationPreferences(
    val deviceId: String,
    val weightKg: Float = 52.0f,
    val heightCm: Float = 173.0f,
    val age: Int = 24,
    val gender: String = "male",
    val fitnessGoal: String = "bulk_up", // bulk_up, cut_down, strength, endurance, general_fitness
    val daysPerWeek: Int = 5,
    val sessionDurationMinutes: Int = 60,
    val experienceLevel: String = "beginner", // beginner, intermediate, advanced
    val availableEquipment: List<String> = listOf("barbell", "dumbbell", "cable", "sled machine", "body weight"),
    val focusMuscles: List<String> = emptyList()
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
    val estimatedMinutes: Float = 6.0f
)

data class DailyWorkoutRoutine(
    val dayNumber: Int,
    val dayName: String,
    val splitCategory: String,
    val isRestDay: Boolean = false,
    val targetFocus: String,
    val estimatedDurationMinutes: Int,
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
    val nutritionTip: String
)
