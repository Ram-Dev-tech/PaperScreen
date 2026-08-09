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
                        searchTxt(reader, query).collect { emit(it) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ReaderSearchEngine", "Search failed", e)
            }
        }
    }
    companion object {
        // Extracted for pure JVM testing
        suspend fun searchTxt(reader: java.io.Reader, query: String): Flow<SearchResult> = flow {
            val chunkSize = 4096
            val overlapSize = minOf(query.length * 2, chunkSize)
            val buffer = CharArray(chunkSize + overlapSize)
            
            var absoluteOffsetOfBufferStart = 0L
            var lastEmittedMatchAbsoluteOffset = -1L
            var readOffset = 0
            var charsRead: Int
            
            while (reader.read(buffer, readOffset, chunkSize).also { charsRead = it } != -1) {
                val totalValid = readOffset + charsRead
                val chunkString = String(buffer, 0, totalValid)
                
                var index = chunkString.indexOf(query, ignoreCase = true)
                while (index >= 0) {
                    val matchAbsoluteOffset = absoluteOffsetOfBufferStart + index
                    
                    if (matchAbsoluteOffset > lastEmittedMatchAbsoluteOffset) {
                        val startSnippet = maxOf(0, index - 30)
                        val endSnippet = minOf(chunkString.length, index + query.length + 30)
                        val snippet = chunkString.substring(startSnippet, endSnippet).replace("\n", " ")
                        
                        emit(SearchResult(
                            positionIdentifier = matchAbsoluteOffset.toString(),
                            chapterIndex = 0,
                            snippet = "...$snippet..."
                        ))
                        lastEmittedMatchAbsoluteOffset = matchAbsoluteOffset
                    }
                    
                    index = chunkString.indexOf(query, startIndex = index + query.length, ignoreCase = true)
                }
                
                val charsToKeep = minOf(totalValid, overlapSize)
                if (charsToKeep > 0) {
                    System.arraycopy(buffer, totalValid - charsToKeep, buffer, 0, charsToKeep)
                    absoluteOffsetOfBufferStart += (totalValid - charsToKeep)
                    readOffset = charsToKeep
                } else {
                    absoluteOffsetOfBufferStart += totalValid
                    readOffset = 0
                }
            }
        }
    }
}
