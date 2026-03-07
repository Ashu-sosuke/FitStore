package com.example.gymfitness.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymfitness.presentation.screen.home.HomeScreen
import com.example.gymfitness.presentation.screen.meals.MealScreen
import com.example.gymfitness.presentation.screen.workouts.WorkoutScreen

@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ){
        composable(route = Screen.Home.route){
            HomeScreen(navController)
        }
        composable(route = Screen.Workout.route){
            WorkoutScreen(navController)
        }
        composable(route = Screen.Meal.route){
            MealScreen(navController)
        }

    }
}