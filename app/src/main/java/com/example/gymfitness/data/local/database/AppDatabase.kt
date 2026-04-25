package com.example.gymfitness.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.local.dao.WeightDao
import com.example.gymfitness.data.local.dao.WorkoutDao
import com.example.gymfitness.data.local.entity.ExerciseEntity
import com.example.gymfitness.data.local.entity.MealEntity
import com.example.gymfitness.data.local.entity.SetEntity
import com.example.gymfitness.data.local.entity.UserEntity
import com.example.gymfitness.data.local.entity.WeightEntity
import com.example.gymfitness.data.local.entity.WorkoutEntity

@Database(
    entities = [
        UserEntity::class,
        MealEntity::class,
        WorkoutEntity::class,
        ExerciseEntity::class,
        SetEntity::class,
        WeightEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun mealDao(): MealDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
}

