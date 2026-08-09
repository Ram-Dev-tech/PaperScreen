package com.paperscreen.android.dictionary.engine

object WordNormalizer {
    /**
     * Normalizes a word for dictionary lookup:
     * - Trims whitespace
     * - Removes surrounding punctuation
     * - Converts to lowercase
     * - Preserves internal characters like apostrophes (e.g. "don't")
     */
    fun normalize(rawWord: String): String {
        val trimmed = rawWord.trim()
        
        // Remove leading/trailing punctuation but preserve letters, numbers, and combining marks (\p{M})
        return trimmed.replace(Regex("^[^\\p{L}\\p{N}\\p{M}]+|[^\\p{L}\\p{N}\\p{M}]+$"), "").lowercase()
    }
}
