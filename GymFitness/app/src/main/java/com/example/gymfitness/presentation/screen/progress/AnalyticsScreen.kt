package com.example.gymfitness.presentation.screen.progress

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.BaseCard
import com.example.gymfitness.presentation.components.HeartRateLineChart
import com.example.gymfitness.presentation.components.StepsBarChart
import com.example.gymfitness.ui.theme.*
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val today = LocalDate.now()
    val days = remember { (0..6).map { today.minusDays(it.toLong()) }.reversed() }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recent Reports", style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = OffWhite) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp).background(SurfaceDark, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OffWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Horizontal Scrollable Date Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(days) { date ->
                    val isToday = date == selectedDate
                    val bgColor by animateColorAsState(
                        targetValue = if (isToday) LimeGreen else SurfaceDark,
                        label = "date_bg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isToday) Color(0xFF121212) else OffWhite,
                        label = "date_text"
                    )

                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .clickable { selectedDate = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfWeek.name.take(3),
                                style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isToday) Color(0xFF121212) else TextMutedDark
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = Typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = textColor
                            )
                        }
                    }
                }
            }

            // 2-Column Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Calories
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔥", fontSize = 16.sp)
                            Text("Calories", style = Typography.bodySmall, color = TextMutedDark)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("420", style = Typography.displayLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = OffWhite)
                            Spacer(Modifier.width(4.dp))
                            Text("kcal", style = Typography.bodySmall, color = TextMutedDark)
                        }
                    }
                }

                // Total Distance
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📍", fontSize = 16.sp)
                            Text("Distance", style = Typography.bodySmall, color = TextMutedDark)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("4.8", style = Typography.displayLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = OffWhite)
                            Spacer(Modifier.width(4.dp))
                            Text("km", style = Typography.bodySmall, color = TextMutedDark)
                        }
                    }
                }
            }

            // Steps Card with StepsBarChart
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DAILY STEPS", style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextMutedDark)
                        Text("Active Progress", style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LimeTintDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                StepsBarChart(stepsData = listOf(8400, 6200, 11200, 7500, 9300, 5200, 10250))
            }

            // Heart Rate Card with HeartRateLineChart
            BaseCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("HEART RATE", style = Typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextMutedDark)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("138", style = Typography.displayLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = OffWhite)
                            Text("bpm peak", style = Typography.bodySmall, color = TextMutedDark)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B1012)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                HeartRateLineChart(heartRateData = listOf(65, 82, 110, 95, 120, 85, 90, 138, 72, 80))
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
