package com.example.gymfitness.presentation.screen.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymfitness.presentation.viewmodel.AuthUiState
import com.example.gymfitness.presentation.viewmodel.AuthViewModel
import com.example.gymfitness.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun GoogleSignInScreen(
    onSignInSuccess: (userId: String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignInSuccess((uiState as AuthUiState.Success).userId)
        }
    }

    var infoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(400); infoVisible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )

    Scaffold(containerColor = PageBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row {
                Text("Fit", style = Typography.displayLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold), color = InkBlack)
                Text("Store", style = Typography.displayLarge.copy(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold), color = SunsetOrange)
            }
            Text("Track · Compete · Dominate", style = Typography.bodyMedium, color = TextMuted)
            
            Spacer(Modifier.height(32.dp))
            SignInIllustration(pulseScale = pulseScale)
            Spacer(Modifier.height(32.dp))

            val isLoading = uiState is AuthUiState.Loading
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.signInWithGoogle(context)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(0.5.dp, StrokeSoft)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SunsetOrange, strokeWidth = 2.dp)
                    } else {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Continue with Google",
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = InkBlack
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = StrokeSoft)
                Text("  — After sign in —  ", style = TextStyle(fontSize = 10.sp), color = TextMuted)
                HorizontalDivider(modifier = Modifier.weight(1f), color = StrokeSoft)
            }
            Spacer(Modifier.height(16.dp))

            val infoItems = listOf(
                Pair("Get your unique 6-digit friend code", SunsetOrange),
                Pair("Add friends to your Squad instantly", InfoBlue),
                Pair("Compete on weekly leaderboards", SunsetOrange),
                Pair("Compare workouts, meals & streaks", InfoBlue),
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeTint),
                border = BorderStroke(1.dp, SunsetOrange.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    infoItems.forEachIndexed { i, (text, color) ->
                        var rowVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(infoVisible) {
                            if (infoVisible) { delay(i * 100L); rowVisible = true }
                        }
                        AnimatedVisibility(
                            visible = rowVisible,
                            enter = slideInHorizontally(initialOffsetX = { -30 }) + fadeIn()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                                Text(text, style = Typography.bodyMedium, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignInIllustration(pulseScale: Float) {
    Canvas(modifier = Modifier.size(120.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        drawCircle(color = SunsetOrange.copy(alpha = 0.05f), radius = (cx - 4.dp.toPx()) * pulseScale, center = Offset(cx, cy))
        drawCircle(color = SunsetOrange.copy(alpha = 0.15f), radius = cx * 0.55f, center = Offset(cx, cy))
        drawCircle(color = SunsetOrange.copy(alpha = 0.3f), radius = cx * 0.55f, center = Offset(cx, cy), style = Stroke(1.dp.toPx()))

        val path = Path().apply {
            moveTo(cx - 14.dp.toPx(), cy)
            lineTo(cx - 4.dp.toPx(), cy + 10.dp.toPx())
            lineTo(cx + 14.dp.toPx(), cy - 12.dp.toPx())
        }
        drawPath(path, color = SunsetOrange, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val dotY = cy + cx * 0.85f
        val dotCenters = listOf(Offset(cx - 28.dp.toPx(), dotY), Offset(cx, dotY + 6.dp.toPx()), Offset(cx + 28.dp.toPx(), dotY))
        val dotColors  = listOf(SunsetOrange, InfoBlue, SunsetOrange)
        dotCenters.forEachIndexed { i, c ->
            drawCircle(color = dotColors[i], radius = 6.dp.toPx(), center = c)
        }
        drawLine(Color.Gray.copy(alpha = 0.2f), dotCenters[0], dotCenters[1], 1.dp.toPx())
        drawLine(Color.Gray.copy(alpha = 0.2f), dotCenters[1], dotCenters[2], 1.dp.toPx())
    }
}
