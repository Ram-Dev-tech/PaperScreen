package com.paperscreen.android.reader.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.reader.data.BookEntity
import com.paperscreen.android.reader.data.PaperReaderDatabase
import com.paperscreen.android.reader.parser.EpubReaderEngineFacade
import com.paperscreen.android.reader.parser.PdfReaderEngineFacade
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
                    "PDF" -> PdfReaderEngineFacade(getApplication(), uri)
                    else -> throw IllegalArgumentException("Unsupported file type")
                }
                
                currentEngine = engine
                val opened = engine.open()
                if (!opened) {
                    _state.value = ReaderState.Error("Failed to open document")
                    return@launch
                }

                if (currentBook!!.fileType == "PDF") {
                    val pdfEngine = engine as PdfReaderEngineFacade
                    _state.value = ReaderState.Success(
                        title = currentBook!!.title,
                        fileType = "PDF",
                        engine = engine,
                        initialPosition = currentBook!!.currentPosition,
                        pageCount = pdfEngine.getPageOrChapterCount()
                    )
                } else if (currentBook!!.fileType == "EPUB") {
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
                e.printStackTrace()
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
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertBookmark(
                com.paperscreen.android.reader.data.BookmarkEntity(
                    bookId = book.id,
                    position = book.currentPosition,
                    label = label
                )
            )
        }
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
