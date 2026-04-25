package com.example.gymfitness.domain.repository


import com.example.gymfitness.data.local.entity.WeightEntity
import kotlinx.coroutines.flow.Flow

interface WeightRepository {

    suspend fun logWeight(weight: WeightEntity)

    fun getWeightHistory(): Flow<List<WeightEntity>>

    fun getLatestWeight(): Flow<WeightEntity?>

}