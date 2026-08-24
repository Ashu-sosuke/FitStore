package com.example.gymfitness.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.navigation.navArgument
import com.example.gymfitness.presentation.screen.home.HomeScreen
import com.example.gymfitness.presentation.screen.meals.MealScreen
import com.example.gymfitness.presentation.screen.onboarding.GetStart
import com.example.gymfitness.presentation.screen.onboarding.OnboardingScreen
import com.example.gymfitness.presentation.screen.profile.ProfileScreen
import com.example.gymfitness.presentation.screen.workouts.WorkoutScreen
import com.example.gymfitness.presentation.screen.leaderboard.LeaderboardScreen
import com.example.gymfitness.presentation.screen.social.FriendCodeScreen
import com.example.gymfitness.presentation.viewmodel.HomeViewModel
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.presentation.viewmodel.FriendCodeViewModel
import com.example.gymfitness.ui.theme.PageBg // Updated
import com.example.gymfitness.ui.theme.SunsetOrange // Updated

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.gymfitness.presentation.screen.workoutdetail.WorkoutDetailScreen
import com.example.gymfitness.presentation.screen.workoutdetail.CreatePlanScreen
import com.example.gymfitness.presentation.screen.progress.AnalyticsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = hiltViewModel()
    val startDestination by userViewModel.startDestination.collectAsState()

    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            composable(route = Screen.GetStart.route) {
                GetStart(navController = navController)
            }

            composable(
                route = Screen.Onboarding.route,
                arguments = listOf(
                    navArgument("displayName") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val displayName = backStackEntry.arguments?.getString("displayName") ?: ""
                val decodedName = try {
                    java.net.URLDecoder.decode(displayName, "UTF-8")
                } catch (_: Exception) { displayName }
                val userVm: UserViewModel = hiltViewModel()
                if (decodedName.isNotBlank() && userVm.name.isBlank()) {
                    userVm.name = decodedName
                }
                OnboardingScreen(navController = navController, viewModel = userVm)
            }

            composable(route = Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(navController, homeViewModel)
            }

            composable(route = Screen.Workout.route) {
                WorkoutScreen(navController)
            }

            composable(route = Screen.PlanGenerator.route) {
                com.example.gymfitness.presentation.screen.workouts.PlanGeneratorScreen(navController)
            }

            composable(route = Screen.CreatePlan.route) {
                CreatePlanScreen(navController)
            }

            // Corrected Workout Detail Navigation
            composable(
                route = Screen.WorkoutDetail.route,
                arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId")
                WorkoutDetailScreen(navController = navController, workoutId = workoutId)
            }



            composable(route = Screen.Meal.route) {
                MealScreen(navController)
            }

            composable(route = Screen.Profile.route) {
                ProfileScreen(navController)
            }

            composable(route = Screen.Leaderboard.route) {
                LeaderboardScreen(navController = navController)
            }

            composable(route = Screen.FriendCode.route) {
                val friendCodeViewModel: FriendCodeViewModel = hiltViewModel()
                FriendCodeScreen(
                    onBack = { navController.navigateUp() },
                    onCompare = { /* Compare view can be implemented if needed */ },
                    viewModel = friendCodeViewModel,
                    currentUserId = ""
                )
            }

            composable(route = Screen.Analytics.route) {
                AnalyticsScreen(navController = navController)
            }
        }
    } else {
        // Loading Splash
        Box(
            modifier = Modifier.fillMaxSize().background(PageBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SunsetOrange, strokeWidth = 4.dp)
        }
    }
}