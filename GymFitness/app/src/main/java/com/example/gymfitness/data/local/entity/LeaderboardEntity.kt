package com.example.gymfitness.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard_cache")
data class LeaderboardEntity(
    @PrimaryKey val userId: String,
    val friendCode: String,
    val displayName: String,
    val avatarInitials: String,
    val weeklyPoints: Int,
    val workoutsThisWeek: Int,
    val currentStreak: Int,
    val period: String,
    val steps: Int = 0
)

