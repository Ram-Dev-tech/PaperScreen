package com.paperscreen.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherPager(onLaunchLibrary: () -> Unit, onLaunchSettings: () -> Unit) {
    // Horizontal pager for App Drawer (0) <- Home (1) -> Focus (2)
    val horizontalState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    
    // Vertical pager for Control Center (0) <- Home (1) -> Search (2)
    val verticalState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    HorizontalPager(
        state = horizontalState,
        modifier = Modifier.fillMaxSize()
    ) { hPage ->
        when (hPage) {
            0 -> com.paperscreen.android.launcher.ui.AppLauncherScreen(
                onNavigateToPaperApp = { destination ->
                    when (destination) {
                        com.paperscreen.android.launcher.PaperDestination.LIBRARY -> onLaunchLibrary()
                        com.paperscreen.android.launcher.PaperDestination.SETTINGS -> onLaunchSettings()
                    }
                }
            )
            1 -> {
                VerticalPager(
                    state = verticalState,
                    modifier = Modifier.fillMaxSize()
                ) { vPage ->
                    when (vPage) {
                        0 -> ControlCenterScreen()
                        1 -> HomeScreen(onLaunchLibrary = onLaunchLibrary)
                        2 -> SearchScreen()
                    }
                }
            }
            2 -> FocusScreen()
        }
    }
}
