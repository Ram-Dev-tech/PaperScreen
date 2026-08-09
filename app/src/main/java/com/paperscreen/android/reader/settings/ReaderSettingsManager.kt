package com.paperscreen.android.reader.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.readerDataStore: DataStore<Preferences> by preferencesDataStore(name = "reader_settings")

class ReaderSettingsManager(private val dataStore: DataStore<Preferences>) {
    
    companion object {
        val FONT_SIZE = intPreferencesKey("font_size")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val FONT_WEIGHT = stringPreferencesKey("font_weight")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val PARAGRAPH_SPACING = floatPreferencesKey("paragraph_spacing")
        val MARGINS = stringPreferencesKey("margins")
        val TEXT_WIDTH = stringPreferencesKey("text_width")
        val ALIGNMENT = stringPreferencesKey("alignment")
    }

    val settingsFlow: Flow<ReaderSettings> = dataStore.data.map { preferences ->
        ReaderSettings(
            fontSize = preferences[FONT_SIZE] ?: 18,
            fontFamily = preferences[FONT_FAMILY] ?: "SansSerif",
            fontWeight = preferences[FONT_WEIGHT] ?: "Normal",
            lineSpacing = preferences[LINE_SPACING] ?: 1.4f,
            letterSpacing = preferences[LETTER_SPACING] ?: 0.0f,
            paragraphSpacing = preferences[PARAGRAPH_SPACING] ?: 16.0f,
            margins = preferences[MARGINS] ?: "Normal",
            textWidth = preferences[TEXT_WIDTH] ?: "Full",
            alignment = preferences[ALIGNMENT] ?: "Left"
        )
    }

    suspend fun updateSettings(settings: ReaderSettings) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE] = settings.fontSize
            preferences[FONT_FAMILY] = settings.fontFamily
            preferences[FONT_WEIGHT] = settings.fontWeight
            preferences[LINE_SPACING] = settings.lineSpacing
            preferences[LETTER_SPACING] = settings.letterSpacing
            preferences[PARAGRAPH_SPACING] = settings.paragraphSpacing
            preferences[MARGINS] = settings.margins
            preferences[TEXT_WIDTH] = settings.textWidth
            preferences[ALIGNMENT] = settings.alignment
        }
    }
    
    suspend fun resetToDefault() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
