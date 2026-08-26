package com.cinenova.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, elevation and icon-size tokens. Never hardcode values in screens.
 */
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
}

object Elevation {
    val level0 = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
}

object IconSize {
    val small = 18.dp
    val medium = 24.dp
    val large = 32.dp
    val hero = 56.dp
}

object CardSize {
    val posterWidth = 128.dp
    val landscapeWidth = 240.dp
    val continueWatchingWidth = 280.dp
}
