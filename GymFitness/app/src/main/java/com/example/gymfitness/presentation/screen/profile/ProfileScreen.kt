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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

            Spacer(Modifier.height(24.dp))

            // AI Workout Routine & Goals Card
            BaseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.PlanGenerator.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "AI Workout Routine & Schedule",
                                fontWeight = FontWeight.Bold,
                                color = OffWhite,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Modify your split, frequency (days/wk), time limit & equipment",
                            color = TextMutedDark,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = LimeGreen
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

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

            // Daily Step Goal Card
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Daily Step Goal", style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                        Surface(color = LimeTintDark, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = "${String.format(java.util.Locale.getDefault(), "%,d", viewModel.dailyStepTarget)} steps",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = LimeGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Calibrates your Home dashboard progress ring & energy expenditure.", color = TextMutedDark, fontSize = 12.sp)
                    Spacer(Modifier.height(14.dp))

                    val stepPresets = listOf(6000, 8000, 10000, 12000, 15000)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        stepPresets.forEach { preset ->
                            val isSel = viewModel.dailyStepTarget == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) LimeGreen else SurfaceDark)
                                    .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.updateStepTarget(preset) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${preset / 1000}k",
                                    color = if (isSel) Color(0xFF121212) else OffWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Fitness Strategy Card
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Fitness Strategy", style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                    Spacer(Modifier.height(16.dp))

                    // Goal Choice
                    Text("Goal", style = Typography.labelSmall, color = TextMutedDark)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Lose Weight", "Gain Muscle", "Maintain").forEach { goalOption ->
                            val isSelected = viewModel.goal == goalOption
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) LimeGreen else SurfaceDark)
                                    .border(1.dp, if (isSelected) LimeGreen else StrokeDark, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.goal = goalOption },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(goalOption) {
                                        "Lose Weight" -> "Lose"
                                        "Gain Muscle" -> "Gain"
                                        else -> "Maintain"
                                    },
                                    color = if (isSelected) Color(0xFF121212) else OffWhite,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Activity Level Choice
                    Text("Activity Level", style = Typography.labelSmall, color = TextMutedDark)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().height(40.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Sedentary", "Light", "Moderate", "Very", "Extra").forEach { level ->
                            val isSelected = viewModel.activityLevel == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeGreen else SurfaceDark)
                                    .border(1.dp, if (isSelected) LimeGreen else StrokeDark, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.activityLevel = level },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    color = if (isSelected) Color(0xFF121212) else OffWhite,
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

            Spacer(Modifier.height(28.dp))

            // Danger Zone Card: Logout & Wipe System Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.5.dp, ErrorRed.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorRed.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = ErrorRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                "Account & System Wipe",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = OffWhite
                            )
                            Text(
                                "Permanently erase data & sign out",
                                style = Typography.labelSmall,
                                color = TextMutedDark
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Logging out will permanently delete your biometric profile, routine split, workout history, nutrition logs, and credentials across both the local database and cloud server.",
                        style = Typography.bodySmall.copy(lineHeight = 18.sp),
                        color = TextMutedDark
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "LOG OUT & DELETE ALL DATA",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        var isDeleting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            containerColor = CardSurface,
            shape = RoundedCornerShape(22.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(ErrorRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = ErrorRed,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    "Log Out & Erase All Data?",
                    style = Typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = OffWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Are you absolutely sure? This will permanently wipe your profile, workouts, nutrition logs, and step history from both the device and server database. This cannot be undone.",
                        style = Typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = TextMutedDark,
                        textAlign = TextAlign.Center
                    )
                    if (isDeleting) {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = ErrorRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Wiping user data across system...",
                            style = Typography.labelSmall,
                            color = OffWhite
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        viewModel.logoutAndClearData {
                            Toast.makeText(context, "All user data deleted successfully.", Toast.LENGTH_LONG).show()
                            showDeleteDialog = false
                            navController.navigate(Screen.GetStart.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("YES, WIPE & LOG OUT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (!isDeleting) {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("CANCEL", color = OffWhite, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        )
    }
}