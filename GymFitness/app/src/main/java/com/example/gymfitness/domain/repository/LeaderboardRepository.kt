package com.example.gymfitness.domain.repository

import com.example.gymfitness.domain.models.LeaderboardEntry
import com.example.gymfitness.domain.models.LeaderboardPeriod
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun observeLeaderboard(period: LeaderboardPeriod): Flow<List<LeaderboardEntry>>
    suspend fun refreshLeaderboard(period: LeaderboardPeriod)
    suspend fun addFriend(friendCode: String): Result<Unit>
    suspend fun syncPoints(points: Int)
}
