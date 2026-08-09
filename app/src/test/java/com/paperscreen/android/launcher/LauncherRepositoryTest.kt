package com.paperscreen.android.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherRepositoryTest {

    @Test
    fun `processLauncherItems filters self package, handles duplicates, and sorts alphabetically`() {
        val externalApps = listOf(
            ExternalApp("com.example.z", "Zebra"),
            ExternalApp("com.example.a", "Apple"),
            ExternalApp("com.paperscreen.android", "PaperScreen"), // Self package
            ExternalApp("com.example.a", "Apple Duplicate") // Duplicate package
        )

        val paperApps = listOf(
            PaperApp(PaperDestination.LIBRARY, "Library")
        )

        val result = LauncherRepository.processLauncherItems(
            externalApps = externalApps,
            paperApps = paperApps,
            selfPackageName = "com.paperscreen.android"
        )

        assertEquals(3, result.size)
        
        // Ensure sorted by label (case-insensitive)
        assertEquals("Apple", result[0].label)
        assertEquals("Library", result[1].label)
        assertEquals("Zebra", result[2].label)

        // Ensure self package is excluded
        assertTrue(result.none { it is ExternalApp && it.packageName == "com.paperscreen.android" })
    }
}
