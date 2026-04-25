package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

    val backgroundImage = if (viewModel.gender == "Male") {
        R.drawable._9e84ac439f8ba294d6f17a2f2a64cd1
    } else {
        R.drawable._f6b749fe68be3833acc6ee7a151ffd1
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        // Background with Crossfade for smooth gender switching
        Crossfade(targetState = backgroundImage, animationSpec = tween(700), label = "bg") { targetImg ->
            Image(
                painter = painterResource(targetImg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(0.7f), Color.Black)
        )))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Animated Progress Indicator
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val color by animateColorAsState(
                        if (index <= viewModel.currentStep) GreenPrimary else Color.Gray.copy(0.3f),
                        label = "progress"
                    )
                    Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(color))
                }
            }

            Spacer(Modifier.height(32.dp))

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
                // If Back arrow isn't enough, we keep the Outlined button or remove based on your preference
                // For "Full Width" feel on Step 0, we only show one button
                if (viewModel.currentStep > 0) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, GreenPrimary.copy(0.5f))
                    ) {
                        Text("BACK", color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }

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
                    modifier = Modifier.weight(2f).height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    enabled = if (viewModel.currentStep == 1) {
                        viewModel.name.isNotEmpty() && viewModel.age.isNotEmpty() &&
                                viewModel.weight.isNotEmpty() && viewModel.height.isNotEmpty()
                    } else true
                ) {
                    Text(
                        text = if (viewModel.currentStep < 2) "CONTINUE" else "FINISH",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun GenderStep(viewModel: UserViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Select Gender", color = TextWhite, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("We use this to calculate your metabolic rate.", color = TextGray, fontSize = 14.sp)
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
                modifier = Modifier.background(Color.White.copy(0.1f), CircleShape).size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GreenPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Text("About You", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Black)
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
                modifier = Modifier.background(Color.White.copy(0.1f), CircleShape).size(40.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = GreenPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Text(title, color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(32.dp))
        goals.forEach { item ->
            SelectionCard(item, viewModel.goal == item, { viewModel.goal = item }, Modifier.fillMaxWidth().padding(vertical = 8.dp).height(75.dp))
        }
    }
}

@Composable
fun OnboardingTextField(value: String, label: String, kbType: KeyboardType, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = kbType, imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
            focusedBorderColor = GreenPrimary, unfocusedBorderColor = Color.White.copy(0.1f),
            focusedContainerColor = Color.Black.copy(0.3f), unfocusedContainerColor = Color.Black.copy(0.3f)
        )
    )
}

@Composable
fun SelectionCard(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor by animateColorAsState(if (isSelected) GreenPrimary else Color.Black.copy(0.4f), label = "cardBg")
    val contentColor by animateColorAsState(if (isSelected) Color.Black else TextWhite, label = "content")

    Surface(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() },
        color = bgColor,
        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(0.1f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}