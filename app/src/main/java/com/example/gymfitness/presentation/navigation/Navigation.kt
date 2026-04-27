package com.example.gymfitness.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymfitness.presentation.screen.home.HomeScreen
import com.example.gymfitness.presentation.screen.meals.MealScreen
import com.example.gymfitness.presentation.screen.onboarding.GetStart
import com.example.gymfitness.presentation.screen.onboarding.OnboardingScreen
import com.example.gymfitness.presentation.screen.profile.ProfileScreen
import com.example.gymfitness.presentation.screen.workouts.WorkoutScreen
import com.example.gymfitness.presentation.viewmodel.HomeViewModel
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.BgLight // Updated
import com.example.gymfitness.ui.theme.AccentBlue // Updated

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()

    // Check if user is already in DB (Session Management)
    val startDestination by userViewModel.startDestination.collectAsState()

    // Wait for the ViewModel to check the database
    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            // 1. Entrance Splash
            composable(route = Screen.GetStart.route) {
                GetStart(navController = navController)
            }

            // 2. Multi-Step Onboarding
            composable(route = Screen.Onboarding.route) {
                OnboardingScreen(navController = navController)
            }

            // 3. Main Dashboard
            composable(route = Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(navController, homeViewModel)
            }

            // 4. Feature Screens
            composable(route = Screen.Workout.route) {
                WorkoutScreen(navController)
            }
            composable(route = Screen.Meal.route) {
                MealScreen(navController)
            }
            composable(route = Screen.Profile.route) {
                ProfileScreen(navController)
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgLight), // Soft White
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AccentBlue, // Modern Blue
                strokeWidth = 4.dp
            )
        }
    }
}