package com.example.gymfitness.presentation.screen.workoutdetail

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gymfitness.domain.models.Exercise
import com.example.gymfitness.domain.usecase.workout.GenerateWorkoutPlanUseCase
import com.example.gymfitness.presentation.components.CategoryBadge
import com.example.gymfitness.presentation.viewmodel.WorkoutViewModel
import com.example.gymfitness.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController, 
    workoutId: String?,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId) {
        workoutId?.toLongOrNull()?.let { id ->
            viewModel.fetchWorkoutDetails(viewModel.deviceId, id)
        }
    }

    val currentWorkout by viewModel.currentWorkout.collectAsState()
    val workoutTitle = currentWorkout?.name ?: "Workout Details"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = workoutTitle,
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = OffWhite,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(SurfaceDark, CircleShape)
                            .border(1.dp, StrokeDark, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OffWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Plan",
                            tint = Color(0xFFEF4444)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = PageBg)
            )
        },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBg)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Plan Summary Card
                item {
                    val exerciseList = currentWorkout?.exercises ?: emptyList()
                    val totalSets = exerciseList.sumOf { it.sets }
                    val estDuration = maxOf(30, exerciseList.size * 8)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StrokeDark, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentWorkout?.splitType?.displayName ?: "CUSTOM SPLIT",
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = LimeGreen
                                )
                                CategoryBadge(
                                    text = "${exerciseList.size} Exercises",
                                    colorTint = LimeTintDark,
                                    textColor = LimeGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = workoutTitle,
                                style = Typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Black),
                                color = OffWhite
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = StrokeDark)
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("TOTAL SETS", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMutedDark)
                                    Text("$totalSets sets", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                                }
                                Column {
                                    Text("EST. TIME", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMutedDark)
                                    Text("$estDuration mins", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = OffWhite)
                                }
                                Column {
                                    Text("TYPE", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMutedDark)
                                    Text("Hypertrophy", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LimeDeepDark)
                                }
                            }
                        }
                    }
                }

                // Exercises List Section
                item {
                    Text(
                        text = "Workout Movements",
                        style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OffWhite
                    )
                }

                itemsIndexed(currentWorkout?.exercises ?: emptyList()) { index, exercise ->
                    ExerciseViewerCard(index = index + 1, exercise = exercise)
                }

                item {
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Delete Workout Plan?", style = Typography.titleLarge, color = OffWhite)
            },
            text = {
                Text(
                    "Are you sure you want to remove '$workoutTitle' from your library?",
                    style = Typography.bodyMedium,
                    color = TextMutedDark
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    workoutId?.toLongOrNull()?.let { wid ->
                        viewModel.deleteWorkout(wid) {
                            Toast.makeText(context, "Workout plan removed", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    }
                }) {
                    Text("DELETE", color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = OffWhite)
                }
            }
        )
    }
}

@Composable
fun ExerciseViewerCard(index: Int, exercise: Exercise) {
    val context = LocalContext.current
    val fullGifUrl = remember(exercise.name) {
        val rawUrl = GenerateWorkoutPlanUseCase.resolveExerciseGif(exercise.name)
        if (rawUrl.startsWith("/")) "https://pulse-backend-6srs.onrender.com$rawUrl" else rawUrl
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StrokeDark, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Animation GIF
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceAltDark),
                contentAlignment = Alignment.Center
            ) {
                if (fullGifUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
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
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$index. ${exercise.name}",
                    fontWeight = FontWeight.Bold,
                    color = OffWhite,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${exercise.sets} sets × ${exercise.reps} reps",
                        color = LimeGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (exercise.weight > 0.0) {
                        Text(
                            text = "• ${exercise.weight} kg",
                            color = TextMutedDark,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Target: ${exercise.primaryMuscle.displayName}",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}