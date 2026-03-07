package com.example.gymfitness.presentation.screen.meals

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.ui.theme.BgDark

@Composable
fun MealScreen(navController: NavController) {

    var selectedTab by remember { mutableStateOf(0) }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF2BE070)
            ) {
                Icon(Icons.Default.Add, contentDescription = "")
            }
        },
        bottomBar = {
            BottomNavBar(
                navController = navController
            )
        },
        containerColor = BgDark
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Header()

            Spacer(modifier = Modifier.height(16.dp))

            DateSelector()

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar()

            Spacer(modifier = Modifier.height(20.dp))

            MealCard("Breakfast", "450 KCAL", "30g", "50g", "15g")

            Spacer(modifier = Modifier.height(12.dp))

            MealCard("Lunch", "620 KCAL", "45g", "65g", "22g")

            Spacer(modifier = Modifier.height(12.dp))

            MealCard("Snacks", "210 KCAL", "12g", "15g", "10g")

            Spacer(modifier = Modifier.height(24.dp))

            ProgressSection()

        }

    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Icon(Icons.Default.Menu, contentDescription = "", tint = Color.White)

        Text(
            text = "Daily Meals Log",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Icon(Icons.Default.DateRange, contentDescription = "", tint = Color.White)
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
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color(0xFF2BE070)
                        else Color(0xFF1C1F22)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    color = if (isSelected) Color.Black else Color.White,
                    textAlign = TextAlign.Center
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
        placeholder = { Text("Search food to add...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1C1F22),
            unfocusedContainerColor = Color(0xFF1C1F22),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun MealCard(
    title: String,
    calories: String,
    protein: String,
    carbs: String,
    fat: String
) {

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16191B)),
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(title, color = Color.White, fontWeight = FontWeight.Bold)

                Text(calories, color = Color(0xFF2BE070))

                Spacer(modifier = Modifier.height(6.dp))

                Row {

                    MacroChip("P", protein)
                    Spacer(modifier = Modifier.width(6.dp))
                    MacroChip("C", carbs)
                    Spacer(modifier = Modifier.width(6.dp))
                    MacroChip("F", fat)
                }
            }

            Icon(Icons.Default.KeyboardArrowRight, "", tint = Color.Gray)
        }
    }
}

@Composable
fun MacroChip(label: String, value: String) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A2D30))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {

        Text(
            text = "$label: $value",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProgressSection() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F2F1D))
            .padding(16.dp)
    ) {

        Text("DAILY PROGRESS", color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "1,280 / 2,400 kcal",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = 0.53f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = Color(0xFF2BE070),
            trackColor = Color.DarkGray
        )
    }
}