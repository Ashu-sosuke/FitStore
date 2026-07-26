package com.example.gymfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymfitness.data.local.entity.MacroTotals
import com.example.gymfitness.data.local.entity.MealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Query("SELECT * FROM meals ORDER BY timestampMs DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals ORDER BY timestampMs DESC")
    suspend fun getAllMealsList(): List<MealEntity>

    @Query(
        """
        SELECT * FROM meals 
        WHERE date(timestampMs/1000,'unixepoch','localtime') = date(:timestamp/1000,'unixepoch','localtime')
        ORDER BY timestampMs DESC
        """
    )
    fun getMealsForDay(timestamp: Long): Flow<List<MealEntity>>

    @Query(
        """
        SELECT SUM(calories) FROM meals
        WHERE date(timestampMs/1000,'unixepoch','localtime') = date(:timestamp/1000,'unixepoch','localtime')
        """
    )
    fun getDailyCalories(timestamp: Long): Flow<Float?>

    @Query(
        """
        SELECT 
        SUM(proteinG) as protein,
        SUM(carbsG) as carbs,
        SUM(fatG) as fat
        FROM meals
        WHERE date(timestampMs/1000,'unixepoch','localtime') = date(:timestamp/1000,'unixepoch','localtime')
        """
    )
    fun getDailyMacros(timestamp: Long): Flow<MacroTotals?>

    @Query("SELECT * FROM meals WHERE isSynced = 0")
    suspend fun getUnsyncedMeals(): List<MealEntity>

    @Query("UPDATE meals SET isSynced = 1 WHERE id = :mealId")
    suspend fun markMealSynced(mealId: Long)
}