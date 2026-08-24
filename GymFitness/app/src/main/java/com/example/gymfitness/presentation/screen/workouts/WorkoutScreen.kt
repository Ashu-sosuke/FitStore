package com.example.gymfitness.presentation.screen.workouts

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.gymfitness.domain.models.DailyWorkoutRoutine
import com.example.gymfitness.domain.models.GeneratedExercise
import com.example.gymfitness.domain.models.GeneratedWorkoutPlan
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.WeekdayTabItem
import com.example.gymfitness.presentation.viewmodel.WorkoutViewModel
import com.example.gymfitness.ui.theme.*

@Composable
fun WorkoutScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val generatedPlan by viewModel.generatedPlan.collectAsState()
    val isGenerating by viewModel.isGeneratingPlan.collectAsState()
    val weekdays by viewModel.weekdays.collectAsState()
    val selectedWeekday by viewModel.selectedWeekday.collectAsState()
    val allWorkouts by viewModel.workouts.collectAsState()

    WorkoutScreenContent(
        plan = generatedPlan,
        isGenerating = isGenerating,
        weekdays = weekdays,
        selectedWeekday = selectedWeekday,
        onSelectWeekday = viewModel::selectWeekday,
        onStartWorkout = { routine ->
            // If local workout exists for routine name, open its detail, or create plan
            val existing = allWorkouts.find { it.name.contains(routine.splitCategory, ignoreCase = true) || it.name.contains(routine.dayName, ignoreCase = true) }
            if (existing != null && existing.id != null) {
                navController.navigate(Screen.WorkoutDetail.createRoute(existing.id))
            } else {
                navController.navigate(Screen.CreatePlan.route)
            }
        },
        onConfigurePlan = { navController.navigate(Screen.PlanGenerator.route) },
        navController = navController
    )
}

@Composable
fun WorkoutScreenContent(
    plan: GeneratedWorkoutPlan?,
    isGenerating: Boolean,
    weekdays: List<WeekdayTabItem>,
    selectedWeekday: Int,
    onSelectWeekday: (Int) -> Unit,
    onStartWorkout: (DailyWorkoutRoutine) -> Unit,
    onConfigurePlan: () -> Unit,
    navController: NavController
) {
    // Resolve routine for selected weekday
    val activeRoutines = plan?.dailyRoutines ?: emptyList()
    val selectedRoutine: DailyWorkoutRoutine? = if (selectedWeekday in activeRoutines.indices) {
        activeRoutines[selectedWeekday]
    } else {
        activeRoutines.firstOrNull()
    }

    val selectedWeekdayItem = weekdays.getOrNull(selectedWeekday)

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = PageBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(PageBg)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp).statusBarsPadding())
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedWeekdayItem?.isToday == true) "Today's Training" else "${selectedWeekdayItem?.fullName ?: "Scheduled"} Training",
                            fontWeight = FontWeight.Black,
                            color = OffWhite,
                            fontSize = 24.sp
                        )
                        Text(
                            text = plan?.title ?: "Personalized Weekly Routine",
                            color = LimeDeepDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onConfigurePlan,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceDark)
                            .border(1.dp, StrokeDark, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust Plan",
                            tint = LimeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Ascending Weekdays Bar (Mon -> Sun)
            item {
                Text(
                    text = "Weekly Schedule",
                    fontWeight = FontWeight.Bold,
                    color = TextMutedDark,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weekdays.forEach { dayItem ->
                        val isSelected = dayItem.dayIndex == selectedWeekday
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LimeGreen else SurfaceDark)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) LimeGreen else if (dayItem.isToday) LimeGreen.copy(alpha = 0.5f) else StrokeDark,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectWeekday(dayItem.dayIndex) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayItem.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF121212) else TextMutedDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${dayItem.dayNumberInMonth}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color(0xFF121212) else OffWhite
                                )
                                if (dayItem.isToday) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF121212) else LimeGreen)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Current Scheduled Day View
            if (isGenerating) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = LimeGreen)
                    }
                }
            } else if (selectedRoutine != null) {
                if (selectedRoutine.isRestDay || selectedRoutine.exercises.isEmpty()) {
                    // Active Rest Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, StrokeDark, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(LimeTintDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelfImprovement,
                                        contentDescription = null,
                                        tint = LimeGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Active Recovery & Rest Day",
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Your muscles grow and repair during rest. Focus on hitting your daily protein target, light walking (7,000+ steps), and 8 hours of sleep.",
                                    color = TextMutedDark,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    // Workout Header Card
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
                                            text = selectedRoutine.splitCategory.uppercase(),
                                            color = Color(0xFF121212),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "~${selectedRoutine.estimatedDurationMinutes} mins • ${selectedRoutine.exercises.size} Exercises",
                                        color = LimeDeepDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = selectedRoutine.dayName,
                                    fontWeight = FontWeight.Black,
                                    color = OffWhite,
                                    fontSize = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Focus: ${selectedRoutine.targetFocus}",
                                    color = TextMutedDark,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { onStartWorkout(selectedRoutine) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = LimeGreen,
                                        contentColor = Color(0xFF121212)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Start Today's Workout 🚀", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Scheduled Exercises Header
                    item {
                        Text(
                            text = "Today's Exercise Routine",
                            fontWeight = FontWeight.Bold,
                            color = OffWhite,
                            fontSize = 16.sp
                        )
                    }

                    // Scheduled Exercise Cards
                    items(selectedRoutine.exercises) { ex ->
                        ExerciseScheduleCard(exercise = ex)
                    }
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No routine generated yet", fontWeight = FontWeight.Bold, color = OffWhite)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onConfigurePlan,
                                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color(0xFF121212))
                            ) {
                                Text("Generate Routine ⚡", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ExerciseScheduleCard(exercise: GeneratedExercise) {
    var expanded by remember { mutableStateOf(false) }

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

                // Exercise Thumbnail or Animated GIF
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
                            modifier = Modifier.size(28.dp)
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
                                Text(
                                    text = muscle.uppercase(),
                                    color = LimeDeepDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        exercise.equipments.firstOrNull()?.let { eq ->
                            Text(text = eq, color = TextMutedDark, fontSize = 11.sp)
                        }
                    }
                }

                if (exercise.instructions.isNotEmpty()) {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Instructions",
                            tint = TextMutedDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = StrokeDark)
            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("SETS", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${exercise.targetSets}", color = OffWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Column {
                        Text("REPS", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(exercise.targetReps, color = LimeGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Column {
                        Text("REST", color = TextMutedDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${exercise.restSeconds}s", color = OffWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                Text(
                    text = "~${exercise.estimatedMinutes}m",
                    color = TextMutedDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Expandable Instruction Steps
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Form & Execution:", color = LimeDeepDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    exercise.instructions.forEach { step ->
                        Text("• $step", color = TextMutedDark, fontSize = 12.sp, modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }
        }
    }
}