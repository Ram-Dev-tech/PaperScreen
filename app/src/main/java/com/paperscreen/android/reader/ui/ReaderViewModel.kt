package com.paperscreen.android.reader.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.reader.data.PaperReaderDatabase
import com.paperscreen.android.reader.parser.EpubReaderEngineFacade
import com.paperscreen.android.reader.parser.PdfReaderEngineFacade
import com.paperscreen.android.reader.parser.ReaderEngine
import com.paperscreen.android.reader.parser.TxtReaderEngineFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ReaderState {
    object Loading : ReaderState()
    data class Success(
        val title: String,
        val fileType: String,
        val engine: ReaderEngine,
        val contentChunks: List<String> = emptyList(), // For TXT/EPUB
        val pageCount: Int = 0 // For PDF
    ) : ReaderState()
    data class Error(val message: String) : ReaderState()
}

class ReaderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PaperReaderDatabase.getDatabase(application)
    private val dao = db.readerDao()

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state.asStateFlow()

    private var currentEngine: ReaderEngine? = null

    fun loadBook(bookId: Long) {
        _state.value = ReaderState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val book = dao.getBookById(bookId)
                if (book == null) {
                    _state.value = ReaderState.Error("Book not found")
                    return@launch
                }

                val uri = Uri.parse(book.uriString)
                val engine: ReaderEngine = when (book.fileType) {
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

                if (book.fileType == "PDF") {
                    val pdfEngine = engine as PdfReaderEngineFacade
                    _state.value = ReaderState.Success(
                        title = book.title,
                        fileType = "PDF",
                        engine = engine,
                        pageCount = pdfEngine.getPageOrChapterCount()
                    )
                } else if (book.fileType == "EPUB") {
                    val epubEngine = engine as EpubReaderEngineFacade
                    val chapter = epubEngine.getChapterContent(0) ?: "No content"
                    val stripped = com.paperscreen.android.reader.parser.HtmlUtils.stripHtml(chapter)
                    _state.value = ReaderState.Success(
                        title = book.title,
                        fileType = "EPUB",
                        engine = engine,
                        contentChunks = listOf(stripped.text)
                    )
                } else {
                    val txtEngine = engine as TxtReaderEngineFacade
                    // Just read first chunks as a test
                    val chunks = mutableListOf<String>()
                    var offset = 0L
                    for (i in 0..10) {
                        val result = txtEngine.readChunk(offset, 4000)
                        if (result.first.isEmpty()) break
                        chunks.add(result.first)
                        offset = result.second
                        if (offset <= 0) break // End of file
                    }
                    _state.value = ReaderState.Success(
                        title = book.title,
                        fileType = "TXT",
                        engine = engine,
                        contentChunks = chunks
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ReaderState.Error(e.message ?: "Failed to open document")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentEngine?.close()
    }
}
