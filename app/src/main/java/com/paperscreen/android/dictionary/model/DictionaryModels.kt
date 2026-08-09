package com.paperscreen.android.dictionary.model

data class DictionaryLanguage(
    val code: String,
    val displayName: String,
    val isInstalled: Boolean = false,
    val isDefault: Boolean = false
)

data class DictionaryDefinition(
    val word: String,
    val partOfSpeech: String?,
    val definition: String,
    val pronunciation: String?,
    val example: String?
)
