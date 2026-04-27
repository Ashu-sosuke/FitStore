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
import com.example.gymfitness.ui.theme.GymGreenDark

@Composable
fun GetStart(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        Image(
            painter = painterResource(R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78),
            contentDescription = "Gym Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.9f // Keep it bright
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f), // Subtle dimming for the mid-section
                            Color.Black.copy(alpha = 0.8f)  // Stronger at bottom for text pop
                        ),
                        startY = 400f
                    )
                )
        )

        // 3. Branding & Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {


            Text(
                text = "Care for\nYour Health\nCompanion",
                color = Color.White,
                fontSize = 44.sp,
                lineHeight = 50.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Your health is your greatest asset—nurture it with mindful choices and regular activity.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(Modifier.height(40.dp))

            // 4. Premium Electric Blue CTA Button
            Button(
                onClick = {
                    navController.navigate(Screen.Onboarding.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp), // Premium taller height
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymGreenDark,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp), // Sleeker 2026 rounded style
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}