package com.paperscreen.android.paper.engine

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.paperscreen.android.papermode.PaperModeSettingsManager

/**
 * The root wrapper for any Paper App.
 * Automatically injects the Paper Rendering Engine and applies the current user configuration
 * to all child composables.
 */
@Composable
fun PaperEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { PaperModeSettingsManager(context) }
    
    val masterEnabled by settingsManager.masterPaperModeEnabled.collectAsState(initial = true)
    val config by settingsManager.paperRenderConfig.collectAsState(initial = PaperRenderConfig())

    PaperEnvironmentProvider(
        config = config,
        enabled = masterEnabled,
        modifier = modifier,
        content = content
    )
}

/**
 * Explicit provider for the Paper Rendering Engine. Useful for Live Previews
 * where the config comes from local state instead of the DataStore.
 */
@Composable
fun PaperEnvironmentProvider(
    config: PaperRenderConfig,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPaperRenderConfig provides config) {
        Box(
            modifier = modifier.paperRenderEffect(
                config = config,
                enabled = enabled
            )
        ) {
            content()
        }
    }
}
