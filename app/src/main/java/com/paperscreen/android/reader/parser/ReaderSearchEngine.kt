package com.paperscreen.android.reader.parser

import android.app.Application
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStreamReader

data class SearchResult(
    val positionIdentifier: String, // TXT offset or EPUB Locator
    val chapterIndex: Int, // for UI display
    val snippet: String
)

class ReaderSearchEngine(private val application: Application, private val engine: ReaderEngine) {

    fun search(query: String): Flow<SearchResult> = flow {
        if (query.isBlank()) return@flow

        if (engine is EpubReaderEngineFacade) {
            val count = engine.getPageOrChapterCount()
            for (i in 0 until count) {
                val htmlContent = engine.getChapterContent(i) ?: continue
                val textContent = HtmlUtils.stripHtml(htmlContent).text
                
                var index = textContent.indexOf(query, ignoreCase = true)
                while (index >= 0) {
                    val startSnippet = maxOf(0, index - 30)
                    val endSnippet = minOf(textContent.length, index + query.length + 30)
                    val snippet = textContent.substring(startSnippet, endSnippet).replace("\n", " ")
                    val locatorStr = engine.getLocatorForChapter(i) ?: "{}"
                    
                    emit(SearchResult(positionIdentifier = locatorStr, chapterIndex = i, snippet = "...$snippet..."))
                    index = textContent.indexOf(query, startIndex = index + query.length, ignoreCase = true)
                }
            }
        } else if (engine is TxtReaderEngineFacade) {
            val uriStr = (engine.javaClass.getDeclaredField("uri").apply { isAccessible = true }.get(engine) as Uri)
            val uri = uriStr
            try {
                application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val chunkSize = 4096
                        val overlapSize = query.length * 2
                        val buffer = CharArray(chunkSize + overlapSize)
                        
                        var charsRead: Int
                        var currentAbsoluteOffset = 0L
                        
                        var readOffset = 0
                        
                        while (reader.read(buffer, readOffset, chunkSize).also { charsRead = it } != -1) {
                            val totalToSearch = readOffset + charsRead
                            val chunkString = String(buffer, 0, totalToSearch)
                            
                            var index = chunkString.indexOf(query, ignoreCase = true)
                            while (index >= 0 && index < charsRead) {
                                // Match found
                                val startSnippet = maxOf(0, index - 30)
                                val endSnippet = minOf(chunkString.length, index + query.length + 30)
                                val snippet = chunkString.substring(startSnippet, endSnippet).replace("\n", " ")
                                
                                val matchAbsoluteOffset = currentAbsoluteOffset + index
                                emit(SearchResult(positionIdentifier = matchAbsoluteOffset.toString(), chapterIndex = 0, snippet = "...$snippet..."))
                                
                                index = chunkString.indexOf(query, startIndex = index + query.length, ignoreCase = true)
                            }
                            
                            currentAbsoluteOffset += charsRead
                            
                            // Copy overlap to beginning of buffer
                            if (totalToSearch > overlapSize) {
                                System.arraycopy(buffer, totalToSearch - overlapSize, buffer, 0, overlapSize)
                                readOffset = overlapSize
                            } else {
                                readOffset = 0
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
