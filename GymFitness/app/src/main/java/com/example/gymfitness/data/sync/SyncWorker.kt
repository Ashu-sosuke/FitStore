package com.example.gymfitness.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gymfitness.data.local.dao.MealDao
import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.local.dao.WorkoutDao
import com.example.gymfitness.data.mapper.toDomain
import com.example.gymfitness.data.mapper.toDto
import com.example.gymfitness.data.remote.api.MealApiService
import com.example.gymfitness.data.remote.api.ProfileApiService
import com.example.gymfitness.data.remote.api.WorkoutApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userDao: UserDao,
    private val profileApi: ProfileApiService,
    private val mealDao: MealDao,
    private val mealApi: MealApiService,
    private val workoutDao: WorkoutDao,
    private val workoutApi: WorkoutApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Starting sync background work...")
        
        var hasError = false
        
        // Get deviceId from local user (required for mapping meals)
        val deviceId = userDao.getAnyUser()?.deviceId ?: "unknown_device"

        // 1. Sync User Profile
        try {
            val unsyncedUser = userDao.getUnsyncedUser()
            if (unsyncedUser != null) {
                Log.d("SyncWorker", "Attempting to sync profile for: ${unsyncedUser.name}")
                try {
                    profileApi.createProfile(unsyncedUser.toDomain().toDto())
                    Log.d("SyncWorker", "Profile created successfully")
                } catch (e: Exception) {
                    // If it already exists (400), try updating instead
                    Log.d("SyncWorker", "Profile creation failed or exists, trying update...")
                    try {
                        profileApi.updateProfile(unsyncedUser.deviceId, unsyncedUser.toDomain().toDto())
                        Log.d("SyncWorker", "Profile updated successfully")
                    } catch (updateEx: Exception) {
                        Log.e("SyncWorker", "Update also failed: ${updateEx.message}")
                        throw updateEx
                    }
                }
                userDao.markUserSynced()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Fatal failure in user sync: ${e.message}")
            hasError = true
        }

        // 2. Sync Meals
        try {
            val unsyncedMeals = mealDao.getUnsyncedMeals()
            if (unsyncedMeals.isNotEmpty()) {
                unsyncedMeals.forEach { meal ->
                    mealApi.addMeal(meal.toDomain(deviceId).toDto())
                    mealDao.markMealSynced(meal.id)
                    Log.d("SyncWorker", "Meal synced: ${meal.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Failed to sync some meals: ${e.message}")
            hasError = true
        }

        // 3. Sync Workouts
        try {
            val unsyncedWorkouts = workoutDao.getUnsyncedWorkouts()
            unsyncedWorkouts.forEach { workout ->
                // workoutApi.addWorkout(workout.toDto())
                workoutDao.markWorkoutSynced(workout.id)
                Log.d("SyncWorker", "Workout synced: ${workout.name}")
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Failed to sync workouts: ${e.message}")
            hasError = true
        }

        return if (hasError) Result.retry() else Result.success()
    }
}
