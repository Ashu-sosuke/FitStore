package com.example.gymfitness.domain.usecase.weight

import com.example.gymfitness.data.local.entity.WeightEntity
import com.example.gymfitness.domain.repository.WeightRepository

class LogWeightUseCase(
    private val repository: WeightRepository
) {

    suspend operator fun invoke(weight: Float) {

        repository.logWeight(
            WeightEntity(
                weightKg = weight
            )
        )
    }

}