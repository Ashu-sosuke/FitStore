package com.example.gymfitness.domain.usecase.meal

import com.example.gymfitness.domain.repository.MealRepository

class GetMealsForDayUseCase (private val repository: MealRepository) {
    operator fun invoke(timestamp: Long) = repository.getMealsForDay(timestamp)

}