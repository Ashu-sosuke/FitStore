package com.example.gymfitness.domain.repository

import com.example.gymfitness.domain.models.GeneratedWorkoutPlan
import com.example.gymfitness.domain.models.PlanGenerationPreferences
import com.example.gymfitness.domain.models.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun saveWorkout(workout: Workout): Result<Workout>
    fun getWorkouts(deviceId: String): Flow<List<Workout>>
    fun getWorkout(deviceId: String, workoutId: Long): Flow<Workout?>
    suspend fun syncWorkouts(deviceId: String): Result<List<Workout>>
    
    // AI Workout Generator
    suspend fun generatePlan(preferences: PlanGenerationPreferences): Result<GeneratedWorkoutPlan>
    suspend fun adoptPlan(deviceId: String, plan: GeneratedWorkoutPlan): Result<Boolean>

    // Granular methods for local tracking
    suspend fun addExercise(workoutId: Long, name: String): Long
    suspend fun addSet(exerciseId: Long, reps: Int, weightKg: Float)
}