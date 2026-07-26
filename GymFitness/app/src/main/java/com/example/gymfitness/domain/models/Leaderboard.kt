package com.example.gymfitness.domain.models

import androidx.compose.ui.graphics.Color

data class LeaderboardEntry(
    val userId: String,
    val displayName: String,
    val avatarInitials: String,
    val avatarColor: Color,
    val weeklyPoints: Int,
    val workoutsThisWeek: Int,
    val currentStreak: Int,
    val isCurrentUser: Boolean,
    val steps: Int = 0
)


enum class LeaderboardPeriod { WEEKLY, MONTHLY, ALL_TIME }

sealed class LeaderboardUiState {
    object Loading : LeaderboardUiState()
    data class Success(
        val entries: List<LeaderboardEntry>,
        val period: LeaderboardPeriod,
        val currentUserEntry: LeaderboardEntry
    ) : LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}

data class FriendEntry(
    val userId: String,
    val displayName: String,
    val avatarInitials: String,
    val avatarColor: Color,
    val currentStreak: Int,
    val isOnline: Boolean,
    val code: String
)

sealed class FriendCodeUiState {
    object Loading : FriendCodeUiState()
    data class Success(
        val currentUserCode: String,
        val friends: List<FriendEntry>
    ) : FriendCodeUiState()
    data class Error(val message: String) : FriendCodeUiState()
}
