package com.example.gymfitness.data.repository


import com.example.gymfitness.data.local.dao.WeightDao
import com.example.gymfitness.data.local.entity.WeightEntity
import com.example.gymfitness.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeightRepositoryImpl @Inject constructor(
    private val weightDao: WeightDao
) : WeightRepository {

    override suspend fun logWeight(weight: WeightEntity) {
        weightDao.insertWeight(weight)
    }

    override fun getWeightHistory(): Flow<List<WeightEntity>> {
        return weightDao.getWeights()
    }

    override fun getLatestWeight(): Flow<WeightEntity?> {
        return weightDao.getLatestWeight()
    }
}