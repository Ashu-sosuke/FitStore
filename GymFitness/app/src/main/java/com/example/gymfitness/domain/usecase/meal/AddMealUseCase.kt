package com.example.gymfitness.domain.usecase.meal

import com.example.gymfitness.domain.models.Meal
import com.example.gymfitness.domain.repository.MealRepository
import javax.inject.Inject

class AddMealUseCase @Inject constructor(
    private val repository: MealRepository
) {
    suspend operator fun invoke(meal: Meal): Result<Meal> {
        return repository.addMeal(meal)
    }
}