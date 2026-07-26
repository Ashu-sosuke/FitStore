package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.mapper.toDomain
import com.example.gymfitness.data.mapper.toDto
import com.example.gymfitness.data.mapper.toEntity
import com.example.gymfitness.data.remote.api.MealApiService
import com.example.gymfitness.data.sync.SyncManager
import com.example.gymfitness.domain.models.Meal
import com.example.gymfitness.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao,
    private val mealApi: MealApiService,
    private val syncManager: SyncManager
) : MealRepository {

    override suspend fun addMeal(meal: Meal): Result<Meal> {
        // Save locally with isSynced = false (default)
        val entity = meal.toEntity().copy(isSynced = false)
        mealDao.insertMeal(entity)
        
        // Trigger background sync
        syncManager.scheduleSync()
        
        // Return local success immediately (Offline First)
        return Result.success(meal)
    }

    override fun getMeals(deviceId: String): Flow<List<Meal>> {
        return mealDao.getAllMeals().map { list ->
            list.map { it.toDomain(deviceId) }
        }
    }

    override suspend fun syncMeals(deviceId: String): Result<List<Meal>> {
        return try {
            val remoteMeals = mealApi.listMeals(deviceId)
            val domainMeals = remoteMeals.map { it.toDomain() }
            
            domainMeals.forEach { meal ->
                // Mark as synced since it came from remote
                mealDao.insertMeal(meal.toEntity().copy(isSynced = true))
            }
            
            Result.success(domainMeals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailyCalories(timestamp: Long): Flow<Double?> {
        return mealDao.getDailyCalories(timestamp).map { it?.toDouble() }
    }

    override fun getMealsForDay(deviceId: String, timestamp: Long): Flow<List<Meal>> {
        return mealDao.getMealsForDay(timestamp).map { list ->
            list.map { it.toDomain(deviceId) }
        }
    }
}