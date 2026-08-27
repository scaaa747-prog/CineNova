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
    secondary = Color(0xFFE5CF98),
    onSecondary = Color(0xFF261D00),
    tertiary = InfoBlue,
    background = MidnightNavy,
    onBackground = OnSurfaceDark,
    surface = MidnightNavy,
    onSurface = OnSurfaceDark,
    surfaceVariant = SoftCharcoal,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = Color(0xFF0D1018),
    surfaceContainerLow = SoftCharcoal,
    surfaceContainer = SlateContainer,
    surfaceContainerHigh = Color(0xFF2E3649),
    surfaceContainerHighest = Color(0xFF3B445B),
    outline = OutlineDark,
    outlineVariant = Color(0xFF323B4E),
    error = ErrorRed,
)

private val LightScheme = lightColorScheme(
    primary = GoldDim,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE9A3),
    onPrimaryContainer = Color(0xFF332400),
    secondary = Color(0xFF78652A),
    onSecondary = Color.White,
    tertiary = Color(0xFF2962FF),
    background = PaperWhite,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F5F9),
    surfaceContainer = Color(0xFFE8EDF4),
    surfaceContainerHigh = Color(0xFFDDE3ED),
    surfaceContainerHighest = Color(0xFFD0D7E4),
    outline = OutlineLight,
    outlineVariant = Color(0xFFCCD3E0),
    error = Color(0xFFD32F2F),
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
