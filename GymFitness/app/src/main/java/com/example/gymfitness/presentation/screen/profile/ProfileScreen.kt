package com.example.gymfitness.presentation.screen.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.BaseCard
import com.example.gymfitness.presentation.components.GhostButton
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.HomeViewModel
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserDetail()
    }

    Scaffold(
        containerColor = PageBg,
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp).statusBarsPadding())

            // Profile Header / Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(SurfaceAlt)
                    .border(2.dp, StrokeSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = TextMuted,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = viewModel.name.ifBlank { "My Profile" },
                style = Typography.displayLarge,
                color = InkBlack
            )

            Spacer(Modifier.height(32.dp))

            // Editable Profile Fields Card
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Personal Details", style = Typography.titleLarge, color = InkBlack)
                    Spacer(Modifier.height(16.dp))

                    PrimaryInputField(
                        value = viewModel.name,
                        onValueChange = { viewModel.name = it },
                        label = "Name"
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryInputField(
                                value = viewModel.age,
                                onValueChange = { viewModel.age = it },
                                label = "Age"
                            )
                        }

                        // Gender Select Buttons (Chips)
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Gender", style = Typography.labelSmall, color = TextMuted)
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Male", "Female").forEach { g ->
                                    val isSelected = viewModel.gender == g
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) SunsetOrange else SurfaceAlt)
                                            .border(1.dp, if (isSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.gender = g },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = g,
                                            color = if (isSelected) Color.White else TextMuted,
                                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryInputField(
                                value = viewModel.height,
                                onValueChange = { viewModel.height = it },
                                label = "Height (cm)"
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            PrimaryInputField(
                                value = viewModel.weight,
                                onValueChange = { viewModel.weight = it },
                                label = "Weight (kg)"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Fitness Strategy Card
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Fitness Strategy", style = Typography.titleLarge, color = InkBlack)
                    Spacer(Modifier.height(16.dp))

                    // Goal Choice
                    Text("Goal", style = Typography.labelSmall, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Lose Weight", "Gain Muscle", "Maintain").forEach { goalOption ->
                            val isSelected = viewModel.goal == goalOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SunsetOrange else SurfaceAlt)
                                    .border(1.dp, if (isSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.goal = goalOption },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(goalOption) {
                                        "Lose Weight" -> "Lose"
                                        "Gain Muscle" -> "Gain"
                                        else -> "Maintain"
                                    },
                                    color = if (isSelected) Color.White else TextMuted,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Activity Level Choice
                    Text("Activity Level", style = Typography.labelSmall, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().height(40.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Sedentary", "Light", "Moderate", "Very", "Extra").forEach { level ->
                            val isSelected = viewModel.activityLevel == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SunsetOrange else SurfaceAlt)
                                    .border(1.dp, if (isSelected) SunsetOrange else StrokeSoft, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.activityLevel = level },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    color = if (isSelected) Color.White else TextMuted,
                                    style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Privacy & Leaderboards Card
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Privacy Settings", style = Typography.titleLarge, color = InkBlack)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Show progress on leaderboards", style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = InkBlack)
                            Text("Share steps & completed workouts with friends.", style = Typography.labelSmall, color = TextMuted)
                        }
                        Switch(
                            checked = viewModel.showOnLeaderboards,
                            onCheckedChange = { viewModel.showOnLeaderboards = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SunsetOrange,
                                checkedTrackColor = SunsetOrange.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceAlt
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))


            PrimaryButton(
                text = "Save Profile Changes",
                onClick = {
                    viewModel.saveUser {
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            GhostButton(
                text = "Export Data (CSV)",
                onClick = { viewModel.exportData(context) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Red),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            ) {
                Text("RESET ALL DATA", fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardSurface,
            title = {
                Text("Reset Progress?", style = Typography.titleLarge, color = InkBlack)
            },
            text = {
                Text(
                    "This will permanently delete your profile and all fitness data from the local database. This cannot be undone.",
                    style = Typography.bodyMedium,
                    color = TextMuted
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
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = InkBlack)
                }
            }
        )
    }
}