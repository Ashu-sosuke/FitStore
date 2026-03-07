package com.example.gymfitness.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workout")
    object Meal : Screen("meal")
    object Profile : Screen("profile")

}