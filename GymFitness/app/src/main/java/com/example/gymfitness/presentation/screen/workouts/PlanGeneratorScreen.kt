package com.example.gymfitness.presentation.screen.workouts

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gymfitness.domain.models.DailyWorkoutRoutine
import com.example.gymfitness.domain.models.GeneratedExercise
import com.example.gymfitness.domain.models.GeneratedWorkoutPlan
import com.example.gymfitness.presentation.viewmodel.WorkoutViewModel
import com.example.gymfitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanGeneratorScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val generatedPlan by viewModel.generatedPlan.collectAsState()
    val isGenerating by viewModel.isGeneratingPlan.collectAsState()
    val isAdopting by viewModel.isAdoptingPlan.collectAsState()
    val planError by viewModel.planError.collectAsState()

    var currentStep by remember { mutableStateOf(1) }

    // Form states (with requested defaults: 52kg, 5'8" / 173cm, 5 days/wk, Bulk Up)
    var weightText by remember { mutableStateOf("52.0") }
    var heightText by remember { mutableStateOf("173.0") }
    var ageText by remember { mutableStateOf("23") }
    var selectedGender by remember { mutableStateOf("male") }
    var selectedExperience by remember { mutableStateOf("beginner") }

    var selectedGoal by remember { mutableStateOf("bulk_up") }
    var selectedDays by remember { mutableStateOf(5) }
    var selectedDuration by remember { mutableStateOf(60) }

    var selectedEquipments by remember {
        mutableStateOf(listOf("barbell", "dumbbell", "cable", "sled machine", "body weight"))
    }
    var selectedFocus by remember { mutableStateOf("Full Body") }

    var selectedDayTab by remember { mutableStateOf(0) }

    LaunchedEffect(planError) {
        planError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(generatedPlan) {
        if (generatedPlan != null) {
            currentStep = 4
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = LimeGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentStep == 4) "Your AI Routine" else "AI Routine Builder",
                            fontWeight = FontWeight.Black,
                            color = OffWhite,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1 && currentStep < 4) {
                            currentStep--
                        } else {
                            viewModel.clearGeneratedPlan()
                            navController.navigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = OffWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
            )
        },
        containerColor = PageBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Step Progress Indicator
                if (currentStep < 4) {
                    StepProgressIndicator(currentStep = currentStep, totalSteps = 3)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Step Contents
                when (currentStep) {
                    1 -> {
                        Step1Biometrics(
                            weightText = weightText,
                            onWeightChange = { weightText = it },
                            heightText = heightText,
                            onHeightChange = { heightText = it },
                            ageText = ageText,
                            onAgeChange = { ageText = it },
                            selectedGender = selectedGender,
                            onGenderChange = { selectedGender = it },
                            selectedExperience = selectedExperience,
                            onExperienceChange = { selectedExperience = it },
                            onNext = { currentStep = 2 }
                        )
                    }
                    2 -> {
                        Step2GoalAndSchedule(
                            selectedGoal = selectedGoal,
                            onGoalChange = { selectedGoal = it },
                            selectedDays = selectedDays,
                            onDaysChange = { selectedDays = it },
                            selectedDuration = selectedDuration,
                            onDurationChange = { selectedDuration = it },
                            onNext = { currentStep = 3 },
                            onBack = { currentStep = 1 }
                        )
                    }
                    3 -> {
                        Step3EquipmentAndFocus(
                            selectedEquipments = selectedEquipments,
                            onToggleEquipment = { eq ->
                                selectedEquipments = if (selectedEquipments.contains(eq)) {
                                    selectedEquipments - eq
                                } else {
                                    selectedEquipments + eq
                                }
                            },
                            selectedFocus = selectedFocus,
                            onFocusChange = { selectedFocus = it },
                            isGenerating = isGenerating,
                            onGenerate = {
                                val w = weightText.toFloatOrNull() ?: 52f
                                val h = heightText.toFloatOrNull() ?: 173f
                                val a = ageText.toIntOrNull() ?: 24
                                viewModel.generateAIPlan(
                                    weightKg = w,
                                    heightCm = h,
                                    age = a,
                                    gender = selectedGender,
                                    goal = selectedGoal,
                                    daysPerWeek = selectedDays,
                                    sessionDurationMinutes = selectedDuration,
                                    experienceLevel = selectedExperience,
                                    equipment = selectedEquipments,
                                    focusMuscles = listOf(selectedFocus)
                                )
                            },
                            onBack = { currentStep = 2 }
                        )
                    }
                    4 -> {
                        generatedPlan?.let { plan ->
                            Step4PlanResult(
                                plan = plan,
                                selectedDayTab = selectedDayTab,
                                onSelectTab = { selectedDayTab = it },
                                isAdopting = isAdopting,
                                onAdoptPlan = {
                                    viewModel.adoptGeneratedPlan {
                                        Toast.makeText(
                                            context,
                                            "🎉 Workout routine adopted & scheduled!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigateUp()
                                    }
                                },
                                onRegenerate = { currentStep = 1 }
                            )
                        }
                    }
                }
            }

            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(24.dp)
                            .border(2.dp, LimeGreen, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = LimeGreen, strokeWidth = 4.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🧠 Optimizing Biomechanics...",
                                fontWeight = FontWeight.Bold,
                                color = OffWhite,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Filtering exercises, matching rep ranges & balancing weekly volume",
                                color = TextMutedDark,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 1..totalSteps) {
            val isActive = i <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) LimeGreen else SurfaceAltDark)
            )
        }
    }
}

@Composable
fun Step1Biometrics(
    weightText: String,
    onWeightChange: (String) -> Unit,
    heightText: String,
    onHeightChange: (String) -> Unit,
    ageText: String,
    onAgeChange: (String) -> Unit,
    selectedGender: String,
    onGenderChange: (String) -> Unit,
    selectedExperience: String,
    onExperienceChange: (String) -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Step 1: Your Biometrics & Experience",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OffWhite
            )
            Text(
                text = "We use your body stats to calculate exact compound loading and volume capacity.",
                fontSize = 13.sp,
                color = TextMutedDark
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = onWeightChange,
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = StrokeDark,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = onHeightChange,
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = StrokeDark,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ageText,
                    onValueChange = onAgeChange,
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = StrokeDark,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text("Gender", color = TextMutedDark, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("male" to "Male", "female" to "Female").forEach { (id, label) ->
                            FilterChip(
                                selected = selectedGender == id,
                                onClick = { onGenderChange(id) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LimeGreen,
                                    selectedLabelColor = Color(0xFF121212),
                                    containerColor = SurfaceDark,
                                    labelColor = OffWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Lifting Experience Level", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val levels = listOf(
                    Triple("beginner", "Beginner (< 6 months)", "Focus on form, motor patterns & 3 sets foundation"),
                    Triple("intermediate", "Intermediate (6-24 months)", "Progressive overload, varying angles & 3-4 sets"),
                    Triple("advanced", "Advanced (2+ years)", "High volume, specialization & intense hypertrophy sets")
                )
                levels.forEach { (id, title, desc) ->
                    val isSel = selectedExperience == id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExperienceChange(id) }
                            .border(
                                width = if (isSel) 2.dp else 1.dp,
                                color = if (isSel) LimeGreen else StrokeDark,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) LimeTintDark else SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { onExperienceChange(id) },
                                colors = RadioButtonDefaults.colors(selectedColor = LimeGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = title, fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
                                Text(text = desc, color = TextMutedDark, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor = Color(0xFF121212)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next: Goal & Schedule ➔", fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun Step2GoalAndSchedule(
    selectedGoal: String,
    onGoalChange: (String) -> Unit,
    selectedDays: Int,
    onDaysChange: (Int) -> Unit,
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Step 2: Primary Goal & Schedule",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OffWhite
            )
            Text(
                text = "Select what you want to achieve and your weekly time commitments.",
                fontSize = 13.sp,
                color = TextMutedDark
            )
        }

        item {
            Text("Fitness Objective", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val goals = listOf(
                Triple("bulk_up", "💪 Bulk Up & Hypertrophy", "8-12 reps, 90s rest, maximum muscle mass growth"),
                Triple("cut_down", "🔥 Cut & Fat Loss", "12-15 reps, 60s rest, high metabolic output & preservation"),
                Triple("strength", "⚡ Pure Strength", "4-6 reps, 150s rest, maximum neural strength & force"),
                Triple("endurance", "🏃 Endurance & Athleticism", "15-20 reps, 45s rest, muscular stamina & agility")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.forEach { (id, title, desc) ->
                    val isSel = selectedGoal == id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGoalChange(id) }
                            .border(
                                width = if (isSel) 2.dp else 1.dp,
                                color = if (isSel) LimeGreen else StrokeDark,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) LimeTintDark else SurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSel,
                                onClick = { onGoalChange(id) },
                                colors = RadioButtonDefaults.colors(selectedColor = LimeGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = title, fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 14.sp)
                                Text(text = desc, color = TextMutedDark, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Workout Frequency (Days / Week)", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4, 5, 6).forEach { days ->
                    val isSel = selectedDays == days
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) LimeGreen else SurfaceDark)
                            .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                            .clickable { onDaysChange(days) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${days}d",
                            fontWeight = FontWeight.Black,
                            color = if (isSel) Color(0xFF121212) else OffWhite,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Text("Preferred Session Time", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(30 to "30m", 45 to "45m", 60 to "60m", 90 to "90m").forEach { (mins, label) ->
                    val isSel = selectedDuration == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) LimeGreen else SurfaceDark)
                            .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                            .clickable { onDurationChange(mins) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Black,
                            color = if (isSel) Color(0xFF121212) else OffWhite,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite)
                ) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LimeGreen,
                        contentColor = Color(0xFF121212)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Next: Equipment ➔", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3EquipmentAndFocus(
    selectedEquipments: List<String>,
    onToggleEquipment: (String) -> Unit,
    selectedFocus: String,
    onFocusChange: (String) -> Unit,
    isGenerating: Boolean,
    onGenerate: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Step 3: Equipment & Target Focus",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OffWhite
            )
            Text(
                text = "Select what equipment you have access to and any specific focus area.",
                fontSize = 13.sp,
                color = TextMutedDark
            )
        }

        item {
            Text("Available Equipment", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val allEquipments = listOf(
                "barbell" to "Barbell & Plates",
                "dumbbell" to "Dumbbells",
                "cable" to "Cable Machine",
                "sled machine" to "Sled & Leg Machines",
                "body weight" to "Bodyweight / Calisthenics",
                "resistance band" to "Resistance Bands"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allEquipments.forEach { (id, label) ->
                    val isSel = selectedEquipments.contains(id)
                    FilterChip(
                        selected = isSel,
                        onClick = { onToggleEquipment(id) },
                        label = { Text(label, fontSize = 13.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LimeGreen,
                            selectedLabelColor = Color(0xFF121212),
                            containerColor = SurfaceDark,
                            labelColor = OffWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSel) LimeGreen else StrokeDark,
                            selectedBorderColor = LimeGreen,
                            enabled = true,
                            selected = isSel
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        item {
            Text("Weak Point / Priority Focus", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val focusOptions = listOf(
                "Full Body Balance",
                "Chest & Arms Focus",
                "Upper Back & Lats",
                "Legs & Posterior Chain",
                "Shoulders & V-Taper"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                focusOptions.forEach { opt ->
                    val isSel = selectedFocus == opt
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFocusChange(opt) }
                            .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) LimeTintDark else SurfaceDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSel) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = if (isSel) LimeGreen else TextMutedDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = opt, color = OffWhite, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OffWhite)
                ) {
                    Text("Back", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGenerate,
                    enabled = !isGenerating,
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LimeGreen,
                        contentColor = Color(0xFF121212)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate My Routine ⚡", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun Step4PlanResult(
    plan: GeneratedWorkoutPlan,
    selectedDayTab: Int,
    onSelectTab: (Int) -> Unit,
    isAdopting: Boolean,
    onAdoptPlan: () -> Unit,
    onRegenerate: () -> Unit
) {
    val nonRestRoutines = plan.dailyRoutines.filter { !it.isRestDay }
    val currentRoutine = if (selectedDayTab in nonRestRoutines.indices) {
        nonRestRoutines[selectedDayTab]
    } else {
        nonRestRoutines.firstOrNull()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Plan Overview Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, LimeGreen, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = LimeGreen) {
                            Text(
                                text = "✨ AI OPTIMIZED",
                                color = Color(0xFF121212),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${plan.daysPerWeek} Days / Week • ${plan.sessionDurationMinutes}m",
                            color = LimeDeepDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = plan.title,
                        fontWeight = FontWeight.Black,
                        color = OffWhite,
                        fontSize = 19.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        color = TextMutedDark,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = StrokeDark)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nutrition Target", color = TextMutedDark, fontSize = 11.sp)
                            Text(plan.recommendedCaloricSurplusOrDeficit, color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Volume Score", color = TextMutedDark, fontSize = 11.sp)
                            Text("${plan.weeklyVolumeScore.toInt()} Sets / Week", color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Daily Routine Selector Tabs
        item {
            Text("Weekly Routine Schedule", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(nonRestRoutines) { idx, routine ->
                    val isSel = idx == selectedDayTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) LimeGreen else SurfaceDark)
                            .border(1.dp, if (isSel) LimeGreen else StrokeDark, RoundedCornerShape(10.dp))
                            .clickable { onSelectTab(idx) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Day ${routine.dayNumber}",
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color(0xFF121212) else TextMutedDark,
                                fontSize = 11.sp
                            )
                            Text(
                                text = routine.splitCategory,
                                fontWeight = FontWeight.Black,
                                color = if (isSel) Color(0xFF121212) else OffWhite,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Selected Day Details Header
        currentRoutine?.let { routine ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceAltDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.dayName,
                                fontWeight = FontWeight.Bold,
                                color = OffWhite,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Focus: ${routine.targetFocus}",
                                color = LimeDeepDark,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "~${routine.estimatedDurationMinutes} mins",
                            fontWeight = FontWeight.Bold,
                            color = TextMutedDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Exercise Cards for the day
            items(routine.exercises) { ex ->
                ExerciseDetailCard(exercise = ex)
            }
        }

        // Action Buttons
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAdoptPlan,
                enabled = !isAdopting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor = Color(0xFF121212)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAdopting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF121212),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adopt & Schedule Routine 🚀", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onRegenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMutedDark)
            ) {
                Text("Re-configure Preferences", fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ExerciseDetailCard(exercise: GeneratedExercise) {
    var expandedInstructions by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StrokeDark, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val fullGifUrl = remember(exercise.gifUrl) {
                    if (exercise.gifUrl.startsWith("/")) {
                        "http://192.168.29.171:5000${exercise.gifUrl}"
                    } else {
                        exercise.gifUrl
                    }
                }

                // Exercise Thumbnail or GIF demo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceAltDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (fullGifUrl.isNotBlank()) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(fullGifUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = exercise.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = LimeGreen,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.Bold,
                        color = OffWhite,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        exercise.targetMuscles.firstOrNull()?.let { muscle ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LimeTintDark)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(muscle.uppercase(), color = LimeDeepDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        exercise.equipments.firstOrNull()?.let { eq ->
                            Text(eq, color = TextMutedDark, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = StrokeDark)
            Spacer(modifier = Modifier.height(10.dp))

            // Sets, Reps & Rest Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("TARGET SETS", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${exercise.targetSets} Sets", color = OffWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Column {
                        Text("TARGET REPS", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(exercise.targetReps, color = LimeGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Column {
                        Text("REST TIME", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${exercise.restSeconds}s", color = OffWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                if (exercise.instructions.isNotEmpty()) {
                    IconButton(
                        onClick = { expandedInstructions = !expandedInstructions },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expandedInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Instructions",
                            tint = TextMutedDark
                        )
                    }
                }
            }

            // Expandable Instructions
            AnimatedVisibility(visible = expandedInstructions) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text("Execution Guide:", color = LimeDeepDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    exercise.instructions.forEach { step ->
                        Text("• $step", color = TextMutedDark, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}
