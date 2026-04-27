package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.*

@Composable
fun OnboardingScreen(navController: NavController, viewModel: UserViewModel = hiltViewModel()) {

    val CyberLime = Color(0xFFD0FD3E)

// Determine if the button should be active
    val isEnabled = if (viewModel.currentStep == 1) {
        viewModel.name.isNotEmpty() && viewModel.age.isNotEmpty()
    } else true

    // Dynamic background based on gender selection
    val backgroundImage = if (viewModel.gender == "Male") {
        R.drawable._9e84ac439f8ba294d6f17a2f2a64cd1
    } else {
        R.drawable._f6b749fe68be3833acc6ee7a151ffd1
    }

    Box(modifier = Modifier.fillMaxSize().background(BgLight)) {
        // High-Key Background (Dimmed to keep UI crisp)
        Crossfade(targetState = backgroundImage, animationSpec = tween(700), label = "bg") { targetImg ->
            Image(
                painter = painterResource(targetImg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.8f // Subtle visibility for light theme
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )


        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Professional Step Indicator
            Row(Modifier.width(100.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val color by animateColorAsState(
                        if (index <= viewModel.currentStep) GymGreenDark else BorderGray,
                        label = "progress"
                    )
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(color))
                }
            }

            Spacer(Modifier.height(40.dp))

            // Animated Step Transitions
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = viewModel.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    }, label = "steps"
                ) { step ->
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (step) {
                            0 -> GenderStep(viewModel)
                            1 -> DetailsStep(viewModel)
                            2 -> GoalStep(viewModel)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Action Button (Full Width when Step 0)
                Button(
                    onClick = {
                        if (viewModel.currentStep < 2) {
                            viewModel.nextStep()
                        } else {
                            viewModel.saveUser {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        // Adding a subtle glow effect to match the "High-Energy" theme
                        .shadow(
                            elevation = if (isEnabled) 12.dp else 0.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = CyberLime.copy(alpha = 0.5f)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0FD3E), // Matching the "Athletic" green
                        contentColor = Color.Black,          // Black text on Lime is more premium
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    enabled = if (viewModel.currentStep == 1) {
                        viewModel.name.isNotEmpty() && viewModel.age.isNotEmpty() &&
                                viewModel.weight.isNotEmpty() && viewModel.height.isNotEmpty()
                    } else true
                ) {
                    Text(
                        text = if (viewModel.currentStep < 2) "CONTINUE" else "GET STARTED",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun GenderStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Select Gender", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("Personalized for your biology.", color = TextSecondary, fontSize = 15.sp)
        Spacer(Modifier.height(48.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SelectionCard("Male", viewModel.gender == "Male", { viewModel.gender = "Male" }, Modifier.weight(1f).height(180.dp))
            SelectionCard("Female", viewModel.gender == "Female", { viewModel.gender = "Female" }, Modifier.weight(1f).height(180.dp))
        }
    }
}

@Composable
fun DetailsStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.previousStep() },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Text("About You", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(32.dp))
        OnboardingTextField(viewModel.name, "Full Name", KeyboardType.Text) { viewModel.name = it }
        Spacer(Modifier.height(20.dp))
        OnboardingTextField(viewModel.age, "Age", KeyboardType.Number) { viewModel.age = it }
        Spacer(Modifier.height(20.dp))
        OnboardingTextField(viewModel.weight, "Weight (kg)", KeyboardType.Decimal) { viewModel.weight = it }
        Spacer(Modifier.height(20.dp))
        OnboardingTextField(viewModel.height, "Height (cm)", KeyboardType.Number) { viewModel.height = it }
    }
}

@Composable
fun GoalStep(viewModel: UserViewModel) {
    val title = if (viewModel.gender == "Male") "Build Your Physique" else "Shape Your Body"
    val goals = if (viewModel.gender == "Male") listOf("Bulk Up", "Athletic", "Lean Out") else listOf("Tone Up", "Maintain", "Weight Loss")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { viewModel.previousStep() },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White // Keep arrow visible
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                title,
                color = Color.White, // CRITICAL: Change to White
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(Modifier.height(32.dp))

        goals.forEach { item ->
            SelectionCard(
                label = item,
                isSelected = viewModel.goal == item,
                onClick = { viewModel.goal = item },
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .height(70.dp)
            )
        }
    }
}

@Composable
fun OnboardingTextField(
    value: String,
    label: String,
    kbType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        // Modern rounded corners
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = kbType,
            imeAction = ImeAction.Next
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,

            focusedBorderColor = Color(0xFFD0FD3E), // That Cyber Lime
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),

            cursorColor = Color(0xFFD0FD3E)
        )
    )
}

@Composable
fun SelectionCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic colors for reactivity
    val bgColor by animateColorAsState(
        if (isSelected) Color(0xFFD0FD3E).copy(alpha = 0.9f) // Bright Green when picked
        else Color.White.copy(alpha = 0.1f), // Subtle glass look when not
        label = "cardBg"
    )
    val textColor by animateColorAsState(
        if (isSelected) Color.Black else Color.White,
        label = "textColor"
    )
    val borderAlpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "border")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = bgColor,
        // Glowing border for selected item
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD0FD3E) else Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isSelected) 10.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center // Left aligned feels more modern
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}