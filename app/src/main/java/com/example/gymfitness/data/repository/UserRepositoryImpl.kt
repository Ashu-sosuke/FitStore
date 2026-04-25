package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.local.entity.UserEntity
import com.example.gymfitness.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject // Crucial import

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    override suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    // For the Splash screen (Direct check)
    override suspend fun getUserById(deviceId: String): UserEntity? {
        return userDao.getUserById(deviceId)
    }

    // For the Home screen (Real-time updates)
    override fun getUserFlow(): Flow<UserEntity?> {
        return userDao.getUserFlow()
    }
}