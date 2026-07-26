package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.WorkoutDao
import com.example.gymfitness.data.local.entity.ExerciseEntity
import com.example.gymfitness.data.local.entity.SetEntity
import com.example.gymfitness.data.local.entity.WorkoutEntity
import com.example.gymfitness.data.mapper.toDomain
import com.example.gymfitness.data.mapper.toDto
import com.example.gymfitness.data.remote.api.WorkoutApiService
import com.example.gymfitness.domain.models.Workout
import com.example.gymfitness.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val workoutApi: WorkoutApiService
) : WorkoutRepository {

    override suspend fun saveWorkout(workout: Workout): Result<Workout> {
        // Complex local save
        val workoutId = workoutDao.insertWorkout(
            WorkoutEntity(name = workout.name)
        )
        
        workout.exercises.forEach { exercise ->
            val exerciseId = workoutDao.insertExercise(
                ExerciseEntity(workoutId = workoutId, name = exercise.name)
            )
            workoutDao.insertSet(
                SetEntity(exerciseId = exerciseId, reps = exercise.reps, weightKg = exercise.weight.toFloat())
            )
        }

        // Push to remote
        return try {
            val remoteWorkout = workoutApi.createWorkout(workout.toDto())
            Result.success(remoteWorkout.toDomain())
        } catch (e: Exception) {
            Result.success(workout)
        }
    }

    override fun getWorkouts(deviceId: String): Flow<List<Workout>> {
        return workoutDao.getWorkouts().map { list ->
            list.map { it.toDomain(deviceId) }
        }
    }

    override fun getWorkout(deviceId: String, workoutId: Long): Flow<Workout?> {
        return workoutDao.getWorkout(workoutId).map { it?.toDomain(deviceId) }
    }

    override suspend fun syncWorkouts(deviceId: String): Result<List<Workout>> {
        return try {
            val remoteWorkouts = workoutApi.listWorkouts(deviceId)
            val domainWorkouts = remoteWorkouts.map { it.toDomain() }
            
            // Syncing remote to local is complex for workouts due to relations.
            // Typically you'd clear local and rebuild or match by some external ID.
            
            Result.success(domainWorkouts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addExercise(workoutId: Long, name: String): Long {
        return workoutDao.insertExercise(com.example.gymfitness.data.local.entity.ExerciseEntity(workoutId = workoutId, name = name))
    }

    override suspend fun addSet(exerciseId: Long, reps: Int, weightKg: Float) {
        workoutDao.insertSet(com.example.gymfitness.data.local.entity.SetEntity(exerciseId = exerciseId, reps = reps, weightKg = weightKg))
    }
}