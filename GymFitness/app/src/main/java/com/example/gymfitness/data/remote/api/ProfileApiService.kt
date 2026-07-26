package com.example.gymfitness.data.remote.api

import com.example.gymfitness.data.remote.dto.ProfileCreateDto
import com.example.gymfitness.data.remote.dto.ProfileDto
import retrofit2.http.*

interface ProfileApiService {
    @POST("api/profile/")
    suspend fun createProfile(@Body profile: ProfileCreateDto): ProfileDto

    @GET("api/profile/{deviceId}")
    suspend fun getProfile(@Path("deviceId") deviceId: String): ProfileDto

    @PUT("api/profile/{deviceId}")
    suspend fun updateProfile(
        @Path("deviceId") deviceId: String,
        @Body profile: ProfileCreateDto
    ): ProfileDto
}
