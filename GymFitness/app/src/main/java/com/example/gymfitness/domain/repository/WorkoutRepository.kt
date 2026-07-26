package com.example.gymfitness.domain.repository

import com.example.gymfitness.domain.models.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun saveWorkout(workout: Workout): Result<Workout>
    fun getWorkouts(deviceId: String): Flow<List<Workout>>
    fun getWorkout(deviceId: String, workoutId: Long): Flow<Workout?>
    suspend fun syncWorkouts(deviceId: String): Result<List<Workout>>
    
    // Granular methods for local tracking
    suspend fun addExercise(workoutId: Long, name: String): Long
    suspend fun addSet(exerciseId: Long, reps: Int, weightKg: Float)
}