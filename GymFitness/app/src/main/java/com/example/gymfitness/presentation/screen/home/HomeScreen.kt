package com.example.gymfitness.presentation.screen.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gymfitness.R
import com.example.gymfitness.domain.models.Workout
import com.example.gymfitness.presentation.components.*
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.state.DayStepEntry
import com.example.gymfitness.presentation.state.HomeState
import com.example.gymfitness.presentation.viewmodel.HomeViewModel
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val state by viewModel.state.collectAsState()
    val friendCode by viewModel.friendCode.collectAsState()

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.containsAll(permissions)) {
            viewModel.fetchHealthConnectSteps()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
        viewModel.fetchHealthConnectSteps()
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
    }

    HomeScreenContent(
        state = state,
        friendCode = friendCode,
        navController = navController,
        onConnectHealth = { permissionLauncher.launch(permissions) },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    friendCode: String,
    navController: NavController,
    onConnectHealth: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = PageBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
            PrimaryFAB(onClick = { navController.navigate(Screen.CreatePlan.route) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp).statusBarsPadding())
            
            // Header Top Row: Greeting & Name (Left) + Avatar (Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMutedDark
                    )
                    Text(
                        text = state.userName.ifBlank { "User" },
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                        color = OffWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(12.dp))

                AvatarInitials(initials = state.userName.take(1).uppercase())
            }

            Spacer(Modifier.height(12.dp))

            // Header Sub-Row: Streak Chip & Friend Code Chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Chip
                Surface(
                    color = LimeTintDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${state.currentStreak} day streak",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LimeGreen
                        )
                    }
                }

                // Friend Code Chip
                if (friendCode.isNotEmpty() && friendCode != "------") {
                    CodeChip(
                        code = friendCode,
                        onCopied = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Friend code $friendCode copied to clipboard!")
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Daily Progress Card (Nutrition & Macro Targets)
            DailyProgressCard(
                caloriesEaten = state.caloriesEaten.toInt(),
                caloriesTarget = if (state.caloriesTarget > 0) state.caloriesTarget.toInt() else 2000,
                protein = state.protein,
                proteinTarget = if (state.proteinTarget > 0) state.proteinTarget else 140f,
                carbs = state.carbs,
                carbsTarget = if (state.carbsTarget > 0) state.carbsTarget else 220f,
                fat = state.fat,
                fatsTarget = if (state.fatsTarget > 0) state.fatsTarget else 65f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Step Activity & Progress Hub Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Step Activity",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OffWhite
                )
                Text(
                    text = "View Analytics ➔",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LimeGreen,
                    modifier = Modifier.clickable { navController.navigate(Screen.Analytics.route) }
                )
            }
            Spacer(Modifier.height(12.dp))
            BaseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable { navController.navigate(Screen.Analytics.route) }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Top Row: Steps count + Dynamic Percentage Badge based on custom step target
                    val stepsTarget = if (state.stepsTarget > 0) state.stepsTarget else 10000
                    val stepPct = ((state.stepsWalked.toFloat() / stepsTarget.toFloat()) * 100).toInt().coerceIn(0, 999)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("STEPS TODAY", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextMutedDark)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%,d", state.stepsWalked),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Black),
                                    color = OffWhite
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "/ ${String.format(Locale.getDefault(), "%,d", stepsTarget)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextMutedDark,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Surface(
                            color = if (stepPct >= 100) LimeGreen else LimeTintDark,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$stepPct% Done",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = if (stepPct >= 100) Color(0xFF121212) else LimeGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Dynamic Progress Bar against custom step target
                    val progressAnim by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = (state.stepsWalked.toFloat() / stepsTarget.toFloat()).coerceIn(0f, 1f),
                        label = "stepProgress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceAltDark)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressAnim)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LimeGreen)
                        )
                    }

                    // 3 Metric Pills: Distance, Burned, Active Time
                    val distanceKm = if (state.distanceKm > 0f) {
                        String.format(Locale.getDefault(), "%.2f", state.distanceKm)
                    } else {
                        String.format(Locale.getDefault(), "%.2f", state.stepsWalked * 0.00075f)
                    }
                    val burnedKcal = if (state.caloriesBurned > 0) {
                        state.caloriesBurned
                    } else {
                        (state.stepsWalked * 0.04f).toInt()
                    }
                    val activeMins = state.stepsWalked / 100

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("DISTANCE", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = TextMutedDark)
                            Text("$distanceKm km", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                        }
                        Column {
                            Text("BURNED", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = TextMutedDark)
                            Text("$burnedKcal kcal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LimeGreen)
                        }
                        Column {
                            Text("ACTIVE TIME", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = TextMutedDark)
                            Text("${activeMins}m", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                        }
                    }

                    HorizontalDivider(color = StrokeDark)

                    // Weekly Steps Chart
                    StepsBarChart(
                        weeklySteps = state.weeklySteps,
                        isHealthConnectGranted = state.isHealthConnectGranted,
                        isLoading = state.isLoading,
                        onConnectClick = onConnectHealth
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Active Split Hero Banner
            if (state.workouts.isNotEmpty()) {
                val todayWorkout = state.workouts.firstOrNull()
                ActiveSplitHeroCard(
                    splitTitle = state.activeSplitTitle,
                    nextWorkoutName = todayWorkout?.name ?: "Upcoming Routine",
                    exerciseCount = todayWorkout?.exercises?.size ?: 5,
                    onStartClick = {
                        todayWorkout?.id?.let { wid ->
                            navController.navigate(Screen.WorkoutDetail.createRoute(wid))
                        } ?: navController.navigate(Screen.Workout.route)
                    }
                )
                Spacer(Modifier.height(24.dp))
            }

            // Scheduled Workouts Carousel Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Routines",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OffWhite
                )
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LimeGreen,
                    modifier = Modifier.clickable { navController.navigate(Screen.Workout.route) }
                )
            }

            Spacer(Modifier.height(12.dp))

            if (state.workouts.isEmpty()) {
                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    EmptyWorkoutCard(onCreateClick = { navController.navigate(Screen.PlanGenerator.route) })
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(state.workouts) { index, workout ->
                        LiveWorkoutCard(
                            workout = workout,
                            index = index,
                            onClick = {
                                workout.id?.let { wid ->
                                    navController.navigate(Screen.WorkoutDetail.createRoute(wid))
                                } ?: navController.navigate(Screen.Workout.route)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // Quick Stats / Today's Activity (Sleep, Steps, Heart Rate)
            Text(
                text = "Today's Biometrics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OffWhite,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val sleepHours = state.sleepMinutes / 60
                val sleepMins = state.sleepMinutes % 60
                val sleepStr = if (state.sleepMinutes > 0) "${sleepHours}h ${sleepMins}m" else "0h 0m"

                SmallStatCard(
                    label = "Sleep",
                    value = sleepStr,
                    subValue = "of 8h target",
                    isGranted = state.isHealthConnectGranted,
                    onConnectClick = onConnectHealth,
                    modifier = Modifier.weight(1f)
                )
                SmallStatCard(
                    label = "Steps",
                    value = String.format(Locale.getDefault(), "%,d", state.stepsWalked),
                    subValue = "of ${state.stepsTarget / 1000}k goal",
                    isGranted = state.isHealthConnectGranted,
                    onConnectClick = onConnectHealth,
                    modifier = Modifier.weight(1f)
                )
                SmallStatCard(
                    label = "Heart Rate",
                    value = "${state.heartRatePeak} BPM",
                    subValue = "Daily Peak",
                    isGranted = state.isHealthConnectGranted,
                    onConnectClick = { navController.navigate(Screen.Analytics.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun ActiveSplitHeroCard(
    splitTitle: String,
    nextWorkoutName: String,
    exerciseCount: Int,
    onStartClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .border(1.dp, LimeGreen.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable { onStartClick() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE SPLIT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = LimeGreen
                    )
                }
                Surface(
                    color = LimeTintDark,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "$exerciseCount Exercises",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LimeDeepDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = nextWorkoutName,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = OffWhite
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = splitTitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color(0xFF121212)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Filled.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("View Workout Details ➔", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun EmptyWorkoutCard(onCreateClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No routines scheduled yet",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMutedDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            GhostButton(
                text = "+ Generate AI Split",
                onClick = onCreateClick,
                modifier = Modifier.height(42.dp)
            )
        }
    }
}

@Composable
fun LiveWorkoutCard(
    workout: Workout,
    index: Int,
    onClick: () -> Unit
) {
    val image = if (index % 2 == 0) R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78 else R.drawable._9e84ac439f8ba294d6f17a2f2a64cd1
    val durationStr = "${maxOf(30, workout.exercises.size * 8)} mins"
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .width(260.dp)
            .height(180.dp)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f)),
                            startY = 60f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryBadge(text = durationStr, colorTint = Color.Black.copy(alpha = 0.65f), textColor = LimeGreen)
                CategoryBadge(text = "${workout.exercises.size} Moves", colorTint = LimeTintDark.copy(alpha = 0.9f), textColor = LimeGreen)
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = workout.name, 
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), 
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tap to view details ➔",
                    style = MaterialTheme.typography.labelSmall,
                    color = LimeGreen
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenContent() {
    GymFitnessTheme {
        val sampleState = HomeState(
            userName = "Ashu Kenpachi",
            caloriesEaten = 1450f,
            caloriesTarget = 2200f,
            protein = 110f,
            proteinTarget = 140f,
            carbs = 180f,
            carbsTarget = 220f,
            fat = 50f,
            fatsTarget = 65f,
            stepsWalked = 8542,
            stepsTarget = 10000,
            sleepMinutes = 450,
            heartRatePeak = 128,
            currentStreak = 2,
            isHealthConnectGranted = true,
            weeklySteps = listOf(
                DayStepEntry(LocalDate.now().minusDays(6), "Mon", 6200),
                DayStepEntry(LocalDate.now().minusDays(5), "Tue", 7800),
                DayStepEntry(LocalDate.now().minusDays(4), "Wed", 8500),
                DayStepEntry(LocalDate.now().minusDays(3), "Thu", 5100),
                DayStepEntry(LocalDate.now().minusDays(2), "Fri", 9400),
                DayStepEntry(LocalDate.now().minusDays(1), "Sat", 11200),
                DayStepEntry(LocalDate.now(), "Sun", 8542)
            )
        )
        HomeScreenContent(
            state = sampleState,
            friendCode = "D06523",
            navController = rememberNavController()
        )
    }
}
