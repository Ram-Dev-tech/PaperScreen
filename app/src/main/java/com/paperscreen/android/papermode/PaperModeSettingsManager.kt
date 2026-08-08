package com.paperscreen.android.papermode

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paperscreen.android.paper.engine.PaperColor
import com.paperscreen.android.paper.engine.PaperRenderConfig
import com.paperscreen.android.paper.engine.PaperRenderMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        val LIGHT_COLOR = stringPreferencesKey("light_color")
        val DARK_COLOR = stringPreferencesKey("dark_color")
        val DITHERING = stringPreferencesKey("dithering")
    }

    val masterPaperModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MASTER_PAPER_MODE_ENABLED] ?: true
        }

    val contrast: Flow<Int> = context.dataStore.data.map { it[CONTRAST] ?: 60 }
    val brightness: Flow<Int> = context.dataStore.data.map { it[BRIGHTNESS] ?: 70 }
    val strength: Flow<Int> = context.dataStore.data.map { it[STRENGTH] ?: 80 }
    val threshold: Flow<Int> = context.dataStore.data.map { it[THRESHOLD] ?: 52 }
    val dithering: Flow<DitheringMode> = context.dataStore.data.map { 
        DitheringMode.valueOf(it[DITHERING] ?: DitheringMode.MEDIUM.name) 
    }

    val paperRenderConfig: Flow<PaperRenderConfig> = context.dataStore.data
        .map { preferences ->
            val mode = try {
                PaperRenderMode.valueOf(preferences[PAPER_MODE_TYPE] ?: PaperRenderMode.TWO_TONE.name)
            } catch (e: Exception) {
                PaperRenderMode.TWO_TONE
            }

            val lightHex = preferences[LIGHT_COLOR] ?: PaperColor.toHex(PaperColor.DefaultLight)
            val darkHex = preferences[DARK_COLOR] ?: PaperColor.toHex(PaperColor.DefaultDark)

            val rawThreshold = preferences[THRESHOLD] ?: 52
            val rawStrength = preferences[STRENGTH] ?: 100
            val rawBrightness = preferences[BRIGHTNESS] ?: 50 // Assume 50 is neutral for UI slider
            val rawContrast = preferences[CONTRAST] ?: 50     // Assume 50 is neutral for UI slider

            PaperRenderConfig(
                lightColor = PaperColor.fromHex(lightHex, PaperColor.DefaultLight),
                darkColor = PaperColor.fromHex(darkHex, PaperColor.DefaultDark),
                threshold = rawThreshold / 100f,
                strength = rawStrength / 100f,
                brightness = (rawBrightness - 50f) / 100f,
                contrast = rawContrast / 50f,
                mode = mode
            )
        }

    // Individual setters
    suspend fun setMasterPaperModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MASTER_PAPER_MODE_ENABLED] = enabled
        }
    }

    suspend fun setPaperRenderMode(mode: PaperRenderMode) {
        context.dataStore.edit { preferences ->
            preferences[PAPER_MODE_TYPE] = mode.name
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

    suspend fun setLightColor(hex: String) {
        context.dataStore.edit { preferences ->
            preferences[LIGHT_COLOR] = hex
        }
    }

    suspend fun setDarkColor(hex: String) {
        context.dataStore.edit { preferences ->
            preferences[DARK_COLOR] = hex
        }
    }
    
    suspend fun setDithering(ditheringMode: DitheringMode) {
        context.dataStore.edit { preferences ->
            preferences[DITHERING] = ditheringMode.name
        }
    }
}
