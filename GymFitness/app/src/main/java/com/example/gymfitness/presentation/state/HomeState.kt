package com.example.gymfitness.presentation.state

import java.time.LocalDate

data class DayStepEntry(
    val date: LocalDate,
    val dayLabel: String,
    val steps: Int,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0
)

data class HomeState(
    val caloriesEaten: Float = 0f,
    val caloriesTarget: Float = 2000f,

    val protein: Float = 0f,
    val proteinTarget: Float = 0f,

    val carbs: Float = 0f,
    val carbsTarget: Float = 0f,

    val fat: Float = 0f,
    val fatsTarget: Float = 0f,

    val latestWeight: Float? = null,
    val userName: String = "User",
    val stepsWalked: Int = 0,
    val stepsTarget: Int = 10000,
    val distanceKm: Float = 0f,
    val caloriesBurned: Int = 0,
    val weeklySteps: List<DayStepEntry> = emptyList(),
    val sleepMinutes: Int = 0,
    val currentStreak: Int = 0,
    val isHealthConnectGranted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)