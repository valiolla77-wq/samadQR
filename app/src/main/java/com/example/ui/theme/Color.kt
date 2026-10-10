package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// SAMAD QR Design System (Emerald & Mint Bento)
// Static Light Palette (Used by Theme.kt)
val SamadLightBackground = Color(0xFFEEF6F1)
val SamadLightSurface = Color(0xFFFFFFFF)
val SamadLightSurfaceBright = Color(0xFFF4FBF6)
val SamadLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val SamadLightSurfaceContainerLow = Color(0xFFEBF6EF)
val SamadLightSurfaceContainer = Color(0xFFE4F0E7)
val SamadLightSurfaceContainerHigh = Color(0xFFD9E8DE)
val SamadLightSurfaceContainerHighest = Color(0xFFCFE0D5)
val SamadLightSurfaceDim = Color(0xFFCBDAD0)
val SamadLightSurfaceVariant = Color(0xFFDBE6DE)
val SamadLightOnSurface = Color(0xFF132018)
val SamadLightOnSurfaceVariant = Color(0xFF3F4D43)
val SamadLightOutline = Color(0xFF6F7D73)
val SamadLightOutlineVariant = Color(0xFFBFCDC3)

val SamadLightPrimary = Color(0xFF006B2C)
val SamadLightPrimaryContainer = Color(0xFF00873A)
val SamadLightPrimaryFixed = Color(0xFF7FFC97)
val SamadLightPrimaryFixedDim = Color(0xFF62DF7D)
val SamadLightOnPrimary = Color(0xFFFFFFFF)
val SamadLightOnPrimaryContainer = Color(0xFFF7FFF2)

val SamadLightSecondary = Color(0xFF855300)
val SamadLightSecondaryContainer = Color(0xFFFFB95F)
val SamadLightSecondaryFixed = Color(0xFFFFDDB8)
val SamadLightTertiary = Color(0xFF006B2D)

// Static Dark Theme Palette (Used by Theme.kt)
val SamadDarkBackground = Color(0xFF0A120D)
val SamadDarkSurface = Color(0xFF131F17)
val SamadDarkSurfaceBright = Color(0xFF1D2E23)
val SamadDarkSurfaceContainerLowest = Color(0xFF101A13)
val SamadDarkSurfaceContainerLow = Color(0xFF142219)
val SamadDarkSurfaceContainer = Color(0xFF19291F)
val SamadDarkSurfaceContainerHigh = Color(0xFF213629)
val SamadDarkSurfaceVariant = Color(0xFF1C2C21)

val SamadDarkPrimary = Color(0xFF46DB79)
val SamadDarkPrimaryContainer = Color(0xFF005322)
val SamadDarkOnPrimary = Color(0xFF003915)
val SamadDarkOnPrimaryContainer = Color(0xFF74FF9B)

val SamadDarkSecondary = Color(0xFFFFB95F)
val SamadDarkSecondaryContainer = Color(0xFF633F00)
val SamadDarkOnSecondary = Color(0xFF452B00)
val SamadDarkOnSecondaryContainer = Color(0xFFFFDDB8)

val SamadDarkOnSurface = Color(0xFFE8F5ED)
val SamadDarkOnSurfaceVariant = Color(0xFFA5B8AC)
val SamadDarkOutline = Color(0xFF5A6F62)
val SamadDarkOutlineVariant = Color(0xFF2E4235)
val SamadDarkCardBorder = Color(0x3346DB79)

// Status colors
val StatusActive = Color(0xFF006B2C)
val StatusUpcoming = Color(0xFF0284C7)
val StatusExpired = Color(0xFFDC2626)
val StatusWarning = Color(0xFF855300)

// Static Aliases for backward compatibility
val SamadPrimaryFixed = Color(0xFF7FFC97)
val SamadPrimaryFixedDim = Color(0xFF62DF7D)
val SamadCyan = SamadLightPrimary
val SamadCyanDark = SamadLightPrimaryContainer
val SamadAmber = SamadLightSecondary
val SamadAmberDark = Color(0xFF684100)
val SamadEmerald = SamadLightPrimary
val SamadEmeraldDark = SamadLightPrimaryContainer

// Dynamic Color Accessors (Seamless Adaptive Support for Light & Dark Modes)
val SamadPrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary

val SamadPrimaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer

val SamadOnPrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary

val SamadSecondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondary

val SamadSecondaryContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer

val SamadTertiary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary

val SamadBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

val SamadSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

val SamadSurfaceBright: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val SamadSurfaceContainerLowest: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

val SamadSurfaceContainerLow: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val SamadSurfaceContainer: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val SamadSurfaceVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val SamadOnSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val SamadOnSurfaceVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val SamadOutline: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

val SamadOutlineVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant

val SamadCardBorder: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

val SamadCardShadow: Color
    @Composable @ReadOnlyComposable get() = Color(0x14000000)

val DarkBackground: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surface

val DarkSurfaceVariant: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val DarkSurfaceElevated: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant

val TextPrimary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface

val TextSecondary: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline
