package com.example.gymfitness.presentation.componts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gymfitness.R
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.ui.theme.*

data class BottomNavItem(
    val icon: Int,
    val label: String,
    val route: String
)

@Composable
fun BottomNavBar(navController: NavController) {

    val items = listOf(
        BottomNavItem(R.drawable.baseline_home_24, "Home", Screen.Home.route),
        BottomNavItem(R.drawable.gym_1025878, "Train", Screen.Workout.route),
        BottomNavItem(R.drawable.baseline_restaurant_24, "Diet", Screen.Meal.route),
        BottomNavItem(R.drawable.baseline_person_24, "Profile", Screen.Profile.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Floating Navigation Bar Container
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp) // Breathable spacing
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(24.dp)), // Subtle edge
        color = SurfaceWhite,
        shadowElevation = 12.dp // Soft shadow for the "floating" effect
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp) // Slightly taller for comfort
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentBlue,
                        selectedTextColor = AccentBlue,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = AccentBlue.copy(alpha = 0.1f) // Soft circular glow
                    )
                )
            }
        }
    }
}