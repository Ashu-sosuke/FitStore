package com.example.gymfitness.presentation.navigation

sealed class Screen(val route: String) {
    object GetStart : Screen("get_start_screen")
    object Onboarding : Screen("onboarding_screen")
    object Home : Screen("home_screen")
    object Workout : Screen("workout_screen")
    object Meal : Screen("meal_screen")
    object Profile : Screen("profile_screen")
}