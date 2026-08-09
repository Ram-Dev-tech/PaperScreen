package com.paperscreen.android.reader.ui

import com.paperscreen.android.reader.data.BookEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryViewModelTest {

    private val sampleBooks = listOf(
        BookEntity(
            uriString = "uri1",
            title = "A Book",
            fileType = "TXT",
            progressPercentage = 0f,
            isFavorite = false,
            lastOpenedAt = 1000L
        ),
        BookEntity(
            uriString = "uri2",
            title = "B Book",
            fileType = "EPUB",
            progressPercentage = 0.5f,
            isFavorite = true,
            lastOpenedAt = 2000L
        ),
        BookEntity(
            uriString = "uri3",
            title = "C Book",
            fileType = "PDF",
            progressPercentage = 1f,
            isFavorite = false,
            lastOpenedAt = 500L
        )
    )

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
        assertEquals(3, result.size)
        assertEquals("B Book", result[0].title) // 2000L
        assertEquals("A Book", result[1].title) // 1000L
        assertEquals("C Book", result[2].title) // 500L
    }

    @Test
    fun `test sorting TitleAZ`() {
        val result = LibraryViewModel.applyFiltersAndSort(
            sampleBooks,
            LibrarySection.AllBooks,
            SortOrder.TitleAZ,
            FilterFormat.All
        )
        assertEquals(3, result.size)
        assertEquals("A Book", result[0].title)
        assertEquals("B Book", result[1].title)
        assertEquals("C Book", result[2].title)
    }
}
