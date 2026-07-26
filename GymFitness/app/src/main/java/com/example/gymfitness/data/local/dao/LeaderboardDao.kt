package com.example.gymfitness.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymfitness.data.local.entity.LeaderboardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {

    @Query("SELECT * FROM leaderboard_cache WHERE period = :period ORDER BY weeklyPoints DESC")
    fun observeLeaderboard(period: String): Flow<List<LeaderboardEntity>>

    @Query("SELECT * FROM leaderboard_cache WHERE period = :period ORDER BY weeklyPoints DESC")
    suspend fun getLeaderboard(period: String): List<LeaderboardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<LeaderboardEntity>)

    @Query("DELETE FROM leaderboard_cache WHERE period = :period")
    suspend fun clearPeriod(period: String)
}
