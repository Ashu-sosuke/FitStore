package com.example.gymfitness.domain.usecase.profile

class CalculateBmrUseCase {

    operator fun invoke(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String
    ): Float {

        val bmr = if (gender.lowercase() == "male") {

            10 * weightKg +
                    6.25 * heightCm -
                    5 * age +
                    5

        } else {

            10 * weightKg +
                    6.25 * heightCm -
                    5 * age -
                    161
        }

        return bmr.toFloat()
    }

}