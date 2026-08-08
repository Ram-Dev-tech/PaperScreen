package com.paperscreen.android.paper.engine

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A CompositionLocal to provide the current PaperRenderConfig down the Compose tree.
 * Built-in Paper Apps (Notes, Reader, etc.) can access this to query current rendering states.
 */
val LocalPaperRenderConfig = staticCompositionLocalOf { PaperRenderConfig() }
