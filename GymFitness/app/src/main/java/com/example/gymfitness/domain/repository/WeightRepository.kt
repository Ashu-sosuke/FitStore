package com.example.gymfitness.domain.repository

import com.example.gymfitness.domain.models.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    suspend fun logWeight(weight: WeightEntry)
    fun getWeightHistory(): Flow<List<WeightEntry>>
    fun getLatestWeight(): Flow<WeightEntry?>
}