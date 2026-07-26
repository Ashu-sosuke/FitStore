package com.example.gymfitness.presentation.screen.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.gymfitness.R
import com.example.gymfitness.presentation.components.PrimaryButton
import com.example.gymfitness.presentation.navigation.Screen
import com.example.gymfitness.presentation.viewmodel.AuthUiState
import com.example.gymfitness.presentation.viewmodel.AuthViewModel
import com.example.gymfitness.ui.theme.*

@Composable
fun GetStart(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle auth success — route based on isNewUser
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            val successState = authState as AuthUiState.Success
            if (successState.isNewUser) {
                // New user: route to Onboarding so they can fill in their details
                val encodedName = java.net.URLEncoder.encode(
                    successState.displayName.ifEmpty { "" }, "UTF-8"
                )
                navController.navigate("onboarding_screen?displayName=$encodedName") {
                    popUpTo(Screen.GetStart.route) { inclusive = true }
                }
            } else {
                // Existing user: skip onboarding, go directly to Home
                navController.navigate(Screen.Home.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Handle auth error — show Snackbar
    LaunchedEffect(authState) {
        if (authState is AuthUiState.Error) {
            snackbarHostState.showSnackbar(
                message = (authState as AuthUiState.Error).message,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2D2D2D),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(PageBg)
        ) {
            // Hero Image top 60%
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
            ) {
                Image(
                    painter = painterResource(R.drawable.b2d3a8fe2d64f98ca2ebea9744a06e78),
                    contentDescription = "Athlete Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // App Logo
                Text(
                    text = "GymFitness",
                    style = Typography.headlineMedium.copy(color = Color.White),
                    modifier = Modifier
                        .padding(top = 48.dp, start = 24.dp)
                        .statusBarsPadding()
                )
            }

            // White rounded sheet sliding up from bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(CardSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Track. Train.\nTransform.",
                            style = Typography.displayLarge,
                            color = InkBlack,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your health is your greatest asset. Nurture it with mindful choices and regular activity.",
                            style = Typography.bodyLarge,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PrimaryButton(
                            text = if (authState is AuthUiState.Loading) "Signing in..." else "Sign in with Google",
                            onClick = {
                                if (authState !is AuthUiState.Loading) {
                                    authViewModel.signInWithGoogle(context)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedButton(
                            onClick = {
                                navController.navigate(Screen.Onboarding.createRoute())
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = InkBlack),
                            border = BorderStroke(1.dp, StrokeSoft),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text(
                                text = "Continue as Guest",
                                style = Typography.titleLarge.copy(fontSize = 15.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}