package com.example.gymfitness.presentation.screen.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.domain.models.SplitType
import com.example.gymfitness.domain.models.Workout
import com.example.gymfitness.presentation.components.CategoryBadge
import com.example.gymfitness.presentation.components.GhostButton
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.WorkoutViewModel
import com.example.gymfitness.ui.theme.*

@Composable
fun WorkoutScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val filteredWorkouts by viewModel.filteredWorkouts.collectAsState()
    val allWorkouts by viewModel.workouts.collectAsState()
    val selectedSplit by viewModel.selectedSplit.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Workout Library",
                            fontWeight = FontWeight.Black,
                            color = OffWhite,
                            fontSize = 26.sp
                        )
                        Text(
                            text = "${allWorkouts.size} Saved Plans & Splits",
                            color = TextMutedDark,
                            fontSize = 13.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { navController.navigate(Screen.PlanGenerator.route) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(LimeTintDark)
                                .border(1.dp, LimeGreen.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "AI Split", tint = LimeGreen, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = { navController.navigate(Screen.CreatePlan.route) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceDark)
                                .border(1.dp, StrokeDark, CircleShape)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Create Plan", tint = OffWhite, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // Quick Create / Generate Action Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Create or Generate Split", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 15.sp)
                            Text("Build multiple custom splits & routines", color = TextMutedDark, fontSize = 12.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { navController.navigate(Screen.CreatePlan.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color(0xFF121212)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("+ New Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search plans or exercises...", color = TextMutedDark) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextMutedDark) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMutedDark)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = StrokeDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark,
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite
                    ),
                    singleLine = true
                )
            }

            // Split Type Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SplitType.values()) { split ->
                        val isSel = selectedSplit == split
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.onSplitSelected(split) },
                            label = { Text(split.displayName, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LimeGreen,
                                selectedLabelColor = Color(0xFF121212),
                                containerColor = SurfaceDark,
                                labelColor = OffWhite
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Workout Plans List
            if (filteredWorkouts.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No workout plans found", fontWeight = FontWeight.Bold, color = OffWhite, fontSize = 16.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Create a custom plan or generate an AI split", color = TextMutedDark, fontSize = 12.sp)
                            Spacer(Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { navController.navigate(Screen.CreatePlan.route) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color(0xFF121212)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Create Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { navController.navigate(Screen.PlanGenerator.route) },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("⚡ AI Split", color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredWorkouts) { workout ->
                    SavedWorkoutCard(
                        workout = workout,
                        onClick = {
                            workout.id?.let { wid ->
                                navController.navigate(Screen.WorkoutDetail.createRoute(wid))
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SavedWorkoutCard(
    workout: Workout,
    onClick: () -> Unit
) {
    val durationStr = "${maxOf(30, workout.exercises.size * 8)} mins"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StrokeDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.name,
                    style = Typography.titleLarge.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = OffWhite,
                    modifier = Modifier.weight(1f)
                )
                CategoryBadge(
                    text = durationStr,
                    colorTint = LimeTintDark,
                    textColor = LimeGreen
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(
                        text = "${workout.exercises.size} Exercises",
                        colorTint = SurfaceAltDark,
                        textColor = OffWhite
                    )
                    CategoryBadge(
                        text = workout.splitType.displayName,
                        colorTint = SurfaceAltDark,
                        textColor = TextMutedDark
                    )
                }

                Text(
                    text = "View Details ➔",
                    color = LimeGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}