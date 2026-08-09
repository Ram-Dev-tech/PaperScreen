package com.paperscreen.android

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.paperscreen.android.ui.LauncherPager
import com.paperscreen.android.reader.ui.LibraryScreen
import com.paperscreen.android.reader.ui.ReaderScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack<Any>(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          LauncherPager(
            onLaunchLibrary = { backStack.add(Library) }
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
      },
  )
}

object Main
object Library
data class Reader(val bookId: Long)
