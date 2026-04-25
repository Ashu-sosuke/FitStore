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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.gymfitness.ui.theme.BgDark
import com.example.gymfitness.ui.theme.GreenPrimary

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()

    // Check if user is already in DB
    val startDestination by userViewModel.startDestination.collectAsState()

    // Wait for the ViewModel to check the database
    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            // 1. Professional Entrance
            composable(route = Screen.GetStart.route) {
                GetStart(navController = navController)
            }

            // 2. Data Entry
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
            composable(Screen.Profile.route){
                ProfileScreen(navController)
            }
        }
    } else {
        // Optional: A loading state while checking the DB
        Box(
            modifier = Modifier.fillMaxSize().background(BgDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GreenPrimary)
        }
    }
}