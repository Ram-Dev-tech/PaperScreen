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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PaperReaderDatabase.getDatabase(application)
    private val dao = db.readerDao()

    val allBooks: StateFlow<List<BookEntity>> = dao.getAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentBooks: StateFlow<List<BookEntity>> = dao.getRecentlyAddedBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteBooks: StateFlow<List<BookEntity>> = dao.getFavoriteBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun importDocument(uri: Uri) {
        viewModelScope.launch {
            // Validate and extract metadata
            val bookEntity = DocumentManager.importDocument(getApplication(), uri)
            
            if (bookEntity != null) {
                // Check if already exists based on URI
                val existing = dao.getBookByUri(bookEntity.uriString)
                if (existing == null) {
                    dao.insertBook(bookEntity)
                } else {
                    // Update the existing entry's lastOpenedAt timestamp or other details if needed, 
                    // but do not duplicate.
                    dao.updateBook(existing.copy(lastOpenedAt = System.currentTimeMillis()))
                }
            } else {
                // Handle unsupported file format or error
            }
        }
    }
}
