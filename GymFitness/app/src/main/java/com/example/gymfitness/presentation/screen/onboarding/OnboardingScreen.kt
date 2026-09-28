package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.R
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
        CelebrationScreen(viewModel = viewModel) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
        return
    }

    val isStepValid = viewModel.isCurrentStepValid()

    Scaffold(
        containerColor = PageBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

                    // 6 Neon Pill Step Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(viewModel.totalSteps) { index ->
                            val width by animateDpAsState(
                                targetValue = if (index == viewModel.currentStep) 24.dp else 7.dp,
                                label = "stepIndicator"
                            )
                            val color = if (index <= viewModel.currentStep) LimeGreen else StrokeDark
                            Box(
                                modifier = Modifier
                                    .height(5.dp)
                                    .width(width)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${viewModel.currentStep + 1}/${viewModel.totalSteps}",
                        color = LimeDeepDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, NearBlack.copy(alpha = 0.95f), NearBlack)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding()
            ) {
                if (viewModel.isSavingUser) {
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LimeGreen, strokeWidth = 3.dp)
                    }
                } else {
                    PrimaryButton(
                        text = if (viewModel.currentStep < viewModel.totalSteps - 1) "Continue ➔" else "Build & Launch My Split ⚡",
                        onClick = {
                            if (viewModel.currentStep < viewModel.totalSteps - 1) {
                                viewModel.nextStep()
                            } else {
                                viewModel.saveUser {
                                    showCelebration = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isStepValid
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
            // Contextual Step Background Ambient Visual
            StepBackgroundAesthetic(step = viewModel.currentStep)

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
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    when (step) {
                        0 -> Step0AboutYou(viewModel)
                        1 -> Step1BodyAndHealth(viewModel)
                        2 -> Step2GoalAndHistory(viewModel)
                        3 -> Step3TrainingSetup(viewModel)
                        4 -> Step4Personalization(viewModel)
                        5 -> Step5RecoveryAndNutrition(viewModel)
                    }
                    Spacer(modifier = Modifier.height(110.dp))
                }
            }
        }
    }
}

@Composable
fun StepBackgroundAesthetic(step: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.08f),
            contentScale = ContentScale.Crop
        )
        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            NearBlack.copy(alpha = 0.85f),
                            NearBlack.copy(alpha = 0.95f),
                            NearBlack
                        )
                    )
                )
        )
    }
}

// ==========================================
// STEP 0: ABOUT YOU
// ==========================================
@Composable
fun Step0AboutYou(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("About You", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(6.dp))
        Text("Let's personalize your training and biometric engine.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(28.dp))

        Text("Your Full Name", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        PrimaryInputField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = "e.g. Alex Hunter",
            singleLine = true
        )
        if (viewModel.name.isNotBlank() && viewModel.name.trim().length < 2) {
            Spacer(Modifier.height(4.dp))
            Text("Please enter at least 2 characters", color = WarningAmber, fontSize = 11.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("Biological Gender", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Male", "Female", "Other").forEach { gender ->
                val isSelected = viewModel.gender.equals(gender, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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

        Spacer(Modifier.height(24.dp))
        Text("Age", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        PrimaryInputField(
            value = viewModel.age,
            onValueChange = { viewModel.age = it.filter { ch -> ch.isDigit() } },
            label = "e.g. 24",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

// ==========================================
// STEP 1: BODY & HEALTH
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step1BodyAndHealth(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Body & Health", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(6.dp))
        Text("Calculates your exact BMR, macro targets, and joint safety limits.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Weight (kg)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                PrimaryInputField(
                    value = viewModel.weight,
                    onValueChange = { viewModel.weight = it },
                    label = "e.g. 70.0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Height (cm)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                PrimaryInputField(
                    value = viewModel.height,
                    onValueChange = { viewModel.height = it },
                    label = "e.g. 175.0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Daily Activity Level", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val activityLevels = listOf(
            "Sedentary" to "Desk job, minimal walking (<4k steps)",
            "Moderate" to "Active daily routine / gym 3-4x weekly",
            "Very" to "High physical intensity or intense sport"
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

        Spacer(Modifier.height(24.dp))
        Text("Daily Step Goal", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(4.dp))
        Text("Sets your baseline daily active calorie & energy expenditure targets.", color = TextMutedDark, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(6000 to "6k", 8000 to "8k", 10000 to "10k", 12000 to "12k", 15000 to "15k").forEach { (target, label) ->
                val isSel = viewModel.dailyStepTarget == target
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                        .clickable { viewModel.dailyStepTarget = target },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color(0xFF121212) else OffWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Injury & Joint Safety", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Filled.Info, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text("AI filters out movements that stress selected areas.", color = TextMutedDark, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))

        val injuryOptions = listOf(
            "none" to "No Limitations (Full Catalog)",
            "shoulder" to "Shoulder",
            "knee" to "Knee",
            "lower_back" to "Lower Back",
            "wrist" to "Wrist",
            "elbow" to "Elbow",
            "ankle" to "Ankle",
            "neck" to "Neck"
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            injuryOptions.forEach { (key, label) ->
                val isSelected = viewModel.physicalLimitations.contains(key) ||
                        (key == "none" && viewModel.physicalLimitations.isEmpty())
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.toggleLimitation(key) },
                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (key == "none") LimeGreen else WarningAmber,
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

// ==========================================
// STEP 2: GOAL & HISTORY
// ==========================================
@Composable
fun Step2GoalAndHistory(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Goal & History", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(6.dp))
        Text("Calibrates hypertrophy rep brackets and volume ceilings.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Text("Primary Objective", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val goals = listOf(
            Triple("Build Muscle", "💪 Build Muscle", "Hypertrophy focus (8-12 reps, caloric surplus)"),
            Triple("Lose Fat", "🔥 Lose Fat", "High metabolic output & lean retention (12-15 reps)"),
            Triple("Increase Strength", "🏋️ Increase Strength", "Heavy compound power (4-6 reps, long rest)"),
            Triple("Body Recomposition", "🔄 Body Recomposition", "Simultaneous fat loss and muscle gain"),
            Triple("Maintain", "⚖️ Maintain Fitness", "Balanced conditioning and joint health")
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
        Text("Training Age & Experience", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val expOptions = listOf(
            Triple("beginner", "Beginner", "<6 months consistent lifting"),
            Triple("intermediate", "Intermediate", "6 months – 2 years"),
            Triple("advanced", "Advanced", "2+ years disciplined training")
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            expOptions.forEach { (id, label, _) ->
                val isSel = viewModel.experienceLevel == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
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
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// STEP 3: TRAINING SETUP
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3TrainingSetup(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Training Setup", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(6.dp))
        Text("Configure your weekly schedule, session budget, and gear.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Text("Workout Frequency (Days / Week)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
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
            listOf(30 to "30m", 45 to "45m", 60 to "60m", 75 to "75m", 90 to "90m").forEach { (mins, label) ->
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
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        // Warm-up toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Include Dynamic Warm-up & Cooldown", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Budgets 8 mins for injury prevention mobility drills", color = TextMutedDark, fontSize = 11.sp)
                }
                Switch(
                    checked = viewModel.warmupIncluded,
                    onCheckedChange = { viewModel.warmupIncluded = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF121212), checkedTrackColor = LimeGreen)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Available Gym Equipment", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val allEquipments = listOf(
            "barbell" to "Barbell",
            "dumbbell" to "Dumbbells",
            "cable" to "Cable Machine",
            "sled machine" to "Leg Machines",
            "body weight" to "Bodyweight",
            "resistance band" to "Resistance Bands"
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

// ==========================================
// STEP 4: PERSONALIZATION & FOCUS
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step4Personalization(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Personalization", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(6.dp))
        Text("Select focus muscle groups to receive boosted weekly set allocation.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Text("Focus Muscles (+2-4 Sets/Week)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val muscles = listOf("Chest", "Back", "Shoulders", "Biceps", "Triceps", "Abs", "Quads", "Hamstrings", "Glutes", "Calves", "Forearms")

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            muscles.forEach { muscle ->
                val isSel = viewModel.focusMuscles.contains(muscle)
                FilterChip(
                    selected = isSel,
                    onClick = { viewModel.toggleFocusMuscle(muscle) },
                    label = { Text(muscle, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
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

        Spacer(Modifier.height(24.dp))
        Text("Training Style", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val styles = listOf(
            "bodybuilding" to "🏋️ Bodybuilding (Hypertrophy & Pump)",
            "strength" to "⚡ Strength Focused (Heavy Compound)",
            "circuit" to "🔄 High Intensity / Circuit",
            "athletic" to "🏃 Athletic & Functional"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            styles.forEach { (id, label) ->
                val isSel = viewModel.trainingStyle == id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.trainingStyle = id }
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isSel) LimeTintDark else SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSel,
                            onClick = { viewModel.trainingStyle = id },
                            colors = RadioButtonDefaults.colors(selectedColor = LimeGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// STEP 5: RECOVERY & NUTRITION
// ==========================================
@Composable
fun Step5RecoveryAndNutrition(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Recovery & Diet", color = OffWhite, style = Typography.displayLarge)
        Spacer(Modifier.height(6.dp))
        Text("Aligns total weekly volume with your sleep and dietary preference.", color = TextMutedDark, style = Typography.bodyLarge)
        Spacer(Modifier.height(24.dp))

        Text("Average Nightly Sleep", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        val sleepOptions = listOf("under_5h" to "<5h", "5_6h" to "5-6h", "6_7h" to "6-7h", "7_8h" to "7-8h", "8h_plus" to "8h+")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            sleepOptions.forEach { (id, label) ->
                val isSel = viewModel.sleepHours == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                        .clickable { viewModel.sleepHours = id },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (isSel) Color(0xFF121212) else OffWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Diet Preference", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))

        val dietOptions = listOf(
            "non_veg" to "🍖 Non-Vegetarian",
            "vegetarian" to "🥗 Vegetarian",
            "eggetarian" to "🍳 Eggetarian",
            "vegan" to "🌱 Vegan"
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            dietOptions.forEach { (id, label) ->
                val isSel = viewModel.dietPreference == id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.dietPreference = id }
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = if (isSel) LimeTintDark else SurfaceDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSel,
                            onClick = { viewModel.dietPreference = id },
                            colors = RadioButtonDefaults.colors(selectedColor = LimeGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Daily Meal Frequency", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2 to "2 Meals", 3 to "3 Meals", 4 to "4 Meals", 5 to "5+ Meals").forEach { (num, label) ->
                val isSel = viewModel.mealFrequency == num
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) LimeGreen else SurfaceDark)
                        .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                        .clickable { viewModel.mealFrequency = num },
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (isSel) Color(0xFF121212) else OffWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// CELEBRATION / PLAN PREVIEW SCREEN
// ==========================================
@Composable
fun CelebrationScreen(viewModel: UserViewModel, onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(LimeGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF121212), modifier = Modifier.size(38.dp))
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = "Your AI Plan is Ready!",
            style = Typography.displayLarge.copy(fontSize = 26.sp),
            color = OffWhite,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Engineered around your biometrics, schedule, focus muscles, and recovery capacity.",
            style = Typography.bodyMedium,
            color = TextMutedDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Plan Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokeDark)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "${viewModel.daysPerWeek}-Day ${viewModel.goal} Routine",
                    fontWeight = FontWeight.Black,
                    color = LimeGreen,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = StrokeDark)
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Frequency", color = TextMutedDark, fontSize = 11.sp)
                        Text("${viewModel.daysPerWeek} days / week", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Session Time", color = TextMutedDark, fontSize = 11.sp)
                        Text("${viewModel.sessionDurationMinutes} mins", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column {
                        Text("Style", color = TextMutedDark, fontSize = 11.sp)
                        Text(viewModel.trainingStyle.replaceFirstChar { it.uppercase() }, color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                if (viewModel.focusMuscles.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Focus Muscles:", color = TextMutedDark, fontSize = 11.sp)
                    Text(viewModel.focusMuscles.joinToString(", "), color = LimeDeepDark, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        PrimaryButton(
            text = "Enter Dashboard 🚀",
            onClick = onExploreClick,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }
}