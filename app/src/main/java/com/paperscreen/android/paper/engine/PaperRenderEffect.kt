package com.paperscreen.android.paper.engine

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies the Paper Rendering Engine AGSL shader to the given Modifier.
 * Hardware accelerated on Android 13+ (Tiramisu).
 */
fun Modifier.paperRenderEffect(config: PaperRenderConfig, enabled: Boolean = true): Modifier {
    if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return this
    }

    return this.graphicsLayer {
        val shader = RuntimeShader(PAPER_SHADER)

        shader.setFloatUniform(
            "lightColor",
            config.lightColor.red,
            config.lightColor.green,
            config.lightColor.blue
        )

        shader.setFloatUniform(
            "darkColor",
            config.darkColor.red,
            config.darkColor.green,
            config.darkColor.blue
        )

        shader.setFloatUniform("threshold", config.threshold)
        shader.setFloatUniform("strength", config.strength)
        shader.setFloatUniform("brightness", config.brightness)
        shader.setFloatUniform("contrast", config.contrast)
        
        val modeInt = when (config.mode) {
            PaperRenderMode.ORIGINAL -> 0
            PaperRenderMode.TWO_TONE -> 1
            PaperRenderMode.GRAYSCALE -> 2
            PaperRenderMode.PAPER -> 3
            PaperRenderMode.ADAPTIVE -> 4
        }
        shader.setIntUniform("mode", modeInt)

        renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
    }
}
