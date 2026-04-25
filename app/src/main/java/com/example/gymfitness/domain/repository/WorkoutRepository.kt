package com.example.gymfitness.domain.repository


import com.example.gymfitness.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    suspend fun createWorkout(workout: WorkoutEntity): Long

    suspend fun addExercise(exercise: ExerciseEntity): Long

    suspend fun addSet(set: SetEntity)

    fun getWorkouts(): Flow<List<WorkoutWithExercises>>

}