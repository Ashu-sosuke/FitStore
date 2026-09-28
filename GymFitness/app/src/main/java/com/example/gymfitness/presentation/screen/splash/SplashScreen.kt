package com.example.gymfitness.presentation.screen.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymfitness.R
import com.example.gymfitness.ui.theme.*

@Composable
fun SplashScreen() {
    // Pulse animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_splash")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with glowing pulse ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Soft background glow ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale * 1.08f)
                        .alpha(alphaGlow * 0.4f)
                        .clip(CircleShape)
                        .background(LimeGreen)
                )

                // Logo icon
                Image(
                    painter = painterResource(R.drawable.pulse_logo),
                    contentDescription = "Pulse Fitness Logo",
                    modifier = Modifier
                        .size(92.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(22.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title
            Text(
                text = "PULSE",
                style = Typography.displayLarge.copy(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle / Tagline
            Text(
                text = "TRACK • TRAIN • TRANSFORM",
                style = Typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = LimeGreen
                )
            )
        }
    }
}
