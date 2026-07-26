package com.example.gymfitness.data.mapper

import androidx.compose.ui.graphics.Color
import com.example.gymfitness.data.local.entity.LeaderboardEntity
import com.example.gymfitness.data.remote.api.LeaderboardEntryDto
import com.example.gymfitness.domain.models.LeaderboardEntry

fun LeaderboardEntryDto.toEntity(period: String): LeaderboardEntity {
    return LeaderboardEntity(
        userId = userId,
        friendCode = friendCode,
        displayName = displayName,
        avatarInitials = avatarInitials,
        weeklyPoints = weeklyPoints,
        workoutsThisWeek = workoutsThisWeek,
        currentStreak = currentStreak,
        period = period,
        steps = steps
    )
}

fun LeaderboardEntity.toDomain(currentUserDeviceId: String): LeaderboardEntry {
    val colors = listOf(
        Color(0xFFD0FD3E),
        Color(0xFF00F2FF),
        Color(0xFFB983FF),
        Color(0xFFFF2D55)
    )
    val colorIndex = Math.abs(userId.hashCode()) % colors.size
    
    return LeaderboardEntry(
        userId = userId,
        displayName = displayName,
        avatarInitials = avatarInitials,
        avatarColor = colors[colorIndex],
        weeklyPoints = weeklyPoints,
        workoutsThisWeek = workoutsThisWeek,
        currentStreak = currentStreak,
        isCurrentUser = (userId == currentUserDeviceId),
        steps = steps
    )
}

