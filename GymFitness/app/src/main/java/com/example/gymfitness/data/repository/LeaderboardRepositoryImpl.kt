package com.example.gymfitness.data.repository

import android.content.Context
import android.provider.Settings
import com.example.gymfitness.data.local.dao.LeaderboardDao
import com.example.gymfitness.data.local.dao.WorkoutDao
import com.example.gymfitness.data.mapper.toDomain
import com.example.gymfitness.data.mapper.toEntity
import com.example.gymfitness.data.remote.api.AddFriendRequest
import com.example.gymfitness.data.remote.api.LeaderboardApiService
import com.example.gymfitness.data.remote.api.WorkoutPointsDto
import com.example.gymfitness.domain.models.LeaderboardEntry
import com.example.gymfitness.domain.models.LeaderboardPeriod
import com.example.gymfitness.domain.repository.LeaderboardRepository
import com.example.gymfitness.utils.HealthConnectManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val leaderboardDao: LeaderboardDao,
    private val workoutDao: WorkoutDao,
    private val healthConnectManager: HealthConnectManager,
    private val apiService: LeaderboardApiService,
    @ApplicationContext private val context: Context
) : LeaderboardRepository {

    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun observeLeaderboard(period: LeaderboardPeriod): Flow<List<LeaderboardEntry>> {
        return leaderboardDao.observeLeaderboard(period.name).map { entities ->
            entities.map { it.toDomain(deviceId) }
        }
    }

    override suspend fun refreshLeaderboard(period: LeaderboardPeriod) {
        try {
            val response = apiService.getFriendsLeaderboard(deviceId, period.name.lowercase())
            if (response.isSuccessful) {
                response.body()?.let { dtoList ->
                    val entities = dtoList.map { it.toEntity(period.name) }
                    // Only clear if we got new data
                    leaderboardDao.clearPeriod(period.name)
                    leaderboardDao.upsertAll(entities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun addFriend(friendCode: String): Result<Unit> {
        return try {
            val response = apiService.addFriend(AddFriendRequest(deviceId, friendCode))
            if (response.isSuccessful) {
                refreshLeaderboard(LeaderboardPeriod.WEEKLY)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to add friend"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPoints(points: Int) {
        try {
            healthConnectManager.fetchDailySteps()
            val steps = healthConnectManager.healthConnectSteps.value
            val workoutsCount = workoutDao.getAllWorkoutsList().size

            val req = WorkoutPointsDto(
                userId = deviceId,
                points = points,
                period = "WEEKLY",
                steps = steps,
                workoutsCount = workoutsCount
            )
            apiService.updatePoints(req)
            // After updating points, refresh the leaderboard to get the latest
            refreshLeaderboard(LeaderboardPeriod.WEEKLY)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

