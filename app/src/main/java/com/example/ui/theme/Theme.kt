package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonGameColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = AbyssDark,
    primaryContainer = AbyssSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = AbyssSurface,
    onSecondaryContainer = NeonMagenta,
    tertiary = NeonGreen,
    onTertiary = AbyssDark,
    background = AbyssDark,
    onBackground = TextPrimary,
    surface = AbyssNavy,
    onSurface = TextPrimary,
    surfaceVariant = AbyssSurface,
    onSurfaceVariant = TextSecondary,
    outline = NeonCyanGlow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonGameColorScheme,
        typography = Typography,
        content = content
    )
}
