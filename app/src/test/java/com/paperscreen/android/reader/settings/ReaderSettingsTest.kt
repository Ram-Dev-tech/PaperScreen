package com.paperscreen.android.reader.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ReaderSettingsTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    
    private lateinit var settingsManager: ReaderSettingsManager

    @Before
    fun setup() {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(tmpFolder.newFolder(), "test_reader_settings.preferences_pb") }
        )
        settingsManager = ReaderSettingsManager(dataStore)
    }

    @Test
    fun `default settings are correct`() = testScope.runTest {
        val settings = settingsManager.settingsFlow.first()
        assertEquals(18, settings.fontSize)
        assertEquals("SansSerif", settings.fontFamily)
        assertEquals("Normal", settings.fontWeight)
        assertEquals(1.4f, settings.lineSpacing)
        assertEquals(0.0f, settings.letterSpacing)
        assertEquals(16.0f, settings.paragraphSpacing)
        assertEquals("Normal", settings.margins)
        assertEquals("Full", settings.textWidth)
        assertEquals("Left", settings.alignment)
    }

    @Test
    fun `custom settings can be applied and persisted`() = testScope.runTest {
        val customSettings = ReaderSettings(
            fontSize = 24,
            fontFamily = "Serif",
            fontWeight = "Bold",
            lineSpacing = 1.8f,
            letterSpacing = 0.1f,
            paragraphSpacing = 24.0f,
            margins = "Wide",
            textWidth = "Comfortable",
            alignment = "Justify"
        )
        
        settingsManager.updateSettings(customSettings)
        
        val settings = settingsManager.settingsFlow.first()
        assertEquals(24, settings.fontSize)
        assertEquals("Serif", settings.fontFamily)
        assertEquals("Bold", settings.fontWeight)
        assertEquals(1.8f, settings.lineSpacing)
        assertEquals(0.1f, settings.letterSpacing)
        assertEquals(24.0f, settings.paragraphSpacing)
        assertEquals("Wide", settings.margins)
        assertEquals("Comfortable", settings.textWidth)
        assertEquals("Justify", settings.alignment)
    }
    
    @Test
    fun `resetting to defaults works`() = testScope.runTest {
        val customSettings = ReaderSettings(
            fontSize = 24,
            fontFamily = "Serif",
            fontWeight = "Bold",
            lineSpacing = 1.8f,
            letterSpacing = 0.1f,
            paragraphSpacing = 24.0f,
            margins = "Wide",
            textWidth = "Comfortable",
            alignment = "Justify"
        )
        settingsManager.updateSettings(customSettings)
        
        settingsManager.resetToDefault()
        
        val settings = settingsManager.settingsFlow.first()
        assertEquals(18, settings.fontSize)
        assertEquals("SansSerif", settings.fontFamily)
        assertEquals("Normal", settings.fontWeight)
        assertEquals(1.4f, settings.lineSpacing)
        assertEquals(0.0f, settings.letterSpacing)
        assertEquals(16.0f, settings.paragraphSpacing)
        assertEquals("Normal", settings.margins)
        assertEquals("Full", settings.textWidth)
        assertEquals("Left", settings.alignment)
    }
}
