package com.example.gymfitness.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequestDto(
    val deviceId: String
)

data class AuthResponseDto(
    val access_token: String,
    val token_type: String = "bearer"
)

interface AuthApiService {
    @POST("api/auth/token")
    suspend fun getAccessToken(@Body request: AuthRequestDto): AuthResponseDto
}
