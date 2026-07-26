package com.example.gymfitness.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val DarkColorScheme = darkColorScheme(
    primary          = LimeGreen,
    onPrimary        = Color(0xFF121212), // Dark text on vibrant LimeGreen
    primaryContainer = LimeTintDark,
    onPrimaryContainer = LimeDeepDark,
    secondary        = InfoBlue,
    onSecondary      = Color.White,
    tertiary         = SuccessGreen,
    background       = NearBlack,
    onBackground     = OffWhite,
    surface          = SurfaceDark,
    onSurface        = OffWhite,
    surfaceVariant   = SurfaceAltDark,
    onSurfaceVariant = TextMutedDark,
    outline          = StrokeDark,
    error            = ErrorRed,
)

@Composable
fun GymFitnessTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowInsetsControllerCompat(window, view)

            // System default for dark theme: light icons on dark background
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}