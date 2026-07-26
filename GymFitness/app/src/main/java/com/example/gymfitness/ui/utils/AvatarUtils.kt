package com.example.gymfitness.ui.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

fun avatarColorFromId(userId: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFFF59E0B),
        Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF8B5CF6),
        Color(0xFFEF4444), Color(0xFF14B8A6)
    )
    return colors[userId.hashCode().absoluteValue % colors.size]
}

fun friendCodeFromUserId(userId: String): String {
    val initials = userId.take(2).uppercase().filter { it.isLetter() }.padEnd(2, 'X')
    val number = (userId.hashCode().absoluteValue % 9000) + 1000
    return "$initials$number"
}
