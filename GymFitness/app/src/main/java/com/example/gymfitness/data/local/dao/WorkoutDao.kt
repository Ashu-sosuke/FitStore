package com.example.gymfitness.data.local.dao

import androidx.room.*
import com.example.gymfitness.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SetEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Transaction
    @Query("SELECT * FROM workouts ORDER BY createdAtMs DESC")
    fun getWorkouts(): Flow<List<WorkoutWithExercises>>

    @Query("SELECT * FROM workouts ORDER BY createdAtMs DESC")
    suspend fun getAllWorkoutsList(): List<WorkoutEntity>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    fun getWorkout(workoutId: Long): Flow<WorkoutWithExercises>

    @Query("SELECT * FROM workouts WHERE isSynced = 0")
    suspend fun getUnsyncedWorkouts(): List<WorkoutEntity>

    @Query("UPDATE workouts SET isSynced = 1 WHERE id = :workoutId")
    suspend fun markWorkoutSynced(workoutId: Long)
}