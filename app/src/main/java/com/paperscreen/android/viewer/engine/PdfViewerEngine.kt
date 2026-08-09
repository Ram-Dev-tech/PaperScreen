package com.paperscreen.android.viewer.engine

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.paperscreen.android.reader.parser.PdfParser

class PdfViewerEngine : ViewerEngine {
    private var parser: PdfParser? = null
    
    val pageCount: Int
        get() = parser?.getPageCount() ?: 0

    override suspend fun load(context: Context, uri: Uri): Boolean {
        parser = PdfParser(context, uri)
        return parser?.open() ?: false
    }

    suspend fun renderPage(pageIndex: Int, scaleFactor: Float = 1.0f): Bitmap? {
        return parser?.renderPage(pageIndex, scaleFactor)
    }

    override fun close() {
        parser?.close()
        parser = null
    }
}
