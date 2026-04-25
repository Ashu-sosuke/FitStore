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
        containerColor = BgDark,
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

            // Header with more "breathable" spacing
            TopHeaderSection(userName = "Alex")

            Spacer(Modifier.height(28.dp))

            // Main Animated Calorie Card
            MainAnimatedCalorieCard(
                calories = state.caloriesEaten,
                target = state.caloriesTarget
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Nutrition Breakdown",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Grid with improved spacing and internal padding
            MacroGridSection(state)

            Spacer(Modifier.height(32.dp))

            // Large Action Banner
            WorkoutActionBanner()

            Spacer(Modifier.height(24.dp)) // Padding for the floating bottom bar
        }
    }
}

@Composable
fun MainAnimatedCalorieCard(calories: Float, target: Float) {
    // Progress sweep animation
    val progress = if (target > 0) (calories / target).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "gauge"
    )

    // Number counting animation
    val animatedCount by animateIntAsState(
        targetValue = calories.toInt(),
        animationSpec = tween(1400),
        label = "count"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) // Subtle border for definition
    ) {
        Row(
            modifier = Modifier.padding(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(145.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track
                    drawArc(
                        color = Color.White.copy(alpha = 0.05f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Animated Progress
                    drawArc(
                        brush = Brush.linearGradient(listOf(GreenMuted, GreenPrimary)),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedCount",
                        color = TextWhite,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("kcal", color = TextGray, fontSize = 14.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                StatLabel("Target", "${target.toInt()} kcal", GreenPrimary)
                StatLabel("Left", "${(target - calories).toInt()} kcal", TextGray)
            }
        }
    }
}

@Composable
fun MacroGridSection(state: com.example.gymfitness.presentation.state.HomeState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MacroDetailCard(
            label = "Protein",
            value = "${state.protein.toInt()}g",
            target = "180g",
            progress = state.protein / 180f,
            color = GreenPrimary,
            icon = R.drawable.baseline_local_fire_department_24,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MacroDetailCard(
                label = "Carbs",
                value = "${state.carbs.toInt()}g",
                target = "220g",
                progress = state.carbs / 220f,
                color = Color(0xFFFF9800),
                icon = R.drawable.baseline_local_fire_department_24,
                modifier = Modifier.weight(1f)
            )
            MacroDetailCard(
                label = "Fats",
                value = "${state.fat.toInt()}g",
                target = "70g",
                progress = state.fat / 70f,
                color = Color(0xFF00BCD4),
                icon = R.drawable.baseline_local_fire_department_24,
                modifier = Modifier.weight(1f)
            )
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
    // Local animation for progress lines
    val lineProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000, delayMillis = 500)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(" / $target", color = TextGray, fontSize = 13.sp, modifier = Modifier.padding(bottom = 3.dp, start = 4.dp))
            }
            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { lineProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = color,
                trackColor = Color.Black.copy(alpha = 0.3f),
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
            Text("Keep it up,", color = TextGray, fontSize = 14.sp)
            Text("Hello $userName! 🔥", color = TextWhite, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        }
        IconButton(
            onClick = { },
            modifier = Modifier.background(CardDark, CircleShape)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = TextWhite)
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
            .background(Brush.horizontalGradient(listOf(GreenMuted, GreenPrimary)))
            .clickable { }
            .padding(24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Ready to sweat?", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text("Start your daily workout session", color = Color.Black.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            Icon(
                painter = painterResource(R.drawable.gym_1025878),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(45.dp)
            )
        }
    }
}

@Composable
fun StatLabel(label: String, value: String, color: Color) {
    Column {
        Text(label, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}