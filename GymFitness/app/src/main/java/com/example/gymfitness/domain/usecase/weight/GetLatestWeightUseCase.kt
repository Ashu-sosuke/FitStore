package com.example.gymfitness.domain.usecase.weight

import com.example.gymfitness.domain.models.WeightEntry
import com.example.gymfitness.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestWeightUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(): Flow<WeightEntry?> {
        return repository.getLatestWeight()
    }
}