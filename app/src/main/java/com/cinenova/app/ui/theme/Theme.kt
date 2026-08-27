package com.cinenova.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1F1800),
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = OnGoldContainerDark,
    secondary = InfoBlue,
    onSecondary = Color(0xFF001F2A),
    tertiary = Color(0xFFA5B4FC),
    background = NavyDarkBackground,
    onBackground = OnSurfaceDark,
    surface = NavyDarkBackground,
    onSurface = OnSurfaceDark,
    surfaceVariant = NavyDarkSurface,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = Color(0xFF0A0F1D),
    surfaceContainerLow = NavyDarkSurface,
    surfaceContainer = NavyDarkContainer,
    surfaceContainerHigh = NavyDarkContainerHigh,
    surfaceContainerHighest = Color(0xFF3B4860),
    outline = OutlineDark,
    outlineVariant = Color(0xFF2E384D),
    error = ErrorRed,
)

private val LightScheme = lightColorScheme(
    primary = GoldDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8A3),
    onPrimaryContainer = Color(0xFF332400),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    tertiary = Color(0xFF4F46E5),
    background = SlateLightBackground,
    onBackground = OnSurfaceLight,
    surface = SlateLightSurface,
    onSurface = OnSurfaceLight,
    surfaceVariant = SlateLightContainer,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = SlateLightContainer,
    surfaceContainerHigh = SlateLightContainerHigh,
    surfaceContainerHighest = Color(0xFFCBD5E1),
    outline = OutlineLight,
    outlineVariant = Color(0xFFE2E8F0),
    error = ErrorRed,
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

@Composable
fun CineNovaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
