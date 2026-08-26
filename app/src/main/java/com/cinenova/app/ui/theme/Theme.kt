package com.cinenova.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Gold,
    onPrimary = InkBlack,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = OnGoldContainerDark,
    secondary = Color(0xFFD8C48E),
    onSecondary = Color(0xFF211B00),
    tertiary = InfoBlue,
    background = InkBlack,
    onBackground = OnSurfaceDark,
    surface = InkBlack,
    onSurface = OnSurfaceDark,
    surfaceVariant = Charcoal,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = Color(0xFF090B0F),
    surfaceContainerLow = Charcoal,
    surfaceContainer = Color(0xFF141821),
    surfaceContainerHigh = Slate,
    surfaceContainerHighest = Color(0xFF232A38),
    outline = OutlineDark,
    outlineVariant = Color(0xFF2C3240),
    error = ErrorRed,
)

private val LightScheme = lightColorScheme(
    primary = GoldDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE08C),
    onPrimaryContainer = Color(0xFF241A00),
    secondary = Color(0xFF6B5D2E),
    onSecondary = Color.White,
    tertiary = Color(0xFF3A6188),
    background = PaperWhite,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = Color(0xFFDDD8CC),
    error = Color(0xFFBA1A1A),
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
