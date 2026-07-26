package com.example.gymfitness.domain.usecase.weight

import com.example.gymfitness.domain.models.WeightEntry
import com.example.gymfitness.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeightHistoryUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(): Flow<List<WeightEntry>> {
        return repository.getWeightHistory()
    }
}