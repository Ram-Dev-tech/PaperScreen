package com.paperscreen.android.dictionary.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class WordNormalizerTest {

    @Test
    fun testWordNormalization_basic() {
        assertEquals("hello", WordNormalizer.normalize("Hello"))
        assertEquals("world", WordNormalizer.normalize("WORLD"))
    }

    @Test
    fun testWordNormalization_punctuationHandling() {
        assertEquals("word", WordNormalizer.normalize("\"word\""))
        assertEquals("test", WordNormalizer.normalize("test."))
        assertEquals("hello", WordNormalizer.normalize(",hello!"))
        assertEquals("quotes", WordNormalizer.normalize("'quotes'"))
    }

    @Test
    fun testWordNormalization_apostrophes() {
        // Internal apostrophes should be preserved, surrounding removed
        assertEquals("don't", WordNormalizer.normalize("don't"))
        assertEquals("can't", WordNormalizer.normalize("\"can't\"."))
        assertEquals("o'clock", WordNormalizer.normalize("O'clock!"))
    }

    @Test
    fun testWordNormalization_unicodeWords() {
        assertEquals("नमस्ते", WordNormalizer.normalize("नमस्ते"))
        assertEquals("कुत्ता", WordNormalizer.normalize("\"कुत्ता\"."))
        assertEquals("résumé", WordNormalizer.normalize("Résumé!"))
    }
    
    @Test
    fun testWordNormalization_emptyOrPunctuationOnly() {
        assertEquals("", WordNormalizer.normalize(""))
        assertEquals("", WordNormalizer.normalize("..."))
        assertEquals("", WordNormalizer.normalize("\"\""))
    }
}
