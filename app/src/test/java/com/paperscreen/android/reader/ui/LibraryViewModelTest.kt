package com.paperscreen.android.reader.ui

import com.paperscreen.android.reader.data.BookEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryViewModelTest {

    private val sampleBooks = listOf(
        BookEntity(
            uriString = "uri1",
            title = "A Book",
            author = null,
            coverUriString = null,
            fileType = "TXT",
            progressPercentage = 0f,
            isFavorite = false,
            lastOpenedAt = 1000L
        ),
        BookEntity(
            uriString = "uri2",
            title = "B Book",
            author = null,
            coverUriString = null,
            fileType = "EPUB",
            progressPercentage = 0.5f,
            isFavorite = true,
            lastOpenedAt = 2000L
        ),
        BookEntity(
            uriString = "uri3",
            title = "C Book",
            author = null,
            coverUriString = null,
            fileType = "PDF",
            progressPercentage = 1f,
            isFavorite = false,
            lastOpenedAt = 500L
        ),
        BookEntity(
            uriString = "uri4",
            title = "D Book",
            author = null,
            coverUriString = null,
            fileType = "TXT",
            progressPercentage = 0f,
            isFavorite = false,
            lastOpenedAt = 0L // Never opened
        )
    )

    @Test
    fun `test section Recent excludes un-opened books`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.Recent,
            SortOrder.TitleAZ,
            FilterFormat.All
        )
        // D Book (lastOpenedAt = 0) should not appear
        assertEquals(3, result.size)
        assertEquals(false, result.any { it.title == "D Book" })
    }

    @Test
    fun `test section ContinueReading`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.ContinueReading,
            SortOrder.TitleAZ,
            FilterFormat.All
        )
        assertEquals(1, result.size)
        assertEquals("B Book", result[0].title)
    }

    @Test
    fun `test section Favorites`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.Favorites,
            SortOrder.TitleAZ,
            FilterFormat.All
        )
        assertEquals(1, result.size)
        assertEquals("B Book", result[0].title)
    }

    @Test
    fun `test section Completed`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.Completed,
            SortOrder.TitleAZ,
            FilterFormat.All
        )
        assertEquals(1, result.size)
        assertEquals("C Book", result[0].title)
    }

    @Test
    fun `test filter format EPUB`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.AllBooks,
            SortOrder.TitleAZ,
            FilterFormat.EPUB
        )
        assertEquals(1, result.size)
        assertEquals("B Book", result[0].title)
    }

    @Test
    fun `test sorting Recent`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.AllBooks,
            SortOrder.Recent,
            FilterFormat.All
        )
        assertEquals(4, result.size)
        assertEquals("B Book", result[0].title) // 2000L
        assertEquals("A Book", result[1].title) // 1000L
        assertEquals("C Book", result[2].title) // 500L
        assertEquals("D Book", result[3].title) // 0L
    }

    @Test
    fun `test sorting TitleAZ`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.AllBooks,
            SortOrder.TitleAZ,
            FilterFormat.All
        )
        assertEquals(4, result.size)
        assertEquals("A Book", result[0].title)
        assertEquals("B Book", result[1].title)
        assertEquals("C Book", result[2].title)
        assertEquals("D Book", result[3].title)
    }
}
