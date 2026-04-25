package com.example.gymfitness.data.repository


import com.example.gymfitness.data.local.dao.WorkoutDao
import com.example.gymfitness.data.local.entity.*
import com.example.gymfitness.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override suspend fun createWorkout(workout: WorkoutEntity): Long {
        return workoutDao.insertWorkout(workout)
    }

    override suspend fun addExercise(exercise: ExerciseEntity): Long {
        return workoutDao.insertExercise(exercise)
    }

    override suspend fun addSet(set: SetEntity) {
        workoutDao.insertSet(set)
    }

    override fun getWorkouts(): Flow<List<WorkoutWithExercises>> {
        return workoutDao.getWorkouts()
    }
}