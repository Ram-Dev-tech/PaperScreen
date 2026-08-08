package com.paperscreen.android.papermode

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class PaperModeType {
    PAPER, GRAYSCALE, ORIGINAL
}

enum class DitheringMode {
    LOW, MEDIUM, HIGH
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "papermode_settings")

class PaperModeSettingsManager(private val context: Context) {
    
    companion object {
        val MASTER_PAPER_MODE_ENABLED = booleanPreferencesKey("master_paper_mode_enabled")
        val PAPER_MODE_TYPE = stringPreferencesKey("paper_mode_type")
        val CONTRAST = intPreferencesKey("contrast")
        val BRIGHTNESS = intPreferencesKey("brightness")
        val STRENGTH = intPreferencesKey("strength")
        val THRESHOLD = intPreferencesKey("threshold")
        val DITHERING = stringPreferencesKey("dithering")
    }

    val masterPaperModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MASTER_PAPER_MODE_ENABLED] ?: true
        }

    val paperModeType: Flow<PaperModeType> = context.dataStore.data
        .map { preferences ->
            PaperModeType.valueOf(preferences[PAPER_MODE_TYPE] ?: PaperModeType.PAPER.name)
        }

    val contrast: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[CONTRAST] ?: 60
        }

    val brightness: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[BRIGHTNESS] ?: 70
        }

    val strength: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[STRENGTH] ?: 80
        }

    val threshold: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[THRESHOLD] ?: 50
        }

    val dithering: Flow<DitheringMode> = context.dataStore.data
        .map { preferences ->
            DitheringMode.valueOf(preferences[DITHERING] ?: DitheringMode.MEDIUM.name)
        }



    suspend fun setMasterPaperModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MASTER_PAPER_MODE_ENABLED] = enabled
        }
    }

    suspend fun setPaperModeType(type: PaperModeType) {
        context.dataStore.edit { preferences ->
            preferences[PAPER_MODE_TYPE] = type.name
        }
    }

    suspend fun setContrast(contrastValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[CONTRAST] = contrastValue
        }
    }

    suspend fun setBrightness(brightnessValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[BRIGHTNESS] = brightnessValue
        }
    }

    suspend fun setStrength(strengthValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[STRENGTH] = strengthValue
        }
    }

    suspend fun setThreshold(thresholdValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[THRESHOLD] = thresholdValue
        }
    }

    suspend fun setDithering(ditheringMode: DitheringMode) {
        context.dataStore.edit { preferences ->
            preferences[DITHERING] = ditheringMode.name
        }
    }
}
