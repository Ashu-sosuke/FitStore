package com.example.gymfitness.domain.repository


import com.example.gymfitness.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun saveUser(user: UserEntity)
    suspend fun updateUser(user: UserEntity)
    suspend fun getUserById(deviceId: String): UserEntity?
    fun getUserFlow(): Flow<UserEntity?>
}