package com.paperscreen.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherPager(onLaunchSettings: () -> Unit) {
    // Horizontal pager for Widget (0) <- Home (1) -> App Drawer (2)
    val horizontalState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    HorizontalPager(
        state = horizontalState,
        modifier = Modifier.fillMaxSize()
    ) { hPage ->
        when (hPage) {
            0 -> WidgetScreen()
            1 -> HomeScreen(onLaunchSettings = onLaunchSettings)
            2 -> com.paperscreen.android.launcher.ui.AppLauncherScreen(
                onNavigateToPaperApp = { destination ->
                    when (destination) {
                        com.paperscreen.android.launcher.PaperDestination.SETTINGS -> onLaunchSettings()
                        // Library is removed, so we ignore it if it somehow comes through
                        else -> {}
                    }
                }
            )
        }
    }
}
