package com.example.gymfitness.presentation.screen.home

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gymfitness.R
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
        HealthPermission.getReadPermission(SleepSessionRecord::class)
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

            // Refactored Daily Progress Card
            DailyProgressCard(
                caloriesEaten = state.caloriesEaten.toInt(),
                caloriesTarget = state.caloriesTarget.toInt(),
                protein = state.protein,
                proteinTarget = state.proteinTarget,
                carbs = state.carbs,
                carbsTarget = state.carbsTarget,
                fat = state.fat,
                fatsTarget = state.fatsTarget,
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
                    // Top Row: Steps count + Percentage Badge
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

                    // Progress Bar
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

                    Divider(color = StrokeDark)

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

            // Workout section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Workouts",
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

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(3) { index ->
                    WorkoutCard(index)
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // Quick Stats / Today's Activity
            Text(
                text = "Today's Activity",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OffWhite,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    subValue = "of ${state.stepsTarget / 1000}k target",
                    isGranted = state.isHealthConnectGranted,
                    onConnectClick = onConnectHealth,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(100.dp))
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
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No upcoming workouts scheduled",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMutedDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            GhostButton(
                text = "+ Create Plan",
                onClick = onCreateClick,
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Composable
fun WorkoutCard(index: Int) {
    val title = if (index % 2 == 0) "Chest & Triceps" else "Legs & Core"
    val duration = if (index % 2 == 0) "45 mins" else "60 mins"
    val image = if (index % 2 == 0) R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78 else R.drawable._9e84ac439f8ba294d6f17a2f2a64cd1
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .width(260.dp)
            .height(180.dp)
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
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 80f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CategoryBadge(text = duration, colorTint = Color.Black.copy(alpha = 0.6f), textColor = LimeGreen)
                CategoryBadge(text = "Advanced", colorTint = LimeTintDark.copy(alpha = 0.9f), textColor = LimeGreen)
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), 
                    color = Color.White
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
            sleepMinutes = 450,
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
