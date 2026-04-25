package com.example.gymfitness.domain.usecase.weight

import com.example.gymfitness.domain.repository.WeightRepository

class GetWeightHistoryUseCase (private val repository: WeightRepository) {
    operator fun invoke() = repository.getWeightHistory()
}