package com.paperscreen.android.viewer.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.viewer.engine.PdfViewerEngine
import com.paperscreen.android.viewer.engine.TxtViewerEngine
import com.paperscreen.android.viewer.engine.ViewerFileTypeDetector
import com.paperscreen.android.viewer.model.DocumentType
import com.paperscreen.android.viewer.model.ViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaperViewerViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<ViewerState>(ViewerState.Loading)
    val state: StateFlow<ViewerState> = _state.asStateFlow()

    private var currentEngine: com.paperscreen.android.viewer.engine.ViewerEngine? = null
    var currentUri: Uri? = null
        private set

    fun loadDocument(uriString: String, mimeType: String? = null) {
        val uri = Uri.parse(uriString)
        currentUri = uri
        val filename = getFilenameFromUri(uri)
        val docType = ViewerFileTypeDetector.detect(mimeType, filename)

        viewModelScope.launch {
            when (docType) {
                DocumentType.PDF -> {
                    val engine = PdfViewerEngine()
                    if (engine.load(getApplication(), uri)) {
                        currentEngine = engine
                        _state.value = ViewerState.Pdf(engine)
                    } else {
                        _state.value = ViewerState.Error("Unable to open document")
                    }
                }
                DocumentType.TXT -> {
                    val engine = TxtViewerEngine()
                    if (engine.load(getApplication(), uri)) {
                        currentEngine = engine
                        _state.value = ViewerState.Txt(engine)
                    } else {
                        _state.value = ViewerState.Error("Unable to open document")
                    }
                }
                DocumentType.IMAGE -> {
                    _state.value = ViewerState.Image(uri)
                }
                DocumentType.EPUB -> {
                    // EPUB is bridged to Reader outside this ViewModel
                    _state.value = ViewerState.Unsupported // Should not reach here if bridged at UI
                }
                DocumentType.UNSUPPORTED -> {
                    _state.value = ViewerState.Unsupported
                }
            }
        }
    }

    private fun getFilenameFromUri(uri: Uri): String {
        var result = "document"
        val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    result = it.getString(displayNameIndex)
                }
            }
        }
        return result
    }
    
    fun getFileSize(): Long {
        currentUri?.let { uri ->
            val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        return it.getLong(sizeIndex)
                    }
                }
            }
        }
        return 0L
    }

    fun getFilename(): String {
        return currentUri?.let { getFilenameFromUri(it) } ?: "document"
    }

    override fun onCleared() {
        super.onCleared()
        currentEngine?.close()
    }
}
