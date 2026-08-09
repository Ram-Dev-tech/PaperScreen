package com.paperscreen.android.viewer.engine

import android.content.Context
import android.net.Uri
import com.paperscreen.android.reader.parser.TxtParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TxtViewerEngine : ViewerEngine {
    private var context: Context? = null
    private var uri: Uri? = null
    
    private val _totalSize = MutableStateFlow(0L)
    val totalSize: StateFlow<Long> = _totalSize.asStateFlow()

    override suspend fun load(context: Context, uri: Uri): Boolean {
        this.context = context
        this.uri = uri
        _totalSize.value = TxtParser.getTotalSize(context, uri)
        return true
    }

    suspend fun loadChunk(offset: Long, maxChars: Int): Pair<String, Long> {
        val ctx = context ?: return Pair("", offset)
        val fileUri = uri ?: return Pair("", offset)
        return TxtParser.readTextChunk(ctx, fileUri, offset, maxChars)
    }

    override fun close() {
        context = null
        uri = null
    }
}
