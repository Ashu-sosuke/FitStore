package com.example.gymfitness.domain.usecase.profile

class CalculateTdeeUseCase {

    operator fun invoke(
        bmr: Float,
        activityLevel: String
    ): Float {

        val multiplier = when(activityLevel) {

            "sedentary" -> 1.2f
            "light" -> 1.375f
            "moderate" -> 1.55f
            "active" -> 1.725f
            else -> 1.9f
        }

        return bmr * multiplier
    }

}

fun adjustForGoal(tdee: Float, goal: String): Float {

    return when(goal) {
        "cut" -> tdee - 500
        "bulk" -> tdee + 300
        else -> tdee
    }

}