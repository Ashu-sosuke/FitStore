package com.example.gymfitness.data.remote.dto

import com.example.gymfitness.domain.models.DailyWorkoutRoutine
import com.example.gymfitness.domain.models.GeneratedExercise
import com.example.gymfitness.domain.models.GeneratedWorkoutPlan
import com.example.gymfitness.domain.models.PlanGenerationPreferences
import com.google.gson.annotations.SerializedName

data class PlanGenerationRequestDto(
    val deviceId: String,
    val weightKg: Float,
    val heightCm: Float,
    val age: Int?,
    val gender: String?,
    val fitnessGoal: String,
    val daysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val experienceLevel: String,
    val availableEquipment: List<String>?,
    val goalPriority: String? = "balanced",
    val focusMuscles: List<String>? = emptyList(),
    val avoidMuscles: List<String>? = emptyList(),
    val preferredExercises: List<String>? = emptyList(),
    val dislikedExercises: List<String>? = emptyList(),
    val physicalLimitations: List<String>? = emptyList(),
    val trainingStyle: String? = "bodybuilding",
    val intensityPreference: String? = "moderate",
    val sleepHours: String? = "7_8h",
    val stressLevel: String? = "moderate",
    val trainingLocation: String? = "commercial_gym",
    val warmupIncluded: Boolean = true,
    val progressionModel: String? = "progressive_overload"
)

data class GeneratedExerciseDto(
    val exerciseId: String,
    val name: String,
    val targetMuscles: List<String>?,
    val bodyParts: List<String>?,
    val equipments: List<String>?,
    val secondaryMuscles: List<String>?,
    val instructions: List<String>?,
    val gifUrl: String?,
    val targetSets: Int,
    val targetReps: String,
    val suggestedWeightKg: Float?,
    val restSeconds: Int,
    val estimatedMinutes: Float,
    val rirTarget: Int? = 2,
    val progressionProtocol: String? = null
)

data class DailyWorkoutRoutineDto(
    val dayNumber: Int,
    val dayName: String,
    val splitCategory: String,
    val isRestDay: Boolean = false,
    val targetFocus: String,
    val estimatedDurationMinutes: Int,
    val warmupMinutes: Int = 5,
    val warmupNotes: List<String>? = emptyList(),
    val cooldownMinutes: Int = 5,
    val cooldownNotes: List<String>? = emptyList(),
    val exercises: List<GeneratedExerciseDto>?
)

data class GeneratedWorkoutPlanDto(
    val planId: String,
    val title: String,
    val description: String,
    val goal: String,
    val daysPerWeek: Int,
    val sessionDurationMinutes: Int,
    val experienceLevel: String,
    val weeklyVolumeScore: Float,
    val dailyRoutines: List<DailyWorkoutRoutineDto>,
    val recommendedCaloricSurplusOrDeficit: String,
    val nutritionTip: String,
    val progressionOverview: String? = null,
    val injurySafetyNotes: List<String>? = null,
    val recoveryAdvisory: String? = null
)

data class AdoptWorkoutPlanRequestDto(
    val deviceId: String,
    val plan: GeneratedWorkoutPlanDto
)

data class AdoptWorkoutPlanResponseDto(
    val success: Boolean,
    val message: String,
    val createdWorkoutIds: List<String>?
)

// Extension Mappers
fun PlanGenerationPreferences.toDto(): PlanGenerationRequestDto = PlanGenerationRequestDto(
    deviceId = deviceId,
    weightKg = weightKg,
    heightCm = heightCm,
    age = age,
    gender = gender,
    fitnessGoal = fitnessGoal,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    experienceLevel = experienceLevel,
    availableEquipment = availableEquipment,
    goalPriority = goalPriority,
    focusMuscles = focusMuscles,
    avoidMuscles = avoidMuscles,
    preferredExercises = preferredExercises,
    dislikedExercises = dislikedExercises,
    physicalLimitations = physicalLimitations,
    trainingStyle = trainingStyle,
    intensityPreference = intensityPreference,
    sleepHours = sleepHours,
    stressLevel = stressLevel,
    trainingLocation = trainingLocation,
    warmupIncluded = warmupIncluded,
    progressionModel = progressionModel
)

fun GeneratedExerciseDto.toDomain(): GeneratedExercise = GeneratedExercise(
    exerciseId = exerciseId,
    name = name,
    targetMuscles = targetMuscles ?: emptyList(),
    bodyParts = bodyParts ?: emptyList(),
    equipments = equipments ?: emptyList(),
    secondaryMuscles = secondaryMuscles ?: emptyList(),
    instructions = instructions ?: emptyList(),
    gifUrl = gifUrl ?: "",
    targetSets = targetSets,
    targetReps = targetReps,
    suggestedWeightKg = suggestedWeightKg,
    restSeconds = restSeconds,
    estimatedMinutes = estimatedMinutes,
    rirTarget = rirTarget ?: 2,
    progressionProtocol = progressionProtocol
)

fun DailyWorkoutRoutineDto.toDomain(): DailyWorkoutRoutine = DailyWorkoutRoutine(
    dayNumber = dayNumber,
    dayName = dayName,
    splitCategory = splitCategory,
    isRestDay = isRestDay,
    targetFocus = targetFocus,
    estimatedDurationMinutes = estimatedDurationMinutes,
    warmupMinutes = warmupMinutes,
    warmupNotes = warmupNotes ?: emptyList(),
    cooldownMinutes = cooldownMinutes,
    cooldownNotes = cooldownNotes ?: emptyList(),
    exercises = exercises?.map { it.toDomain() } ?: emptyList()
)

fun GeneratedWorkoutPlanDto.toDomain(): GeneratedWorkoutPlan = GeneratedWorkoutPlan(
    planId = planId,
    title = title,
    description = description,
    goal = goal,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    experienceLevel = experienceLevel,
    weeklyVolumeScore = weeklyVolumeScore,
    dailyRoutines = dailyRoutines.map { it.toDomain() },
    recommendedCaloricSurplusOrDeficit = recommendedCaloricSurplusOrDeficit,
    nutritionTip = nutritionTip,
    progressionOverview = progressionOverview,
    injurySafetyNotes = injurySafetyNotes,
    recoveryAdvisory = recoveryAdvisory
)

fun GeneratedWorkoutPlan.toDto(): GeneratedWorkoutPlanDto = GeneratedWorkoutPlanDto(
    planId = planId,
    title = title,
    description = description,
    goal = goal,
    daysPerWeek = daysPerWeek,
    sessionDurationMinutes = sessionDurationMinutes,
    experienceLevel = experienceLevel,
    weeklyVolumeScore = weeklyVolumeScore,
    dailyRoutines = dailyRoutines.map { r ->
        DailyWorkoutRoutineDto(
            dayNumber = r.dayNumber,
            dayName = r.dayName,
            splitCategory = r.splitCategory,
            isRestDay = r.isRestDay,
            targetFocus = r.targetFocus,
            estimatedDurationMinutes = r.estimatedDurationMinutes,
            warmupMinutes = r.warmupMinutes,
            warmupNotes = r.warmupNotes,
            cooldownMinutes = r.cooldownMinutes,
            cooldownNotes = r.cooldownNotes,
            exercises = r.exercises.map { e ->
                GeneratedExerciseDto(
                    exerciseId = e.exerciseId,
                    name = e.name,
                    targetMuscles = e.targetMuscles,
                    bodyParts = e.bodyParts,
                    equipments = e.equipments,
                    secondaryMuscles = e.secondaryMuscles,
                    instructions = e.instructions,
                    gifUrl = e.gifUrl,
                    targetSets = e.targetSets,
                    targetReps = e.targetReps,
                    suggestedWeightKg = e.suggestedWeightKg,
                    restSeconds = e.restSeconds,
                    estimatedMinutes = e.estimatedMinutes,
                    rirTarget = e.rirTarget,
                    progressionProtocol = e.progressionProtocol
                )
            }
        )
    },
    recommendedCaloricSurplusOrDeficit = recommendedCaloricSurplusOrDeficit,
    nutritionTip = nutritionTip,
    progressionOverview = progressionOverview,
    injurySafetyNotes = injurySafetyNotes,
    recoveryAdvisory = recoveryAdvisory
)
