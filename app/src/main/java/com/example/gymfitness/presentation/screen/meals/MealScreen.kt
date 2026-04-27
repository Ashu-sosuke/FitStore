package com.example.gymfitness.presentation.screen.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.ui.theme.*

@Composable
fun MealScreen(navController: NavController) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
            }
        },
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = BgLight
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Header()

            Spacer(modifier = Modifier.height(24.dp))

            DateSelector()

            Spacer(modifier = Modifier.height(24.dp))

            SearchBar()

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Today's Meals",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            MealCard("Breakfast", "450 kcal", "30g", "50g", "15g")
            Spacer(modifier = Modifier.height(12.dp))
            MealCard("Lunch", "620 kcal", "45g", "65g", "22g")
            Spacer(modifier = Modifier.height(12.dp))
            MealCard("Snacks", "210 kcal", "12g", "15g", "10g")

            Spacer(modifier = Modifier.height(32.dp))

            ProgressSection()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {},
            modifier = Modifier.background(SurfaceWhite, CircleShape).border(1.dp, BorderGray, CircleShape)
        ) {
            Icon(Icons.Default.Menu, contentDescription = null, tint = TextPrimary)
        }

        Text(
            text = "Nutrition Log",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
        )

        IconButton(
            onClick = {},
            modifier = Modifier.background(SurfaceWhite, CircleShape).border(1.dp, BorderGray, CircleShape)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = TextPrimary)
        }
    }
}

@Composable
fun DateSelector() {
    val days = listOf("MON\n14", "TUE\n15", "WED\n16", "THU\n17", "FRI\n18")

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = index == 2
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(75.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) AccentBlue else SurfaceWhite)
                    .then(if (!isSelected) Modifier.border(1.dp, BorderGray, RoundedCornerShape(16.dp)) else Modifier)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) Color.White else TextSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun SearchBar() {
    TextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search meals...", color = TextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary
        )
    )
}

@Composable
fun MealCard(title: String, calories: String, protein: String, carbs: String, fat: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🍽️", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(calories, color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    MacroChip("P", protein, Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(6.dp))
                    MacroChip("C", carbs, Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(6.dp))
                    MacroChip("F", fat, Color(0xFF00D1FF))
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "", tint = BorderGray)
        }
    }
}

@Composable
fun MacroChip(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(text = "$label $value", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProgressSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(AccentBlue, Color(0xFF00D1FF))))
            .padding(24.dp)
    ) {
        Text("DAILY PROGRESS", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("1,280", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text(" / 2,400 kcal", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { 0.53f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}