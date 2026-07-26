package com.example.gymfitness.data.remote.api

import com.example.gymfitness.data.remote.dto.WorkoutCreateDto
import com.example.gymfitness.data.remote.dto.WorkoutDto
import retrofit2.http.*

interface WorkoutApiService {
    @POST("api/workouts/")
    suspend fun createWorkout(@Body workout: WorkoutCreateDto): WorkoutDto

    @GET("api/workouts/{deviceId}")
    suspend fun listWorkouts(
        @Path("deviceId") deviceId: String,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): List<WorkoutDto>

    @GET("api/workouts/detail/{workoutId}")
    suspend fun getWorkoutDetail(@Path("workoutId") workoutId: String): WorkoutDto
}
