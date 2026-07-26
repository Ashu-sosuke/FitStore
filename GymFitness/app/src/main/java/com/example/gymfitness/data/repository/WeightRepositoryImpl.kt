package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.WeightDao
import com.example.gymfitness.data.mapper.toDomain
import com.example.gymfitness.data.mapper.toEntity
import com.example.gymfitness.domain.models.WeightEntry
import com.example.gymfitness.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao
) : WeightRepository {

    override suspend fun logWeight(weight: WeightEntry) {
        weightDao.insertWeight(weight.toEntity())
    }

    override fun getWeightHistory(): Flow<List<WeightEntry>> {
        return weightDao.getWeights().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getLatestWeight(): Flow<WeightEntry?> {
        return weightDao.getLatestWeight().map { it?.toDomain() }
    }
}