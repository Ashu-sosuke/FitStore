package com.example.gymfitness.presentation.componts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gymfitness.R
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.ui.theme.GreenPrimary
import com.example.gymfitness.ui.theme.NavBg
import com.example.gymfitness.ui.theme.TextGray

data class BottomNavItem(
    val icon: Int,
    val label: String,
    val route: String
)

@Composable
fun BottomNavBar(navController: NavController) {

    val items = listOf(
        BottomNavItem(R.drawable.baseline_home_24,"HOME", Screen.Home.route),
        BottomNavItem(R.drawable.gym_1025878,"WORKOUTS", Screen.Workout.route),
        BottomNavItem(R.drawable.baseline_restaurant_24,"DIET", Screen.Meal.route),
        BottomNavItem(R.drawable.baseline_person_24,"PROFILE", Screen.Profile.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(30.dp)),
        color = NavBg,
        shadowElevation = 8.dp
    ) {

        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(70.dp)
        ) {

            items.forEach { item ->

                NavigationBarItem(

                    selected = currentRoute == item.route,

                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                            restoreState = true
                        }
                    },

                    icon = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    },

                    label = {
                        Text(item.label)
                    },

                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GreenPrimary,
                        selectedTextColor = GreenPrimary,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}