package com.paperscreen.android.reader.parser

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.StringReader

class TxtReaderSearchEngineTest {

    @Test
    fun testTxtSearchWithChunkBoundaryCrossing() = runBlocking {
        // Create a string that is 8192 characters long.
        // We want a search string to cross the 4096 boundary.
        val searchWord = "CROSSING_BOUNDARY_WORD"
        
        val sb = StringBuilder()
        // Fill up to 4090 characters
        while (sb.length < 4096 - 10) {
            sb.append("a")
        }
        // Append the target word so it crosses 4096
        val expectedOffset = sb.length
        sb.append(searchWord)
        
        // Fill some more characters to make it cross chunk size
        while (sb.length < 8192) {
            sb.append("b")
        }
        
        val reader = StringReader(sb.toString())
        val results = ReaderSearchEngine.searchTxt(reader, searchWord).toList()

        assertEquals(1, results.size)
        assertEquals(expectedOffset.toString(), results[0].positionIdentifier)
        assertTrue(results[0].snippet.contains(searchWord))
    }

    @Test
    fun testTxtSearchMultipleMatches() = runBlocking {
        val text = "Find me here. Then find ME again. find me once more at the end."
        val reader = StringReader(text)
        
        val results = ReaderSearchEngine.searchTxt(reader, "find me").toList()

        assertEquals(3, results.size)
        assertEquals(text.indexOf("Find me").toString(), results[0].positionIdentifier)
        assertEquals(text.indexOf("find ME").toString(), results[1].positionIdentifier)
        assertEquals(text.indexOf("find me", startIndex = 20).toString(), results[2].positionIdentifier)
    }
}
