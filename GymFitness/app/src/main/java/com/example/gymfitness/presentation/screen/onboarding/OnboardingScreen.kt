package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: UserViewModel = hiltViewModel()) {
    
    var showCelebration by remember { mutableStateOf(false) }

    if (showCelebration) {
        CelebrationScreen {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (viewModel.currentStep > 0) {
                    IconButton(onClick = { viewModel.previousStep() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OffWhite)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Step Indicator (4 neon dots)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { index ->
                        val width by animateDpAsState(
                            targetValue = if (index == viewModel.currentStep) 28.dp else 8.dp,
                            label = "stepIndicator"
                        )
                        val color = if (index <= viewModel.currentStep) LimeGreen else StrokeDark
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(width)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                if (viewModel.isSavingUser) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LimeGreen)
                    }
                } else {
                    PrimaryButton(
                        text = if (viewModel.currentStep < 3) "Continue ➔" else "Generate & Launch Plan ⚡",
                        onClick = {
                            if (viewModel.currentStep < 3) {
                                viewModel.nextStep()
                            } else {
                                viewModel.saveUser {
                                    showCelebration = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = viewModel.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                }, label = "steps"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    when (step) {
                        0 -> IdentityStep(viewModel)
                        1 -> BiometricsStep(viewModel)
                        2 -> GoalAndExperienceStep(viewModel)
                        3 -> ScheduleAndEquipmentStep(viewModel)
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun IdentityStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Welcome to Pulse", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Let's set up your personalized fitness & training engine.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(32.dp))

        Text("Your Name", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        PrimaryInputField(viewModel.name, { viewModel.name = it }, "Enter your name")

        Spacer(Modifier.height(28.dp))
        Text("Biological Gender", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val genders = listOf("Male", "Female", "Other")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            genders.forEach { gender ->
                val isSelected = viewModel.gender.equals(gender, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSelected) LimeGreen else StrokeDark, RoundedCornerShape(12.dp))
                        .clickable { viewModel.gender = gender },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gender,
                        color = if (isSelected) Color(0xFF121212) else OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BiometricsStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Your Body Metrics", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Used for exact BMR, macro targets, and compound volume load.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Weight (kg)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                PrimaryInputField(viewModel.weight, { viewModel.weight = it }, "e.g. 52.0")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Height (cm)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                PrimaryInputField(viewModel.height, { viewModel.height = it }, "e.g. 173.0")
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Age", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        PrimaryInputField(viewModel.age, { viewModel.age = it }, "e.g. 23")

        Spacer(Modifier.height(24.dp))
        Text("Daily Activity Level", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val activityLevels = listOf(
            "Sedentary" to "Desk job, minimal walking",
            "Moderate" to "Active daily lifestyle / gym 3-5x",
            "Very" to "High physical intensity or athlete"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activityLevels.forEach { (level, desc) ->
                val isSelected = viewModel.activityLevel.equals(level, ignoreCase = true)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.activityLevel = level }
                        .border(1.dp, if (isSelected) LimeGreen else StrokeDark, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) LimeTintDark else SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.activityLevel = level },
                            colors = RadioButtonDefaults.colors(selectedColor = LimeGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(level, fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
                            Text(desc, color = TextMutedDark, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalAndExperienceStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Fitness Goal & Level", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Our AI tailors sets, rep ranges, and rest periods to your objective.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(20.dp))

        Text("Primary Objective", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val goals = listOf(
            Triple("Gain Muscle", "💪 Bulk Up & Hypertrophy", "8-12 reps, 90s rest, mass builder"),
            Triple("Lose Weight", "🔥 Cut & Fat Loss", "12-15 reps, 60s rest, metabolic conditioning"),
            Triple("Strength", "⚡ Pure Strength", "4-6 reps, 150s rest, heavy compounds"),
            Triple("Maintain", "⚖️ General Fitness", "10-12 reps, balanced volume")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { (id, title, desc) ->
                val isSelected = viewModel.goal.equals(id, ignoreCase = true)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.goal = id }
                        .border(1.dp, if (isSelected) LimeGreen else StrokeDark, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) LimeTintDark else SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.goal = id },
                            colors = RadioButtonDefaults.colors(selectedColor = LimeGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
                            Text(desc, color = TextMutedDark, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Lifting Experience", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val levels = listOf("beginner" to "Beginner (<6 mo)", "intermediate" to "Intermediate (6-24 mo)", "advanced" to "Advanced (2+ yrs)")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            levels.forEach { (id, label) ->
                val isSel = viewModel.experienceLevel == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                        .clickable { viewModel.experienceLevel = id },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color(0xFF121212) else OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleAndEquipmentStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Schedule & Gear", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Tell us when and where you train to craft your weekly calendar.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(20.dp))

        Text("Workout Frequency (Days per Week)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3, 4, 5, 6).forEach { days ->
                val isSel = viewModel.daysPerWeek == days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                        .clickable { viewModel.daysPerWeek = days },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${days}d",
                        color = if (isSel) Color(0xFF121212) else OffWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Target Session Time Limit", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30 to "30m", 45 to "45m", 60 to "60m", 90 to "90m").forEach { (mins, label) ->
                val isSel = viewModel.sessionDurationMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                        .clickable { viewModel.sessionDurationMinutes = mins },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color(0xFF121212) else OffWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Available Equipment", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val allEquipments = listOf(
            "barbell" to "Barbell",
            "dumbbell" to "Dumbbells",
            "cable" to "Cables",
            "sled machine" to "Leg Machines",
            "body weight" to "Bodyweight",
            "resistance band" to "Bands"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allEquipments.forEach { (id, label) ->
                val isSel = viewModel.availableEquipments.contains(id)
                FilterChip(
                    selected = isSel,
                    onClick = { viewModel.toggleEquipment(id) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LimeGreen,
                        selectedLabelColor = Color(0xFF121212),
                        containerColor = SurfaceDark,
                        labelColor = OffWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun CelebrationScreen(onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(LimeGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF121212), modifier = Modifier.size(44.dp))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Your Plan is Ready!",
            style = Typography.displayLarge,
            color = OffWhite,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "We built your personalized split based on your biometrics, schedule, and ExerciseDB movements.",
            style = Typography.bodyLarge,
            color = TextMutedDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        PrimaryButton(
            text = "Start Training 🚀",
            onClick = onExploreClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}