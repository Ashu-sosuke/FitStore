package com.example.gymfitness.domain.repository

import com.example.gymfitness.domain.models.Meal
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    suspend fun addMeal(meal: Meal): Result<Meal>
    fun getMeals(deviceId: String): Flow<List<Meal>>
    suspend fun syncMeals(deviceId: String): Result<List<Meal>>
    
    // Analytics/Summary
    fun getDailyCalories(timestamp: Long): Flow<Double?>
    fun getMealsForDay(deviceId: String, timestamp: Long): Flow<List<Meal>>
}