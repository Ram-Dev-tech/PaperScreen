package com.paperscreen.android.viewer.engine

import com.paperscreen.android.viewer.model.DocumentType

object ViewerFileTypeDetector {
    
    fun detect(mimeType: String?, filename: String?): DocumentType {
        val safeMime = mimeType?.lowercase() ?: ""
        val safeName = filename?.lowercase() ?: ""

        if (safeMime == "application/pdf" || safeName.endsWith(".pdf")) {
            return DocumentType.PDF
        }
        
        if (safeMime == "application/epub+zip" || safeName.endsWith(".epub")) {
            return DocumentType.EPUB
        }
        
        if (safeMime.startsWith("image/") || safeName.endsWith(".jpg") || safeName.endsWith(".jpeg") || safeName.endsWith(".png") || safeName.endsWith(".webp")) {
            return DocumentType.IMAGE
        }

        if (safeMime == "text/plain" || safeName.endsWith(".txt")) {
            return DocumentType.TXT
        }

        return DocumentType.UNSUPPORTED
    }
}
