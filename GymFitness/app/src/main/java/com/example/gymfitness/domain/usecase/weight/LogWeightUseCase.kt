package com.example.gymfitness.domain.usecase.weight

import com.example.gymfitness.domain.models.WeightEntry
import com.example.gymfitness.domain.repository.WeightRepository
import javax.inject.Inject

class LogWeightUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    suspend operator fun invoke(weight: Float) {
        repository.logWeight(
            WeightEntry(
                weightKg = weight
            )
        )
    }
}