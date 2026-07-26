package com.example.gymfitness.data.repository

import com.example.gymfitness.data.local.dao.UserDao
import com.example.gymfitness.data.mapper.toDomain
import com.example.gymfitness.data.mapper.toDto
import com.example.gymfitness.data.mapper.toEntity
import com.example.gymfitness.data.remote.api.ProfileApiService
import com.example.gymfitness.data.sync.SyncManager
import com.example.gymfitness.domain.models.UserProfile
import com.example.gymfitness.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val profileApi: ProfileApiService,
    private val syncManager: SyncManager
) : UserRepository {

    override suspend fun saveProfile(profile: UserProfile) {
        // Save locally first with isSynced = false
        userDao.insertUser(profile.toEntity().copy(isSynced = false))
        
        // Trigger background sync
        syncManager.scheduleSync()
    }

    override suspend fun getProfile(deviceId: String): UserProfile? {
        return userDao.getUserById(deviceId)?.toDomain()
    }

    override fun getProfileFlow(deviceId: String): Flow<UserProfile?> {
        return userDao.getUserFlow().map { it?.toDomain() }
    }

    override suspend fun syncProfile(deviceId: String): Result<UserProfile> {
        return try {
            val remoteProfile = profileApi.getProfile(deviceId)
            val domainProfile = remoteProfile.toDomain()
            // Update local DB with fresh data from remote and mark as synced
            userDao.insertUser(domainProfile.toEntity().copy(isSynced = true))
            Result.success(domainProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStreak(deviceId: String) {
        val user = userDao.getUserById(deviceId) ?: return
        
        val now = System.currentTimeMillis()
        val lastLaunch = user.lastLaunchDateMs
        
        val calendarNow = java.util.Calendar.getInstance().apply { timeInMillis = now }
        val calendarLast = java.util.Calendar.getInstance().apply { timeInMillis = lastLaunch }
        
        val dayNow = calendarNow.get(java.util.Calendar.DAY_OF_YEAR)
        val yearNow = calendarNow.get(java.util.Calendar.YEAR)
        
        val dayLast = calendarLast.get(java.util.Calendar.DAY_OF_YEAR)
        val yearLast = calendarLast.get(java.util.Calendar.YEAR)
        
        if (yearNow == yearLast && dayNow == dayLast) {
            // Already launched today, do nothing
            return
        }
        
        var newStreak = user.currentStreak
        
        // Check if consecutive day
        if (yearNow == yearLast && dayNow - dayLast == 1) {
            newStreak += 1
        } else if (yearNow > yearLast && dayNow == 1 && calendarLast.getActualMaximum(java.util.Calendar.DAY_OF_YEAR) == dayLast) {
            // Happy new year consecutive day
            newStreak += 1
        } else {
            // Missed a day or more, reset streak
            newStreak = 1
        }
        
        val highestStreak = maxOf(user.highestStreak, newStreak)
        
        val updatedUser = user.copy(
            currentStreak = newStreak,
            highestStreak = highestStreak,
            lastLaunchDateMs = now,
            isSynced = false
        )
        
        userDao.insertUser(updatedUser)
        syncManager.scheduleSync()
    }
}