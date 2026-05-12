package com.otero.runningvoicecoach.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFF00B2E3),
    tertiary = Color(0xFFFF6A00),
    background = Color(0xFFF2F4F7),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF3FF),
    onBackground = Color(0xFF0A1F3D),
    onSurface = Color(0xFF0A1F3D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF082F49),
    secondary = Color(0xFF00B2E3),
    tertiary = Color(0xFFFF6A00),
    background = Color(0xFF06162D),
    surface = Color(0xFF0A1F3D),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

@Composable
fun RunningVoiceCoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
