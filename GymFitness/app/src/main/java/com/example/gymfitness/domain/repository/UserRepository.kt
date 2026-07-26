package com.example.gymfitness.domain.repository

import com.example.gymfitness.domain.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveProfile(profile: UserProfile)
    suspend fun getProfile(deviceId: String): UserProfile?
    fun getProfileFlow(deviceId: String): Flow<UserProfile?>
    suspend fun syncProfile(deviceId: String): Result<UserProfile>
    suspend fun updateStreak(deviceId: String)
}