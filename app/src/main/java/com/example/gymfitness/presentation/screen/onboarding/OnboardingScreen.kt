package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.UserViewModel
import com.example.gymfitness.ui.theme.BgDark
import com.example.gymfitness.ui.theme.CardDark
import com.example.gymfitness.ui.theme.GreenPrimary
import com.example.gymfitness.ui.theme.TextGray
import com.example.gymfitness.ui.theme.TextWhite

@Composable
fun OnboardingScreen(navController: NavController, viewModel: UserViewModel = hiltViewModel()) {
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var goal by remember { mutableStateOf("Maintain") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "FitStore",
            color = GreenPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Personalize your profile",
            color = TextGray,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(32.dp))

        // Gender Selection Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionCard(
                label = "Male",
                isSelected = gender == "Male",
                onClick = { gender = "Male" },
                modifier = Modifier.weight(1f)
            )
            SelectionCard(
                label = "Female",
                isSelected = gender == "Female",
                onClick = { gender = "Female" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Fixes the 'it' error by using explicit naming or ensuring the lambda is scoped
        OnboardingTextField(value = age, label = "Age", onValueChange = { newValue -> age = newValue })
        Spacer(Modifier.height(16.dp))
        OnboardingTextField(value = weight, label = "Weight (kg)", onValueChange = { newValue -> weight = newValue })
        Spacer(Modifier.height(16.dp))
        OnboardingTextField(value = height, label = "Height (cm)", onValueChange = { newValue -> height = newValue })

        Spacer(Modifier.height(24.dp))

        Text("Your Goal", color = TextWhite, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(12.dp))

        val goals = listOf("Lose Weight", "Maintain", "Gain Muscle")
        goals.forEach { item ->
            SelectionCard(
                label = item,
                isSelected = goal == item,
                onClick = { goal = item },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                viewModel.saveUser(
                    age = age,
                    weight = weight,
                    height = height,
                    gender = gender,
                    goal = goal
                )
                navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(16.dp),
            enabled = age.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()
        ) {
            Text("GET STARTED", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// Fixes 'Unresolved reference: OnboardingTextField'
@Composable
fun OnboardingTextField(value: String, label: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextGray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = CardDark,
            focusedContainerColor = CardDark,
            unfocusedContainerColor = CardDark
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
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) GreenPrimary else CardDark)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextWhite,
            fontWeight = FontWeight.Bold
        )
    }
}