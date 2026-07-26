package com.example.gymfitness.presentation.screen.workoutdetail

import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.BaseCard
import com.example.gymfitness.presentation.components.CategoryBadge
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    navController: NavController, 
    workoutId: String?,
    viewModel: com.example.gymfitness.presentation.viewmodel.WorkoutViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val deviceId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }
    
    LaunchedEffect(workoutId) {
        workoutId?.toLongOrNull()?.let { id ->
            viewModel.fetchWorkoutDetails(deviceId, id)
        }
    }

    val currentWorkout by viewModel.currentWorkout.collectAsState()
    val workoutTitle = currentWorkout?.name ?: "Workout Detail"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(workoutTitle, style = Typography.titleLarge, color = InkBlack) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(start = 8.dp).background(CardSurface, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = InkBlack)
                    }
                },
                actions = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("FINISH", color = SunsetOrange, style = Typography.labelMedium.copy(fontWeight = FontWeight.Black))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = PageBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Live Interactive Timer
            WorkoutTimerHeader()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Badge
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Exercises", style = Typography.titleLarge, color = InkBlack)
                        CategoryBadge("Total: ${currentWorkout?.exercises?.size ?: 0}")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                items(currentWorkout?.exercises ?: emptyList()) { exercise ->
                    ExerciseLogCard(exercise = exercise)
                }
            }
        }
    }
}

@Composable
fun WorkoutTimerHeader() {
    var seconds by remember { mutableIntStateOf(0) }
    
    // Live Timer Effect
    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            seconds++
        }
    }

    val timeString = remember(seconds) {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        String.format("%02d:%02d:%02d", h, m, s)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("WORKOUT ELAPSED", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
            Text(timeString, style = Typography.displayLarge, color = SunsetOrange)
        }
    }
}

@Composable
fun ExerciseLogCard(exercise: com.example.gymfitness.domain.models.Exercise) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    BaseCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.animateContentSize()
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.size(8.dp, 24.dp).clip(RoundedCornerShape(4.dp)).background(SunsetOrange))
                Spacer(Modifier.width(12.dp))
                Text(exercise.name, style = Typography.titleLarge, color = InkBlack, modifier = Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextMuted, modifier = Modifier.rotate(rotation))
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SET", modifier = Modifier.weight(1f), style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    Text("KG", modifier = Modifier.weight(2f), style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    Text("REPS", modifier = Modifier.weight(2f), style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(8.dp))

                val numSets = if (exercise.sets > 0) exercise.sets else 3 // fallback if 0 sets
                for (i in 1..numSets) {
                    SetInputRow(
                        setNum = i, 
                        initialWeight = if (exercise.weight > 0) exercise.weight.toString() else "",
                        initialReps = if (exercise.reps > 0) exercise.reps.toString() else ""
                    )
                }
            }
        }
    }
}

@Composable
fun SetInputRow(setNum: Int, initialWeight: String = "", initialReps: String = "") {
    var isCompleted by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf(initialWeight) }
    var reps by remember { mutableStateOf(initialReps) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCompleted) OrangeTint else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$setNum", modifier = Modifier.weight(1f), style = Typography.titleMedium.copy(fontWeight = FontWeight.Black), color = if(isCompleted) SunsetOrange else InkBlack)

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            placeholder = { Text("0", color = TextMutedDark) },
            modifier = Modifier.weight(2f).height(50.dp).padding(end = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceAltDark,
                unfocusedContainerColor = SurfaceAltDark,
                focusedTextColor = OffWhite,
                unfocusedTextColor = OffWhite,
                focusedBorderColor = LimeGreen,
                unfocusedBorderColor = StrokeDark
            ),
            shape = RoundedCornerShape(10.dp),
            textStyle = Typography.bodyMedium
        )

        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            placeholder = { Text("0", color = TextMutedDark) },
            modifier = Modifier.weight(2f).height(50.dp).padding(end = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceAltDark,
                unfocusedContainerColor = SurfaceAltDark,
                focusedTextColor = OffWhite,
                unfocusedTextColor = OffWhite,
                focusedBorderColor = LimeGreen,
                unfocusedBorderColor = StrokeDark
            ),
            shape = RoundedCornerShape(10.dp),
            textStyle = Typography.bodyMedium
        )

        IconButton(
            onClick = { isCompleted = !isCompleted },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isCompleted) LimeGreen else StrokeDark,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}