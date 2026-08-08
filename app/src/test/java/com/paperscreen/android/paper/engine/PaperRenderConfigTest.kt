package com.paperscreen.android.paper.engine

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
class PaperRenderConfigTest {

    @Test
    fun `default config values are correct`() {
        val config = PaperRenderConfig()
        
        // Assert Light Color is #D8D6CF (RGB: 216, 214, 207)
        assertEquals(216 / 255f, config.lightColor.red, 0.01f)
        assertEquals(214 / 255f, config.lightColor.green, 0.01f)
        assertEquals(207 / 255f, config.lightColor.blue, 0.01f)

        // Assert Dark Color is #444444 (RGB: 68, 68, 68)
        assertEquals(68 / 255f, config.darkColor.red, 0.01f)
        assertEquals(68 / 255f, config.darkColor.green, 0.01f)
        assertEquals(68 / 255f, config.darkColor.blue, 0.01f)

        // Assert Threshold and Strength Defaults
        assertEquals(0.52f, config.threshold, 0.001f)
        assertEquals(1.0f, config.strength, 0.001f)
        
        // Assert Rendering Mode Default
        assertEquals(PaperRenderMode.TWO_TONE, config.mode)
    }

    @Test
    fun `hex parsing parses valid colors correctly`() {
        val parsedLight = PaperColor.fromHex("#D8D6CF", Color.Red)
        assertEquals(PaperColor.DefaultLight, parsedLight)

        val parsedDark = PaperColor.fromHex("#444444", Color.White)
        assertEquals(PaperColor.DefaultDark, parsedDark)
    }

    @Test
    fun `hex parsing falls back on invalid input`() {
        val fallback = Color.Cyan
        val parsed = PaperColor.fromHex("invalid-hex", fallback)
        assertEquals(fallback, parsed)
    }

    @Test
    fun `color formatting returns valid hex string`() {
        val hexLight = PaperColor.toHex(PaperColor.DefaultLight)
        assertEquals("#D8D6CF", hexLight.uppercase())

        val hexDark = PaperColor.toHex(PaperColor.DefaultDark)
        assertEquals("#444444", hexDark.uppercase())
    }
}
