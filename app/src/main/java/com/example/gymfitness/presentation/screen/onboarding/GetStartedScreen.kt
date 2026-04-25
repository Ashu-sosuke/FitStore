package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.ui.theme.GreenPrimary

@Composable
fun GetStart(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Full Screen Background Image
        Image(
            painter = painterResource(R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Important to fill the screen
        )

        // 2. Bottom Gradient Overlay (Makes text readable)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black
                        ),
                        startY = 300f // Starts gradient mid-screen
                    )
                )
        )

        // 3. Content Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 50.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start // Left aligned like the image
        ) {
            Text(
                text = "Care for\nYour Health\nCompanion",
                color = Color.White,
                fontSize = 48.sp, // Large impactful font
                lineHeight = 52.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Your health is your greatest asset—nurture it with mindful choices, regular activity, and balanced habits.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(48.dp))

            // 4. Large Neon Button
            Button(
                onClick = {
                    navController.navigate(Screen.Onboarding.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp), // Taller button as seen in reference
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(40.dp) // Very rounded
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}