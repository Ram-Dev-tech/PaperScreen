package com.paperscreen.android

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.paperscreen.android.ui.LauncherPager
import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Composable
fun MainNavigation(initialIntent: Intent? = null) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          LauncherPager(
            onLaunchSettings = { backStack.add(Settings) }
          )
        }
        entry<Settings> {
          com.paperscreen.android.ui.settings.SettingsScreen(
            onNavigate = {
              // Stub for deeper settings navigation
            }
          )
        }
      },
  )
}

@Serializable object Main : NavKey
@Serializable object Settings : NavKey
