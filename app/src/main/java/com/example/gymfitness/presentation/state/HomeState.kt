package com.example.gymfitness.presentation.state

data class HomeState(
    val caloriesEaten: Float = 0f,
    val caloriesTarget: Float = 2000f,

    val protein: Float = 0f,
    val proteinTarget: Float = 0f,

    val carbs: Float = 0f,
    val carbsTarget: Float = 0f,

    val fat: Float = 0f,
    val fatTarget: Float = 0f,

    val latestWeight: Float? = null,
    val userName: String = "User"
)