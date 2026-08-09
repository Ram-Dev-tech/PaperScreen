package com.paperscreen.android.reader.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderSettingsTest {

    @Test
    fun `default settings are correct`() {
        val settings = ReaderSettings()
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
    fun `custom settings can be applied`() {
        val settings = ReaderSettings(
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
}
