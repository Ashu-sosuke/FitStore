package com.example.gymfitness.domain.repository


import com.example.gymfitness.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

interface MealRepository {

    suspend fun addMeal(meal: MealEntity)

    suspend fun deleteMeal(meal: MealEntity)

    fun getMealsForDay(timestamp: Long): Flow<List<MealEntity>>

    fun getDailyCalories(timestamp: Long): Flow<Float?>

}