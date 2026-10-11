package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SamadLightColorScheme = lightColorScheme(
    primary = SamadLightPrimary,
    onPrimary = SamadLightOnPrimary,
    primaryContainer = SamadLightPrimaryContainer,
    onPrimaryContainer = SamadLightOnPrimaryContainer,
    secondary = SamadLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = SamadLightSecondaryContainer,
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = SamadLightTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA1F5B4),
    onTertiaryContainer = Color(0xFF00210A),
    background = SamadLightBackground,
    onBackground = SamadLightOnSurface,
    surface = Color.White,
    onSurface = SamadLightOnSurface,
    surfaceVariant = SamadLightSurfaceVariant,
    onSurfaceVariant = SamadLightOnSurfaceVariant,
    outline = SamadLightOutline,
    outlineVariant = SamadLightOutlineVariant,
    error = StatusExpired,
    onError = Color.White
)

private val SamadDarkColorScheme = darkColorScheme(
    primary = SamadDarkPrimary,
    onPrimary = SamadDarkOnPrimary,
    primaryContainer = SamadDarkPrimaryContainer,
    onPrimaryContainer = SamadDarkOnPrimaryContainer,
    secondary = SamadDarkSecondary,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF633F00),
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = SamadDarkPrimary,
    onTertiary = SamadDarkOnPrimary,
    background = SamadDarkBackground,
    onBackground = SamadDarkOnSurface,
    surface = SamadDarkSurface,
    onSurface = SamadDarkOnSurface,
    surfaceVariant = SamadDarkSurfaceVariant,
    onSurfaceVariant = SamadDarkOnSurfaceVariant,
    outline = SamadDarkOutline,
    outlineVariant = SamadDarkOutlineVariant,
    error = StatusExpired,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SamadDarkColorScheme else SamadLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
