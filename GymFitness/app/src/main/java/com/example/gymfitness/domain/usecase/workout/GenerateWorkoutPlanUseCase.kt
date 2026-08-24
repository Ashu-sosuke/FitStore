package com.example.gymfitness.domain.usecase.workout

import com.example.gymfitness.domain.models.*
import com.example.gymfitness.domain.repository.WorkoutRepository
import java.util.UUID
import javax.inject.Inject

class GenerateWorkoutPlanUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {

    suspend operator fun invoke(preferences: PlanGenerationPreferences): Result<GeneratedWorkoutPlan> {
        val result = repository.generatePlan(preferences)
        if (result.isSuccess) {
            return result
        }

        // Offline Fallback Generator
        return try {
            val fallbackPlan = generateOfflineFallback(preferences)
            Result.success(fallbackPlan)
        } catch (e: Exception) {
            result // return original failure if even fallback fails
        }
    }

    private fun generateOfflineFallback(pref: PlanGenerationPreferences): GeneratedWorkoutPlan {
        val isBulking = pref.fitnessGoal.contains("bulk", ignoreCase = true) || pref.fitnessGoal.contains("muscle", ignoreCase = true)
        val repScheme = if (isBulking) "8-12 reps" else "12-15 reps"
        val restSec = if (isBulking) 90 else 60
        val targetSets = if (pref.experienceLevel.equals("advanced", ignoreCase = true)) 4 else 3

        val routines = mutableListOf<DailyWorkoutRoutine>()
        val days = pref.daysPerWeek.coerceIn(2, 6)

        val splitNames = when (days) {
            2 -> listOf("Full Body Push Focus", "Full Body Pull & Legs Focus")
            3 -> listOf("Full Body A (Chest & Quads)", "Full Body B (Back & Hams)", "Full Body C (Shoulders & Arms)")
            4 -> listOf("Upper Body Heavy", "Lower Body & Calves", "Upper Hypertrophy", "Lower Posterior & Core")
            5 -> listOf("Push (Chest, Delts, Triceps)", "Pull (Back & Biceps)", "Legs & Abs", "Upper Hypertrophy", "Lower Conditioning")
            else -> listOf("Push A", "Pull A", "Legs A", "Push B", "Pull B", "Legs B")
        }

        for (i in 0 until days) {
            val routineName = "Day ${i + 1}: ${splitNames[i]}"
            val sampleExercises = StandardExerciseCatalog.exercises.shuffled().take(5).map { catalogEx ->
                GeneratedExercise(
                    exerciseId = catalogEx.id,
                    name = catalogEx.name,
                    targetMuscles = listOf(catalogEx.primaryMuscle.displayName),
                    bodyParts = listOf("Body"),
                    equipments = listOf("Gym / Dumbbell"),
                    instructions = listOf("Perform with controlled eccentric cadence.", "Maintain full range of motion."),
                    gifUrl = "",
                    targetSets = targetSets,
                    targetReps = repScheme,
                    suggestedWeightKg = if (isBulking) pref.weightKg * 0.4f else null,
                    restSeconds = restSec,
                    estimatedMinutes = 6.5f
                )
            }

            routines.add(
                DailyWorkoutRoutine(
                    dayNumber = i + 1,
                    dayName = routineName,
                    splitCategory = "Custom Split",
                    isRestDay = false,
                    targetFocus = "Hypertrophy & Progressive Volume",
                    estimatedDurationMinutes = (sampleExercises.size * 7) + 5,
                    exercises = sampleExercises
                )
            )
        }

        while (routines.size < 7) {
            val restDayNum = routines.size + 1
            routines.add(
                DailyWorkoutRoutine(
                    dayNumber = restDayNum,
                    dayName = "Day $restDayNum: Active Rest & Recovery",
                    splitCategory = "Rest",
                    isRestDay = true,
                    targetFocus = "Mobility, hydration, light walking and muscle rest.",
                    estimatedDurationMinutes = 20,
                    exercises = emptyList()
                )
            )
        }

        return GeneratedWorkoutPlan(
            planId = UUID.randomUUID().toString(),
            title = "${days}-Day ${if (isBulking) "Hypertrophy Mass Builder" else "Conditioning"} Split",
            description = "Personalized routine tailored for ${pref.weightKg}kg, ${pref.heightCm}cm at ${pref.daysPerWeek} days/week.",
            goal = pref.fitnessGoal,
            daysPerWeek = days,
            sessionDurationMinutes = pref.sessionDurationMinutes,
            experienceLevel = pref.experienceLevel,
            weeklyVolumeScore = (days * 5 * targetSets).toFloat(),
            dailyRoutines = routines,
            recommendedCaloricSurplusOrDeficit = if (isBulking) "+300 kcal Surplus" else "-400 kcal Deficit",
            nutritionTip = "Ensure adequate protein consumption (1.8-2.2g per kg) and progressive overload."
        )
    }
}
