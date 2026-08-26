package com.cinenova.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween

/**
 * Motion tokens — refined, unobtrusive Material-style motion.
 */
object Motion {
    const val FAST_MS = 150
    const val NORMAL_MS = 250
    const val SLOW_MS = 400

    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val StandardEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    fun <T> fast() = tween<T>(FAST_MS, easing = StandardEasing)
    fun <T> normal() = tween<T>(NORMAL_MS, easing = EmphasizedEasing)
    fun <T> slow() = tween<T>(SLOW_MS, easing = EmphasizedEasing)
}
