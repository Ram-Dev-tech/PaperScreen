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
import com.paperscreen.android.reader.ui.LibraryScreen
import com.paperscreen.android.reader.ui.ReaderScreen
import com.paperscreen.android.viewer.ui.PaperViewerScreen
import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Composable
fun MainNavigation(initialIntent: Intent? = null) {
  val backStack = rememberNavBackStack(Main)

  LaunchedEffect(initialIntent) {
    if (initialIntent?.action == Intent.ACTION_VIEW) {
      initialIntent.data?.let { uri ->
        val uriString = uri.toString()
        val mimeType = initialIntent.type
        // Navigate to Viewer
        backStack.add(Viewer(uriString, mimeType))
      }
    }
  }

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          LauncherPager(
            onLaunchLibrary = { backStack.add(Library) },
            onLaunchSettings = { backStack.add(Settings) }
          )
        }
        entry<Library> {
          LibraryScreen(
            onBookClick = { book ->
              backStack.add(Reader(book.id))
            },
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<Reader> { entry ->
          ReaderScreen(
            bookId = entry.bookId,
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<Viewer> { entry ->
          PaperViewerScreen(
            uriString = entry.uriString,
            mimeType = entry.mimeType,
            onBack = { backStack.removeLastOrNull() },
            onBridgeToReader = { bookId ->
              backStack.removeLastOrNull()
              backStack.add(Reader(bookId))
            }
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
@Serializable object Library : NavKey
@Serializable object Settings : NavKey
@Serializable data class Reader(val bookId: Long) : NavKey
@Serializable data class Viewer(val uriString: String, val mimeType: String?) : NavKey
