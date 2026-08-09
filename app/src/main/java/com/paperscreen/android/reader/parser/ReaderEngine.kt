package com.paperscreen.android.reader.parser

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

sealed class ReaderEngine {
    abstract suspend fun open(): Boolean
    abstract fun getPageOrChapterCount(): Int
    abstract fun getTitle(): String?
    abstract fun close()
}

class TxtReaderEngineFacade(private val context: Context, private val uri: Uri) : ReaderEngine() {
    private var size: Long = 0
    override suspend fun open(): Boolean {
        size = TxtParser.getTotalSize(context, uri)
        return size > 0
    }
    
    suspend fun readChunk(offset: Long, maxChars: Int): Pair<String, Long> {
        return TxtParser.readTextChunk(context, uri, offset, maxChars)
    }

    override fun getPageOrChapterCount(): Int = 1 // Single continuous scroll
    override fun getTitle(): String? = null
    override fun close() {}
}

class PdfReaderEngineFacade(context: Context, uri: Uri) : ReaderEngine() {
    private val parser = PdfParser(context, uri)

    override suspend fun open(): Boolean = parser.open()
    override fun getPageOrChapterCount(): Int = parser.getPageCount()
    override fun getTitle(): String? = null
    override fun close() = parser.close()
    
    suspend fun renderPage(pageIndex: Int, scaleFactor: Float = 1.0f): Bitmap? {
        return parser.renderPage(pageIndex, scaleFactor)
    }
}

class EpubReaderEngineFacade(context: Context, uri: Uri) : ReaderEngine() {
    private val engine = EpubReaderEngine(context, uri)

    override suspend fun open(): Boolean = engine.open()
    override fun getPageOrChapterCount(): Int = engine.getChapterCount()
    override fun getTitle(): String? = engine.getTitle()
    override fun close() = engine.close()

    suspend fun getChapterContent(index: Int): String? = engine.getChapterContent(index)
    fun getChapterTitle(index: Int): String? = engine.getChapterTitle(index)
    fun getLocatorForChapter(index: Int): String? = engine.getLocatorForChapter(index)
    fun getChapterIndexFromLocator(json: String): Int = engine.getChapterIndexFromLocator(json)
}
