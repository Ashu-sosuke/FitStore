package com.example.gymfitness.presentation.screen.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.HomeViewModel
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val userState by homeViewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BgLight, // Soft white background
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Profile Header - Clean White Surface
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(2.dp, BorderGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = AccentBlue,
                    modifier = Modifier.size(55.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "My Profile",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(32.dp))

            // User Stats Card - Modern Surface
            ProfileInfoCard(
                weight = "${userState.latestWeight ?: "--"} kg",
                target = "${userState.caloriesTarget.toInt()} kcal"
            )

            Spacer(Modifier.weight(1f))

            // Danger Zone Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Danger Zone",
                    color = Color.Red.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                // Professional Reset Button - Soft Red Tint
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE), // Very light red
                        contentColor = Color.Red
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Text(
                        "RESET ALL DATA",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Confirmation Dialog - Modern Light Style
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceWhite,
            title = {
                Text("Reset Progress?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will permanently delete your profile and all fitness data. You will need to set up your account again.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.logoutAndClearData {
                        navController.navigate(Screen.GetStart.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }) {
                    Text("YES, DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = TextPrimary)
                }
            }
        )
    }
}

@Composable
fun ProfileInfoCard(weight: String, target: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(28.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Weight", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(weight, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(35.dp)
                    .background(BorderGray)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Daily Goal", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(target, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}