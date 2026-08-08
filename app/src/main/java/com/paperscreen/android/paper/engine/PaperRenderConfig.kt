package com.paperscreen.android.paper.engine

import androidx.compose.ui.graphics.Color

/**
 * Serializable configuration model for the Paper Rendering Engine.
 * This represents a single preset or current state of the engine.
 */
data class PaperRenderConfig(
    val lightColor: Color = PaperColor.DefaultLight,
    val darkColor: Color = PaperColor.DefaultDark,
    val threshold: Float = 0.52f, // 52%
    val strength: Float = 1.0f,   // 100%
    val brightness: Float = 0.0f, // 0.0 is neutral
    val contrast: Float = 1.2f,   // 1.0 is neutral
    val mode: PaperRenderMode = PaperRenderMode.TWO_TONE
)
