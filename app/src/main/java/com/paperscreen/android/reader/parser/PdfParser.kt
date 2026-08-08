package com.paperscreen.android.reader.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfParser(private val context: Context, private val uri: Uri) {
    
    private var pfd: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    
    suspend fun open(): Boolean = withContext(Dispatchers.IO) {
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                pdfRenderer = PdfRenderer(pfd!!)
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    fun getPageCount(): Int {
        return pdfRenderer?.pageCount ?: 0
    }

    suspend fun renderPage(pageIndex: Int, scaleFactor: Float = 1.0f): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val renderer = pdfRenderer ?: return@withContext null
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

            renderer.openPage(pageIndex).use { page ->
                val width = (page.width * scaleFactor).toInt()
                val height = (page.height * scaleFactor).toInt()
                
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                // Fill with white background to prevent transparent black background rendering
                bitmap.eraseColor(android.graphics.Color.WHITE)
                
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return@withContext bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun close() {
        pdfRenderer?.close()
        pfd?.close()
    }
}
