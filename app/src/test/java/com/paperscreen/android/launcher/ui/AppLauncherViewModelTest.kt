package com.paperscreen.android.launcher.ui

import com.paperscreen.android.launcher.ExternalApp
import com.paperscreen.android.launcher.PaperApp
import com.paperscreen.android.launcher.PaperDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLauncherViewModelTest {

    @Test
    fun `filterLauncherItems correctly filters items by label ignore case`() {
        val items = listOf(
            ExternalApp("com.example.z", "Zebra"),
            ExternalApp("com.example.a", "Apple"),
            PaperApp(PaperDestination.LIBRARY, "Library")
        )

        // Empty query -> returns all
        val resultEmpty = AppLauncherViewModel.filterLauncherItems(items, "")
        assertEquals(3, resultEmpty.size)

        // Query "z" -> returns Zebra
        val resultZ = AppLauncherViewModel.filterLauncherItems(items, "z")
        assertEquals(1, resultZ.size)
        assertEquals("Zebra", resultZ[0].label)

        // Query "app" -> returns Apple
        val resultApp = AppLauncherViewModel.filterLauncherItems(items, "app")
        assertEquals(1, resultApp.size)
        assertEquals("Apple", resultApp[0].label)

        // Query "lib" -> returns Library
        val resultLib = AppLauncherViewModel.filterLauncherItems(items, "LiBrArY")
        assertEquals(1, resultLib.size)
        assertEquals("Library", resultLib[0].label)
    }
}
