package com.example.gymfitness.domain.usecase.meal

import com.example.gymfitness.domain.models.Meal
import com.example.gymfitness.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMealsForDayUseCase @Inject constructor(
    private val repository: MealRepository
) {
    operator fun invoke(deviceId: String, timestamp: Long): Flow<List<Meal>> {
        return repository.getMealsForDay(deviceId, timestamp)
    }
}