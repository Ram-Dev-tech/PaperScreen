package com.paperscreen.android.reader.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.reader.data.BookEntity
import com.paperscreen.android.reader.data.PaperReaderDatabase
import com.paperscreen.android.reader.parser.EpubReaderEngineFacade

import com.paperscreen.android.reader.parser.ReaderEngine
import com.paperscreen.android.reader.parser.TxtReaderEngineFacade
import com.paperscreen.android.reader.parser.EpubTocItem
import com.paperscreen.android.reader.settings.ReaderSettings
import com.paperscreen.android.reader.settings.ReaderSettingsManager
import com.paperscreen.android.reader.settings.readerDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ReaderState {
    object Loading : ReaderState()
    data class Success(
        val title: String,
        val fileType: String,
        val engine: ReaderEngine,
        val initialPosition: String, // String to be parsed per format
        val contentChunks: List<String> = emptyList(), // For TXT/EPUB
        val contentPositions: List<String> = emptyList(), // To map UI chunk index back to absolute position
        val pageCount: Int = 0, // For PDF
        val chapterIndex: Int = 0, // For EPUB
        val chapterCount: Int = 0, // For EPUB
        val tableOfContents: List<EpubTocItem> = emptyList() // For EPUB
    ) : ReaderState()
    data class Error(val message: String) : ReaderState()
}

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PaperReaderDatabase.getDatabase(application)
    private val dao = db.readerDao()

    private val settingsManager = ReaderSettingsManager(application.readerDataStore)
    val settingsState: StateFlow<ReaderSettings> = settingsManager.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderSettings()
    )

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    private var currentEngine: ReaderEngine? = null
    private var currentBook: BookEntity? = null

    fun loadBook(bookId: Long) {
        _state.value = ReaderState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val book = dao.getBookById(bookId)
                if (book == null) {
                    _state.value = ReaderState.Error("Book not found")
                    return@launch
                }
                
                // Update last opened
                currentBook = book.copy(lastOpenedAt = System.currentTimeMillis())
                dao.updateBook(currentBook!!)

                val uri = Uri.parse(currentBook!!.uriString)
                val engine: ReaderEngine = when (currentBook!!.fileType) {
                    "TXT" -> TxtReaderEngineFacade(getApplication(), uri)
                    "EPUB" -> EpubReaderEngineFacade(getApplication(), uri)
                    else -> throw IllegalArgumentException("Unsupported file type")
                }
                
                currentEngine = engine
                val opened = engine.open()
                if (!opened) {
                    _state.value = ReaderState.Error("Failed to open document")
                    return@launch
                }

                if (currentBook!!.fileType == "EPUB") {
                    val epubEngine = engine as EpubReaderEngineFacade
                    
                    // Parse locator to get chapter index (fallback to 0)
                    var chapterIndex = 0
                    if (currentBook!!.currentPosition.isNotEmpty()) {
                        chapterIndex = epubEngine.getChapterIndexFromLocator(currentBook!!.currentPosition)
                    }

                    val chapter = epubEngine.getChapterContent(chapterIndex) ?: "No content"
                    val stripped = com.paperscreen.android.reader.parser.HtmlUtils.stripHtml(chapter)
                    
                    // Generate a proper Readium Locator JSON string
                    val locatorStr = epubEngine.getLocatorForChapter(chapterIndex) ?: "{}"

                    _state.value = ReaderState.Success(
                        title = currentBook!!.title,
                        fileType = "EPUB",
                        engine = engine,
                        initialPosition = locatorStr,
                        contentChunks = listOf(stripped.text),
                        contentPositions = listOf(locatorStr),
                        chapterIndex = chapterIndex,
                        chapterCount = epubEngine.getPageOrChapterCount(),
                        tableOfContents = epubEngine.getTableOfContents()
                    )
                } else {
                    val txtEngine = engine as TxtReaderEngineFacade
                    val chunks = mutableListOf<String>()
                    val positions = mutableListOf<String>()
                    var offset = 0L
                    
                    if (currentBook!!.currentPosition.isNotEmpty()) {
                        offset = currentBook!!.currentPosition.toLongOrNull() ?: 0L
                    }

                    for (i in 0..50) { // Load more chunks to allow scrolling
                        positions.add(offset.toString())
                        val result = txtEngine.readChunk(offset, 4000)
                        if (result.first.isEmpty()) break
                        chunks.add(result.first)
                        offset = result.second
                        if (offset <= 0) break // End of file
                    }
                    _state.value = ReaderState.Success(
                        title = currentBook!!.title,
                        fileType = "TXT",
                        engine = engine,
                        initialPosition = currentBook!!.currentPosition,
                        contentChunks = chunks,
                        contentPositions = positions
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("ReaderViewModel", "Failed to open document", e)
                _state.value = ReaderState.Error(e.message ?: "Failed to open document")
            }
        }
    }

    fun loadEpubChapter(index: Int) {
        val epubEngine = currentEngine as? EpubReaderEngineFacade ?: return
        val count = epubEngine.getPageOrChapterCount()
        if (index < 0 || index >= count) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val chapter = epubEngine.getChapterContent(index) ?: "No content"
            val stripped = com.paperscreen.android.reader.parser.HtmlUtils.stripHtml(chapter)
            val locatorStr = epubEngine.getLocatorForChapter(index) ?: "{}"
            
            val currentState = _state.value as? ReaderState.Success ?: return@launch
            
            _state.value = currentState.copy(
                initialPosition = locatorStr,
                contentChunks = listOf(stripped.text),
                contentPositions = listOf(locatorStr),
                chapterIndex = index
            )
            
            val progress = index.toFloat() / count.toFloat()
            saveReadingState(locatorStr, progress)
        }
    }

    fun saveReadingState(position: String, progress: Float) {
        val book = currentBook ?: return
        val clampedProgress = progress.coerceIn(0f, 1f)
        val updatedBook = book.copy(
            currentPosition = position,
            progressPercentage = clampedProgress,
            lastOpenedAt = System.currentTimeMillis()
        )
        currentBook = updatedBook
        
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateBook(updatedBook)
        }
    }

    fun addBookmark(label: String) {
        val book = currentBook ?: return
        val pos = book.currentPosition
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getBookmarksForBook(book.id).first().find { it.position == pos }
            if (existing == null) {
                dao.insertBookmark(
                    com.paperscreen.android.reader.data.BookmarkEntity(
                        bookId = book.id,
                        position = pos,
                        label = label
                    )
                )
            }
        }
    }

    fun jumpToPosition(position: String) {
        val engine = currentEngine ?: return
        val book = currentBook ?: return
        if (book.fileType == "EPUB") {
            val epubEngine = engine as EpubReaderEngineFacade
            val chapterIndex = epubEngine.getChapterIndexFromLocator(position)
            loadEpubChapter(chapterIndex)
        } else if (book.fileType == "TXT") {
            // For TXT we re-trigger loading the chunks from the offset
            val offset = position.toLongOrNull() ?: 0L
            val txtEngine = engine as TxtReaderEngineFacade
            viewModelScope.launch(Dispatchers.IO) {
                val chunks = mutableListOf<String>()
                val positions = mutableListOf<String>()
                var currOffset = offset
                
                for (i in 0..50) {
                    positions.add(currOffset.toString())
                    val result = txtEngine.readChunk(currOffset, 4000)
                    if (result.first.isEmpty()) break
                    chunks.add(result.first)
                    currOffset = result.second
                    if (currOffset <= 0) break
                }
                
                val currentState = _state.value as? ReaderState.Success ?: return@launch
                _state.value = currentState.copy(
                    initialPosition = position,
                    contentChunks = chunks,
                    contentPositions = positions
                )
            }
        }
    }

    fun addHighlight(positionIdentifier: String, startIndex: Int, endIndex: Int, selectedText: String) {
        val book = currentBook ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHighlight(
                com.paperscreen.android.reader.data.HighlightEntity(
                    bookId = book.id,
                    fileType = book.fileType,
                    positionIdentifier = positionIdentifier,
                    startIndex = startIndex,
                    endIndex = endIndex,
                    selectedText = selectedText
                )
            )
        }
    }

    fun getHighlightsForPosition(positionIdentifier: String): kotlinx.coroutines.flow.Flow<List<com.paperscreen.android.reader.data.HighlightEntity>> {
        val book = currentBook ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return dao.getHighlightsForPosition(book.id, positionIdentifier)
    }

    fun getBookmarks() = currentBook?.let { dao.getBookmarksForBook(it.id) } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun deleteBookmark(bookmark: com.paperscreen.android.reader.data.BookmarkEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteBookmark(bookmark)
        }
    }

    fun getNotesForHighlight(highlightId: Long) = dao.getNotesForHighlight(highlightId)

    fun addNote(highlightId: Long, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertNote(
                com.paperscreen.android.reader.data.NoteEntity(
                    highlightId = highlightId,
                    text = text
                )
            )
        }
    }

    fun updateNote(note: com.paperscreen.android.reader.data.NoteEntity, newText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateNote(note.copy(text = newText, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: com.paperscreen.android.reader.data.NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteNote(note)
        }
    }

    fun deleteHighlight(highlight: com.paperscreen.android.reader.data.HighlightEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteHighlight(highlight)
        }
    }

    private val _searchResults = MutableStateFlow<List<com.paperscreen.android.reader.parser.SearchResult>>(emptyList())
    val searchResults: StateFlow<List<com.paperscreen.android.reader.parser.SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun searchBook(query: String) {
        val engine = currentEngine ?: return
        _isSearching.value = true
        _searchResults.value = emptyList()
        val searchEngine = com.paperscreen.android.reader.parser.ReaderSearchEngine(getApplication(), engine)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                searchEngine.search(query).collect { result ->
                    val currentList = _searchResults.value.toMutableList()
                    currentList.add(result)
                    _searchResults.value = currentList
                }
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun updateSettings(settings: ReaderSettings) {
        viewModelScope.launch {
            settingsManager.updateSettings(settings)
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            settingsManager.resetToDefault()
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentEngine?.close()
    }
}
