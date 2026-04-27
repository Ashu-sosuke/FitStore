package com.example.gymfitness.presentation.screen.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.viewmodel.HomeViewModel
import com.example.gymfitness.ui.theme.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BgLight, // Soft White background
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Updated for White Theme
            TopHeaderSection(userName = "Alex")

            Spacer(Modifier.height(28.dp))

            // Modern Gauge with Blue Accents
            MainAnimatedCalorieCard(
                calories = state.caloriesEaten,
                target = state.caloriesTarget
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Nutrition Breakdown",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Macro Grid with White Surface Cards
            MacroGridSection(state)

            Spacer(Modifier.height(32.dp))

            // Premium Blue Action Banner
            WorkoutActionBanner()

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun MainAnimatedCalorieCard(calories: Float, target: Float) {
    val progress = if (target > 0) (calories / target).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    val animatedCount by animateIntAsState(targetValue = calories.toInt(), animationSpec = tween(1400))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Professional depth
    ) {
        Row(
            modifier = Modifier.padding(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(145.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = BorderGray, // Muted track
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.linearGradient(listOf(AccentBlue.copy(0.7f), AccentBlue)),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedCount",
                        color = TextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("kcal", color = TextSecondary, fontSize = 14.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                StatLabel("Target", "${target.toInt()} kcal", AccentBlue)
                StatLabel("Left", "${(target - calories).toInt()} kcal", TextSecondary)
            }
        }
    }
}

@Composable
fun MacroDetailCard(
    label: String,
    value: String,
    target: String,
    progress: Float,
    color: Color,
    icon: Int,
    modifier: Modifier = Modifier
) {
    val lineProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(1000))

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BorderGray) // Clean border
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(" / $target", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 3.dp, start = 4.dp))
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { lineProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = BorderGray,
            )
        }
    }
}

@Composable
fun TopHeaderSection(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Keep it up,", color = TextSecondary, fontSize = 14.sp)
            Text("Hello $userName! 👋", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        }
        IconButton(
            onClick = { },
            modifier = Modifier.background(SurfaceWhite, CircleShape).border(1.dp, BorderGray, CircleShape)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TextPrimary)
        }
    }
}

@Composable
fun WorkoutActionBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(AccentBlue, Color(0xFF00D1FF))))
            .clickable { }
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ready to sweat?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("Start your daily session", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            Icon(
                painter = painterResource(R.drawable.gym_1025878), // Ensure this exists
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(45.dp)
            )
        }
    }
}

@Composable
fun MacroGridSection(state: com.example.gymfitness.presentation.state.HomeState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Protein (Full Width for emphasis)
        MacroDetailCard(
            label = "Protein",
            value = "${state.protein.toInt()}g",
            target = "${state.proteinTarget.toInt()}g", // Using dynamic target from state
            progress = state.protein / state.proteinTarget,
            color = AccentBlue, // Your new modern accent
            icon = R.drawable.baseline_local_fire_department_24,
            modifier = Modifier.fillMaxWidth()
        )

        // Carbs and Fats (Side by Side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MacroDetailCard(
                label = "Carbs",
                value = "${state.carbs.toInt()}g",
                target = "${state.carbsTarget.toInt()}g",
                progress = state.carbs / state.carbsTarget,
                color = Color(0xFFFF9800), // Energetic Orange
                icon = R.drawable.baseline_local_fire_department_24,
                modifier = Modifier.weight(1f)
            )
            MacroDetailCard(
                label = "Fats",
                value = "${state.fat.toInt()}g",
                target = "${state.fatTarget.toInt()}g",
                progress = state.fat / state.fatTarget,
                color = Color(0xFF00D1FF), // Sleek Cyan
                icon = R.drawable.baseline_local_fire_department_24,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun StatLabel(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}