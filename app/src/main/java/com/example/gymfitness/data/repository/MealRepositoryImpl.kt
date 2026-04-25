package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao
) : MealRepository {

    override suspend fun addMeal(meal: MealEntity) = mealDao.insertMeal(meal)

    override suspend fun deleteMeal(meal: MealEntity) = mealDao.deleteMeal(meal)

    override fun getMealsForDay(timestamp: Long): Flow<List<MealEntity>> =
        mealDao.getMealsForDay(timestamp)

    override fun getDailyCalories(timestamp: Long): Flow<Float?> =
        mealDao.getDailyCalories(timestamp)
}