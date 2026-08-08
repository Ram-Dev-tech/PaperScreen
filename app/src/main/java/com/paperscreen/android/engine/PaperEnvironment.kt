package com.paperscreen.android.engine

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.paperscreen.android.papermode.PaperModeSettingsManager
import com.paperscreen.android.papermode.PaperModeType

@Composable
fun PaperEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { PaperModeSettingsManager(context) }
    
    val masterEnabled by settingsManager.masterPaperModeEnabled.collectAsState(initial = true)
    val paperModeType by settingsManager.paperModeType.collectAsState(initial = PaperModeType.PAPER)
    val contrast by settingsManager.contrast.collectAsState(initial = 60)
    val brightness by settingsManager.brightness.collectAsState(initial = 70)
    val strength by settingsManager.strength.collectAsState(initial = 80)
    val threshold by settingsManager.threshold.collectAsState(initial = 52)
    
    val effectModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && masterEnabled) {
        val shader = remember { RuntimeShader(PAPER_SHADER) }
        
        // lightColor = #D8D6CF -> RGB(216, 214, 207)
        shader.setFloatUniform("lightColor", 216f / 255f, 214f / 255f, 207f / 255f)
        // darkColor = #444444 -> RGB(68, 68, 68)
        shader.setFloatUniform("darkColor", 68f / 255f, 68f / 255f, 68f / 255f)
        
        // Map 0-100 sliders to float values
        shader.setFloatUniform("brightness", (brightness - 50f) / 100f)
        shader.setFloatUniform("contrast", contrast / 50f)
        shader.setFloatUniform("strength", strength / 100f)
        shader.setFloatUniform("threshold", threshold / 100f)
        
        val modeInt = when (paperModeType) {
            PaperModeType.ORIGINAL -> 0
            PaperModeType.PAPER -> 1
            PaperModeType.GRAYSCALE -> 2
        }
        shader.setIntUniform("mode", modeInt)
        
        Modifier.graphicsLayer {
            renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
        }
    } else {
        Modifier
    }

    Box(modifier = modifier.then(effectModifier)) {
        content()
    }
}
