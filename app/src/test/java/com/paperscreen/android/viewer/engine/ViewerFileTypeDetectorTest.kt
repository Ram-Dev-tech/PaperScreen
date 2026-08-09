package com.paperscreen.android.viewer.engine

import com.paperscreen.android.viewer.model.DocumentType
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewerFileTypeDetectorTest {

    @Test
    fun testDetectPdf() {
        assertEquals(DocumentType.PDF, ViewerFileTypeDetector.detect("application/pdf", null))
        assertEquals(DocumentType.PDF, ViewerFileTypeDetector.detect(null, "document.pdf"))
        assertEquals(DocumentType.PDF, ViewerFileTypeDetector.detect("application/octet-stream", "report.PDF"))
    }

    @Test
    fun testDetectTxt() {
        assertEquals(DocumentType.TXT, ViewerFileTypeDetector.detect("text/plain", null))
        assertEquals(DocumentType.TXT, ViewerFileTypeDetector.detect(null, "notes.txt"))
    }

    @Test
    fun testDetectEpub() {
        assertEquals(DocumentType.EPUB, ViewerFileTypeDetector.detect("application/epub+zip", null))
        assertEquals(DocumentType.EPUB, ViewerFileTypeDetector.detect(null, "book.epub"))
    }

    @Test
    fun testDetectImage() {
        assertEquals(DocumentType.IMAGE, ViewerFileTypeDetector.detect("image/jpeg", null))
        assertEquals(DocumentType.IMAGE, ViewerFileTypeDetector.detect("image/png", null))
        assertEquals(DocumentType.IMAGE, ViewerFileTypeDetector.detect(null, "photo.jpg"))
        assertEquals(DocumentType.IMAGE, ViewerFileTypeDetector.detect(null, "photo.jpeg"))
        assertEquals(DocumentType.IMAGE, ViewerFileTypeDetector.detect(null, "photo.png"))
        assertEquals(DocumentType.IMAGE, ViewerFileTypeDetector.detect(null, "photo.webp"))
    }

    @Test
    fun testDetectUnsupported() {
        assertEquals(DocumentType.UNSUPPORTED, ViewerFileTypeDetector.detect("application/msword", null))
        assertEquals(DocumentType.UNSUPPORTED, ViewerFileTypeDetector.detect(null, "document.docx"))
        assertEquals(DocumentType.UNSUPPORTED, ViewerFileTypeDetector.detect(null, null))
        assertEquals(DocumentType.UNSUPPORTED, ViewerFileTypeDetector.detect("application/octet-stream", "unknown.bin"))
    }
}
