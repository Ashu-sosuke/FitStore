package com.example.gymfitness.domain.usecase

import com.example.gymfitness.domain.models.CatalogExercise
import com.example.gymfitness.domain.models.SplitPlan
import com.example.gymfitness.domain.models.SplitType
import com.example.gymfitness.domain.models.StandardExerciseCatalog
import javax.inject.Inject

class SplitRecommenderUseCase @Inject constructor() {

    fun computeRecommendation(
        experienceLevel: String,
        daysPerWeek: Int,
        userLoggedWorkoutCount: Int = 0
    ): SplitPlan {
        val isBeginner = experienceLevel.equals("BEGINNER", ignoreCase = true) || userLoggedWorkoutCount < 5

        return when {
            isBeginner || daysPerWeek <= 3 -> {
                SplitPlan(
                    splitType = SplitType.FULL_BODY,
                    title = "Full Body Split",
                    daysPerWeek = if (daysPerWeek in 2..3) daysPerWeek else 3,
                    description = "Hits every major muscle group each session. Maximizes consistency and recovery for beginners & busy schedules.",
                    recommendedExercises = listOf(
                        findExercise("Barbell Back Squat"),
                        findExercise("Barbell Bench Press"),
                        findExercise("Pull-Ups / Lat Pulldown"),
                        findExercise("Overhead Barbell Press"),
                        findExercise("Romanian Deadlift (RDL)"),
                        findExercise("Plank Hold")
                    ).filterNotNull()
                )
            }
            daysPerWeek == 4 -> {
                SplitPlan(
                    splitType = SplitType.UPPER,
                    title = "Upper / Lower Split",
                    daysPerWeek = 4,
                    description = "Optimal frequency-to-recovery ratio for intermediates. Trains each muscle group 2x per week with dedicated upper & lower days.",
                    recommendedExercises = listOf(
                        findExercise("Barbell Bench Press"),
                        findExercise("Barbell Bent-Over Row"),
                        findExercise("Overhead Barbell Press"),
                        findExercise("Pull-Ups / Lat Pulldown"),
                        findExercise("Incline Dumbbell Press"),
                        findExercise("Barbell Bicep Curl")
                    ).filterNotNull()
                )
            }
            else -> {
                SplitPlan(
                    splitType = SplitType.PUSH,
                    title = "Push / Pull / Legs (PPL) Split",
                    daysPerWeek = daysPerWeek.coerceIn(5, 6),
                    description = "High-volume split for advanced lifters. Groups movements by push, pull, and leg actions with a guaranteed rest day between leg sessions.",
                    recommendedExercises = listOf(
                        findExercise("Barbell Bench Press"),
                        findExercise("Overhead Barbell Press"),
                        findExercise("Incline Dumbbell Press"),
                        findExercise("Chest Dips"),
                        findExercise("Tricep Rope Pushdown"),
                        findExercise("Dumbbell Lateral Raises")
                    ).filterNotNull()
                )
            }
        }
    }

    private fun findExercise(name: String): CatalogExercise? {
        return StandardExerciseCatalog.exercises.find { it.name.equals(name, ignoreCase = true) }
    }
}
