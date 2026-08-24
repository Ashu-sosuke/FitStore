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
    val focusMuscles: List<String>?
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
    val estimatedMinutes: Float
)

data class DailyWorkoutRoutineDto(
    val dayNumber: Int,
    val dayName: String,
    val splitCategory: String,
    val isRestDay: Boolean = false,
    val targetFocus: String,
    val estimatedDurationMinutes: Int,
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
    val nutritionTip: String
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
    focusMuscles = focusMuscles
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
    estimatedMinutes = estimatedMinutes
)

fun DailyWorkoutRoutineDto.toDomain(): DailyWorkoutRoutine = DailyWorkoutRoutine(
    dayNumber = dayNumber,
    dayName = dayName,
    splitCategory = splitCategory,
    isRestDay = isRestDay,
    targetFocus = targetFocus,
    estimatedDurationMinutes = estimatedDurationMinutes,
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
    nutritionTip = nutritionTip
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
                    estimatedMinutes = e.estimatedMinutes
                )
            }
        )
    },
    recommendedCaloricSurplusOrDeficit = recommendedCaloricSurplusOrDeficit,
    nutritionTip = nutritionTip
)
