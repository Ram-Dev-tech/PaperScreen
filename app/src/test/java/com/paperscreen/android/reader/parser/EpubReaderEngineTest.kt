package com.paperscreen.android.reader.parser

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.readium.r2.shared.publication.*

@RunWith(RobolectricTestRunner::class)
class EpubReaderEngineTest {

    @Test
    fun `test Locator JSON serialization and parsing`() {
        val locator = Locator(
            href = "/chapter1.xhtml",
            type = "application/xhtml+xml",
            title = "Chapter 1",
            locations = Locator.Locations(progression = 0.5)
        )
        val json = locator.toJSON().toString()
        val parsed = Locator.fromJSON(org.json.JSONObject(json))
        assertNotNull(parsed)
        assertEquals("/chapter1.xhtml", parsed?.href)
        assertEquals("Chapter 1", parsed?.title)
        assertEquals(0.5, parsed?.locations?.progression)
    }

    @Test
    fun `test EpubReaderEngine TOC fallback and Locator boundaries`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = EpubReaderEngine(context, Uri.parse("file://dummy"))
        
        // Inject a dummy Publication using reflection
        val manifest = Manifest(
            metadata = Metadata(localizedTitle = LocalizedString("Test Book")),
            readingOrder = listOf(
                Link(href = "/chapter1.xhtml", title = "Chapter 1"),
                Link(href = "/chapter2.xhtml", title = "Chapter 2")
            ),
            tableOfContents = listOf(
                Link(href = "/chapter1.xhtml#start", title = "Intro"),
                Link(href = "/chapter2.xhtml", title = "Part 2")
            )
        )
        val publication = Publication(manifest)
        
        val pubField = EpubReaderEngine::class.java.getDeclaredField("publication")
        pubField.isAccessible = true
        pubField.set(engine, publication)

        // Test TOC mapping
        val toc = engine.getTableOfContents()
        assertEquals(2, toc.size)
        assertEquals("Intro", toc[0].title)
        assertEquals(0, toc[0].chapterIndex)
        assertEquals("Part 2", toc[1].title)
        assertEquals(1, toc[1].chapterIndex)

        // Test Locator mapping
        val locatorJson = engine.getLocatorForChapter(1)
        assertNotNull(locatorJson)
        val index = engine.getChapterIndexFromLocator(locatorJson!!)
        assertEquals(1, index)
        
        // Test boundaries
        assertNull(engine.getLocatorForChapter(-1))
        assertNull(engine.getLocatorForChapter(2))
    }
}
