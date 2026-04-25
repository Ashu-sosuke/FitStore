package com.example.gymfitness.domain.usecase.meal

import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.domain.repository.MealRepository

class AddMealUseCase(private val repository: MealRepository) {
    suspend operator fun invoke(meal: MealEntity) {
        repository.addMeal(meal)
    }
}