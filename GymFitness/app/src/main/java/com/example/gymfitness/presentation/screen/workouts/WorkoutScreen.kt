package com.example.gymfitness.presentation.screen.workouts

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gymfitness.R
import com.example.gymfitness.domain.models.SplitPlan
import com.example.gymfitness.domain.models.SplitType
import com.example.gymfitness.domain.models.Workout
import com.example.gymfitness.presentation.components.*
import com.example.gymfitness.presentation.componts.BottomNavBar
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.WorkoutViewModel
import com.example.gymfitness.ui.theme.*
import java.util.Locale

data class WorkoutRoutine(
    val id: String,
    val title: String,
    val date: String,
    val duration: String,
    val volume: String,
    val splitType: SplitType,
    @DrawableRes val imageRes: Int
)

@Composable
fun WorkoutScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val filteredWorkouts by viewModel.filteredWorkouts.collectAsState()
    val allWorkouts by viewModel.workouts.collectAsState()
    val selectedSplit by viewModel.selectedSplit.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recommendedSplit by viewModel.recommendedSplit.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    WorkoutScreenContent(
        workouts = filteredWorkouts,
        totalWorkoutCount = allWorkouts.size,
        selectedSplit = selectedSplit,
        searchQuery = searchQuery,
        recommendedSplit = recommendedSplit,
        isLoading = isLoading,
        onSplitSelected = viewModel::onSplitSelected,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onLogWorkoutClick = { navController.navigate(Screen.CreatePlan.route) },
        onWorkoutClick = { id -> navController.navigate(Screen.WorkoutDetail.createRoute(id)) },
        navController = navController
    )
}

@Composable
fun WorkoutScreenContent(
    workouts: List<Workout>,
    totalWorkoutCount: Int,
    selectedSplit: SplitType,
    searchQuery: String,
    recommendedSplit: SplitPlan?,
    isLoading: Boolean = false,
    onSplitSelected: (SplitType) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onLogWorkoutClick: () -> Unit = {},
    onWorkoutClick: (String) -> Unit = {},
    navController: NavController = rememberNavController()
) {
    val routines = workouts.map { workout ->
        WorkoutRoutine(
            id = workout.id ?: "0",
            title = workout.name,
            date = workout.date.ifBlank { "Today" },
            duration = "45 min",
            volume = String.format(Locale.getDefault(), "%,.0f kg", workout.totalVolume),
            splitType = workout.splitType,
            imageRes = R.drawable._9e84ac439f8ba294d6f17a2f2a64cd1
        )
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = PageBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp).statusBarsPadding())
            WorkoutHeader()

            Spacer(modifier = Modifier.height(20.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search workouts, exercises..."
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Split Type Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SplitType.values()) { split ->
                    WorkoutFilterChip(
                        text = "${split.categoryIcon} ${split.displayName}",
                        isSelected = split == selectedSplit,
                        onClick = { onSplitSelected(split) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // "Recommended For You" Guided Split Card
            recommendedSplit?.let { recommendation ->
                RecommendedSplitCard(
                    recommendation = recommendation,
                    onStartRoutineClick = onLogWorkoutClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            PrimaryButton(
                text = "Log Custom Workout",
                onClick = onLogWorkoutClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))
            RecentHistoryHeader()
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LimeGreen)
                }
            } else if (routines.isEmpty()) {
                ContextualEmptyState(
                    totalWorkoutCount = totalWorkoutCount,
                    selectedSplit = selectedSplit,
                    searchQuery = searchQuery,
                    recommendedSplit = recommendedSplit,
                    onStartWorkoutClick = onLogWorkoutClick
                )
            } else {
                routines.forEach { routine ->
                    WorkoutCard(
                        title = routine.title,
                        date = routine.date,
                        duration = routine.duration,
                        volume = routine.volume,
                        splitTypeLabel = routine.splitType.displayName,
                        image = routine.imageRes,
                        onClick = { onWorkoutClick(routine.id) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RecommendedSplitCard(
    recommendation: SplitPlan,
    onStartRoutineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, LimeGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = LimeGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "RECOMMENDED FOR YOU",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = LimeGreen
                    )
                }

                Surface(
                    color = LimeTintDark,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${recommendation.daysPerWeek} Days/Wk",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LimeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = recommendation.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = OffWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark
            )

            Spacer(modifier = Modifier.height(16.dp))
            GhostButton(
                text = "Start ${recommendation.splitType.displayName} Routine",
                onClick = onStartRoutineClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            )
        }
    }
}

@Composable
fun ContextualEmptyState(
    totalWorkoutCount: Int,
    selectedSplit: SplitType,
    searchQuery: String,
    recommendedSplit: SplitPlan?,
    onStartWorkoutClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val titleText = when {
                searchQuery.isNotBlank() -> "No workouts match '$searchQuery'"
                selectedSplit != SplitType.ALL -> "No ${selectedSplit.displayName} workouts logged yet"
                totalWorkoutCount == 0 -> "Start Your Fitness Journey!"
                else -> "No workouts found"
            }

            val bodyText = when {
                searchQuery.isNotBlank() -> "Try searching for a different exercise or clear your filter."
                selectedSplit != SplitType.ALL -> "Log your first ${selectedSplit.displayName} session today!"
                else -> "Guided splits get you the fastest results. Pick a split below or create a custom plan."
            }

            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = OffWhite,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = if (totalWorkoutCount == 0 && recommendedSplit != null) "Start ${recommendedSplit.splitType.displayName} Workout" else "+ Log Workout",
                onClick = onStartWorkoutClick,
                modifier = Modifier.height(44.dp)
            )
        }
    }
}

@Composable
fun WorkoutCard(
    title: String,
    date: String,
    duration: String,
    volume: String,
    splitTypeLabel: String,
    image: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier
            .fillMaxWidth()
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
            // Scrim overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 70f
                        )
                    )
            )

            // Top-Left Badges
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryBadge(text = duration, colorTint = Color.Black.copy(alpha = 0.6f), textColor = LimeGreen)
                CategoryBadge(text = splitTypeLabel.uppercase(), colorTint = LimeTintDark.copy(alpha = 0.9f), textColor = LimeGreen)
            }

            // Title Bottom-Left
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$date  •  Volume: $volume",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedDark
                )
            }
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
                text = "Training History",
                style = MaterialTheme.typography.bodyLarge,
                color = TextMutedDark
            )
            Text(
                text = "Workouts",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                color = OffWhite
            )
        }

        Surface(
            shape = CircleShape,
            color = SurfaceAltDark,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = "User Account",
                tint = LimeGreen,
                modifier = Modifier.padding(12.dp)
            )
        }
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
            "Recent Activity",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = OffWhite
        )
        Text(
            "See All",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = LimeGreen,
            modifier = Modifier.clickable { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWorkoutScreenContent() {
    GymFitnessTheme {
        val sampleWorkouts = listOf(
            Workout(
                id = "1",
                deviceId = "dev1",
                name = "Push Hypertrophy",
                exercises = emptyList(),
                totalVolume = 4850.0,
                date = "Yesterday",
                splitType = SplitType.PUSH
            ),
            Workout(
                id = "2",
                deviceId = "dev1",
                name = "Leg Day Heavy",
                exercises = emptyList(),
                totalVolume = 6200.0,
                date = "3 days ago",
                splitType = SplitType.LEGS
            )
        )

        val sampleRecommendation = SplitPlan(
            splitType = SplitType.UPPER,
            title = "Upper / Lower Split",
            daysPerWeek = 4,
            description = "Best frequency-to-recovery ratio for intermediates. Trains each muscle group ~2x/week.",
            recommendedExercises = emptyList()
        )

        WorkoutScreenContent(
            workouts = sampleWorkouts,
            totalWorkoutCount = 2,
            selectedSplit = SplitType.ALL,
            searchQuery = "",
            recommendedSplit = sampleRecommendation
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyWorkoutScreenContent() {
    GymFitnessTheme {
        WorkoutScreenContent(
            workouts = emptyList(),
            totalWorkoutCount = 0,
            selectedSplit = SplitType.ALL,
            searchQuery = "",
            recommendedSplit = SplitPlan(
                splitType = SplitType.FULL_BODY,
                title = "Full Body Split",
                daysPerWeek = 3,
                description = "Hits every major muscle group each session. Ideal for consistency & beginners.",
                recommendedExercises = emptyList()
            )
        )
    }
}