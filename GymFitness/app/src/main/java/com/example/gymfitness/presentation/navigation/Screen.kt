package com.example.gymfitness.presentation.navigation

sealed class Screen(val route: String) {
    object GetStart : Screen("get_start_screen")
    object Onboarding : Screen("onboarding_screen?displayName={displayName}") {
        fun createRoute(displayName: String = "") = "onboarding_screen?displayName=$displayName"
    }
    object Home : Screen("home_screen")
    object Workout : Screen("workout_screen")
    object CreatePlan : Screen("create_plan") // Manual builder
    object PlanGenerator : Screen("plan_generator_screen") // AI Routine Generator Wizard
    object WorkoutDetail : Screen("workout_detail/{workoutId}") {
        fun createRoute(workoutId: String) = "workout_detail/$workoutId"
    }
    object Meal : Screen("meal_screen")
    object Profile : Screen("profile_screen")
    object Leaderboard : Screen("leaderboard_screen")
    object FriendCode : Screen("friend_code")
    object Analytics : Screen("analytics_screen")
}