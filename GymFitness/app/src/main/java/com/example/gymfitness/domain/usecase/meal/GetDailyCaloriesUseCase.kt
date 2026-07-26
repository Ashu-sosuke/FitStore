package com.example.gymfitness.domain.usecase.meal

import com.example.gymfitness.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyCaloriesUseCase @Inject constructor(
    private val repository: MealRepository
) {
    operator fun invoke(timestamp: Long): Flow<Double?> {
        return repository.getDailyCalories(timestamp)
    }
}