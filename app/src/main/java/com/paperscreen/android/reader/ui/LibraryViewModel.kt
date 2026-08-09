package com.paperscreen.android.reader.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.reader.data.BookEntity
import com.paperscreen.android.reader.data.DocumentManager
import com.paperscreen.android.reader.data.PaperReaderDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibrarySection(val label: String) {
    Recent("Recent"),
    ContinueReading("Continue Reading"),
    Favorites("Favorites"),
    Completed("Completed"),
    AllBooks("All Books")
}

enum class SortOrder(val label: String) {
    Recent("Recently Opened"),
    TitleAZ("Title A-Z")
}

enum class FilterFormat(val label: String) {
    All("All Formats"),
    EPUB("EPUB"),
    PDF("PDF"),
    TXT("TXT")
}

data class LibraryUiState(
    val books: List<BookEntity> = emptyList(),
    val section: LibrarySection = LibrarySection.Recent,
    val sortOrder: SortOrder = SortOrder.Recent,
    val filterFormat: FilterFormat = FilterFormat.All,
    val isLoading: Boolean = true
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PaperReaderDatabase.getDatabase(application)
    private val dao = db.readerDao()

    private val _section = MutableStateFlow(LibrarySection.Recent)
    private val _sortOrder = MutableStateFlow(SortOrder.Recent)
    private val _filterFormat = MutableStateFlow(FilterFormat.All)

    val uiState: StateFlow<LibraryUiState> = combine(
        dao.getAllBooks(),
        _section,
        _sortOrder,
        _filterFormat
    ) { allBooks, section, sort, filter ->
        val filtered = applyFiltersAndSort(allBooks, section, sort, filter)
        LibraryUiState(
            books = filtered,
            section = section,
            sortOrder = sort,
            filterFormat = filter,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    companion object {
        fun applyFiltersAndSort(
            allBooks: List<BookEntity>,
            section: LibrarySection,
            sort: SortOrder,
            filter: FilterFormat
        ): List<BookEntity> {
            var filtered = allBooks

            // Apply Section
            filtered = when (section) {
                LibrarySection.Recent -> filtered.filter { it.lastOpenedAt > 0L } 
                LibrarySection.ContinueReading -> filtered.filter { it.progressPercentage > 0f && it.progressPercentage < 1f }
                LibrarySection.Favorites -> filtered.filter { it.isFavorite }
                LibrarySection.Completed -> filtered.filter { it.progressPercentage >= 1f }
                LibrarySection.AllBooks -> filtered
            }

            // Apply Filter
            filtered = when (filter) {
                FilterFormat.All -> filtered
                FilterFormat.EPUB -> filtered.filter { it.fileType == "EPUB" }
                FilterFormat.PDF -> filtered.filter { it.fileType == "PDF" }
                FilterFormat.TXT -> filtered.filter { it.fileType == "TXT" }
            }

            // Apply Sort
            filtered = when (sort) {
                SortOrder.Recent -> filtered.sortedByDescending { it.lastOpenedAt }
                SortOrder.TitleAZ -> filtered.sortedBy { it.title.lowercase() }
            }
            
            return filtered
        }
    }

    fun setSection(section: LibrarySection) {
        _section.value = section
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun setFilterFormat(filterFormat: FilterFormat) {
        _filterFormat.value = filterFormat
    }

    fun toggleFavorite(book: BookEntity) {
        viewModelScope.launch {
            dao.updateBook(book.copy(isFavorite = !book.isFavorite))
        }
    }

    fun importDocument(uri: Uri) {
        viewModelScope.launch {
            val bookEntity = DocumentManager.importDocument(getApplication(), uri)
            if (bookEntity != null) {
                val existing = dao.getBookByUri(bookEntity.uriString)
                if (existing == null) {
                    dao.insertBook(bookEntity)
                }
            }
        }
    }
}
