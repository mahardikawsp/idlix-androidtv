package com.idlix.data

import com.idlix.data.util.ColorMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ColorMathTest {

    @Test
    fun testHsvToArgbAndBack() {
        // Red
        val red = ColorMath.hsvToArgb(0f, 1f, 1f, 1f)
        val hsvRed = ColorMath.argbToHsv(red)
        assertEquals(0f, hsvRed[0], 1f)
        assertEquals(1f, hsvRed[1], 0.01f)
        assertEquals(1f, hsvRed[2], 0.01f)

        // Green
        val green = ColorMath.hsvToArgb(120f, 1f, 1f, 1f)
        val hsvGreen = ColorMath.argbToHsv(green)
        assertEquals(120f, hsvGreen[0], 1f)
        assertEquals(1f, hsvGreen[1], 0.01f)
        assertEquals(1f, hsvGreen[2], 0.01f)

        // Pure Black
        val black = ColorMath.hsvToArgb(0f, 0f, 0f, 1f)
        assertEquals(0xFF000000.toInt(), black)

        // Pure White
        val white = ColorMath.hsvToArgb(0f, 0f, 1f, 1f)
        assertEquals(0xFFFFFFFF.toInt(), white)
    }

    @Test
    fun testHexFormattingAndParsing() {
        val color = 0xFFFF5722.toInt()
        val hexNoAlpha = ColorMath.toHex(color, includeAlpha = false)
        assertEquals("#FF5722", hexNoAlpha)

        val hexWithAlpha = ColorMath.toHex(color, includeAlpha = true)
        assertEquals("#FFFF5722", hexWithAlpha)

        val parsed6 = ColorMath.parseHex("#FF5722")
        assertEquals(color, parsed6)

        val parsed8 = ColorMath.parseHex("#FFFF5722")
        assertEquals(color, parsed8)

        assertNull(ColorMath.parseHex("invalid"))
        assertNull(ColorMath.parseHex("#12"))
    }

    @Test
    fun testAlphaModifier() {
        val color = 0xFF000000.toInt()
        val halfAlpha = ColorMath.withAlpha(color, 0.5f)
        val alphaChannel = (halfAlpha ushr 24) and 0xFF
        assertEquals(128, alphaChannel)
    }
}

