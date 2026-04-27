package com.example.gymfitness.presentation.screen.workouts

import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.ui.theme.*

@Composable
fun WorkoutScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = BgLight // Pure White/Off-white
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(20.dp)
        ) {
            WorkoutHeader()

            Spacer(modifier = Modifier.height(24.dp))

            CreateWorkoutButton()

            Spacer(modifier = Modifier.height(32.dp))

            RecentHistoryHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // Professional Workout Cards
            WorkoutCard(
                title = "Upper Body A",
                date = "Oct 24, 2026",
                duration = "45 min",
                volume = "13,450 kg",
                image = R.drawable._9e84ac439f8ba294d6f17a2f2a64cd1 // Using your male asset
            )

            Spacer(modifier = Modifier.height(20.dp))

            WorkoutCard(
                title = "Leg Day - Power",
                date = "Oct 22, 2026",
                duration = "62 min",
                volume = "18,900 kg",
                image = R.drawable._f6b749fe68be3833acc6ee7a151ffd1 // Using your female asset
            )

            Spacer(modifier = Modifier.height(20.dp))

            WorkoutCard(
                title = "Full Body Core",
                date = "Oct 20, 2026",
                duration = "30 min",
                volume = "9,400 kg",
                image = R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78
            )
        }
    }
}

@Composable
fun WorkoutHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "My Training",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Workouts",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        IconButton(
            onClick = { },
            modifier = Modifier.background(SurfaceWhite, CircleShape).border(1.dp, BorderGray, CircleShape)
        ) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = TextPrimary)
        }
    }
}

@Composable
fun CreateWorkoutButton() {
    Button(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Log New Workout",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun RecentHistoryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Recent History",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "See All",
            color = AccentBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { }
        )
    }
}

@Composable
fun WorkoutCard(
    title: String,
    date: String,
    duration: String,
    volume: String,
    image: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Glassmorphism Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("COMPLETED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Bottom Gradient over image for text contrast
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f)))
                ))
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WorkoutDetailItem(R.drawable.outline_calendar_month_24, date)
                    WorkoutDetailItem(R.drawable.baseline_timer_24, duration)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total Volume: $volume",
                    color = AccentBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WorkoutDetailItem(@DrawableRes icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)) {
        Icon(
            painter = painterResource(id = icon),            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = TextSecondary, fontSize = 13.sp)
    }
}