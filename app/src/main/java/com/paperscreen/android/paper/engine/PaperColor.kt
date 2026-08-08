package com.paperscreen.android.paper.engine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object PaperColor {
    // Defaults as specified by user
    val DefaultLight = Color(0xFFD8D6CF)
    val DefaultDark = Color(0xFF444444)

    /**
     * Parses a hex string (e.g., "#D8D6CF") to a Compose Color.
     */
    fun fromHex(hex: String, fallback: Color): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val colorInt = cleanHex.toLong(16)
            if (cleanHex.length == 6) {
                Color(0xFF000000 or colorInt)
            } else if (cleanHex.length == 8) {
                Color(colorInt)
            } else {
                fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Converts a Compose Color to a hex string (e.g., "#D8D6CF").
     */
    fun toHex(color: Color): String {
        return String.format("#%06X", (0xFFFFFF and color.toArgb()))
    }
}
