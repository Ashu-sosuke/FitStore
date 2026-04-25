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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.ui.theme.BgDark
import com.example.gymfitness.ui.theme.GreenPrimary
import com.example.gymfitness.ui.theme.TextGray
import com.example.gymfitness.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStart(navController: NavController) {
    Scaffold(
        containerColor = BgDark,
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0),
                title = {
                    Text(
                        "FITSTORE",
                        color = GreenPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp // Increased spacing for branding feel
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgDark.copy(alpha = 0.9f) // Slight transparency
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Neon Glow Background Effect
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (-100).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GreenPrimary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Image with better scaling
                Image(
                    painter = painterResource(R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78),
                    contentDescription = "Gym Hero Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .padding(bottom = 24.dp)
                )

                // Text Content Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Transform Your\nBody & Mind",
                        color = TextWhite,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "The ultimate companion for your fitness journey. Track, analyze, and conquer your goals.",
                        color = TextGray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                // CTA Button Section
                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.Onboarding.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp), // Slightly taller for premium feel
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp), // Softer corners
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
                ) {
                    Text(
                        "GET STARTED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartPreview() {
    val navController = rememberNavController()
    GetStart(navController)
}