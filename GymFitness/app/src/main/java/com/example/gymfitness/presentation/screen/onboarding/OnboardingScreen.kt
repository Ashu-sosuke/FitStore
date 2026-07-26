package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.components.PrimaryInputField
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(navController: NavController, viewModel: UserViewModel = hiltViewModel()) {
    
    var showCelebration by remember { mutableStateOf(false) }

    val isEnabled = if (viewModel.currentStep == 1) {
        viewModel.name.isNotEmpty() && viewModel.age.isNotEmpty() &&
                viewModel.weight.isNotEmpty() && viewModel.height.isNotEmpty()
    } else true

    if (showCelebration) {
        CelebrationScreen {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (viewModel.currentStep > 0) {
                    IconButton(onClick = { viewModel.previousStep() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = InkBlack)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Step Indicator (3 orange dots)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        val width by animateDpAsState(
                            targetValue = if (index == viewModel.currentStep) 24.dp else 8.dp,
                            label = "stepIndicator"
                        )
                        val color = if (index <= viewModel.currentStep) SunsetOrange else StrokeSoft
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
                PrimaryButton(
                    text = if (viewModel.currentStep < 2) "Continue" else "Create My Plan",
                    onClick = {
                        if (viewModel.currentStep < 2) {
                            viewModel.nextStep()
                        } else {
                            viewModel.saveUser {
                                showCelebration = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = viewModel.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                }, label = "steps"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    when (step) {
                        0 -> GenderStep(viewModel)
                        1 -> DetailsStep(viewModel)
                        2 -> GoalStep(viewModel)
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun GenderStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Select Gender", color = InkBlack, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Personalized for your biology.", color = TextMuted, style = Typography.bodyLarge)
        Spacer(Modifier.height(48.dp))
        
        // Horizontal Pill toggle
        val genders = listOf("Male", "Female", "Other")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            genders.forEach { gender ->
                val isSelected = viewModel.gender == gender
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.gender = gender },
                    color = if (isSelected) SunsetOrange else SurfaceAlt,
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = gender,
                            color = if (isSelected) Color.White else InkBlack,
                            style = Typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("About You", color = InkBlack, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("Help us customize your fitness plan.", color = TextMuted, style = Typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        
        PrimaryInputField(viewModel.name, { viewModel.name = it }, "Full Name")
        Spacer(Modifier.height(16.dp))
        PrimaryInputField(viewModel.age, { viewModel.age = it }, "Age")
        Spacer(Modifier.height(16.dp))
        PrimaryInputField(viewModel.weight, { viewModel.weight = it }, "Weight (kg)")
        Spacer(Modifier.height(16.dp))
        PrimaryInputField(viewModel.height, { viewModel.height = it }, "Height (cm)")
    }
}

@Composable
fun GoalStep(viewModel: UserViewModel) {
    var activityLevel by remember { mutableStateOf("Active") }
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Your Goal", color = InkBlack, style = Typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Text("What are you trying to achieve?", color = TextMuted, style = Typography.bodyLarge)
        Spacer(Modifier.height(32.dp))
        
        val goals = listOf("Weight Loss", "Maintain", "Muscle Gain")
        goals.forEach { item ->
            val isSelected = viewModel.goal == item
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.goal = item },
                color = if (isSelected) OrangeTint else CardSurface,
                border = BorderStroke(1.5.dp, if (isSelected) SunsetOrange else StrokeSoft),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) OrangeDeep else InkBlack,
                        style = Typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Activity Level", color = NeutralDark, style = Typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        val levels = listOf("Sedentary", "Light", "Active", "Very Active", "Extra Active")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(levels) { level ->
                val selected = activityLevel == level
                Surface(
                    modifier = Modifier
                        .height(80.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { activityLevel = level },
                    color = if (selected) OrangeTint else CardSurface,
                    border = BorderStroke(1.dp, if (selected) SunsetOrange else StrokeSoft),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = level,
                            color = if (selected) OrangeDeep else InkBlack,
                            style = Typography.labelSmall.copy(textAlign = TextAlign.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CelebrationScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onFinish()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SunsetOrange),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎉",
                fontSize = 80.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Your Plan\nIs Ready",
                style = Typography.displayLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}