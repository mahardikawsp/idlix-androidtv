package com.idlix.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseIniParserTest {

    private val sampleIni = """
        [TV]
        Version=1.0.5
        Link=https://github.com/samdgea/idlix-androidtv/releases/download/1.05/com.idlix.tv.apk

        [Mobile]
        Version=1.0.2
        Link=https://github.com/samdgea/idlix-androidtv/releases/download/1.05/com.idlix.mobile.apk
    """.trimIndent()

    @Test
    fun `parse extracts TV section accurately`() {
        val result = ReleaseIniParser.parse(sampleIni, AppPlatform.TV)
        assertNotNull(result)
        assertEquals("1.0.5", result?.version)
        assertEquals(
            "https://github.com/samdgea/idlix-androidtv/releases/download/1.05/com.idlix.tv.apk",
            result?.link
        )
    }

    @Test
    fun `parse extracts Mobile section accurately`() {
        val result = ReleaseIniParser.parse(sampleIni, AppPlatform.MOBILE)
        assertNotNull(result)
        assertEquals("1.0.2", result?.version)
        assertEquals(
            "https://github.com/samdgea/idlix-androidtv/releases/download/1.05/com.idlix.mobile.apk",
            result?.link
        )
    }

    @Test
    fun `parse handles comments whitespace and BOM`() {
        val messyIni = "\uFEFF" + """
            # Release configuration
            ; Generated file
            [tv]
            Version = "1.0.6"
            Link = https://example.com/tv.apk#download
        """.trimIndent()

        val result = ReleaseIniParser.parse(messyIni, AppPlatform.TV)
        assertNotNull(result)
        assertEquals("1.0.6", result?.version)
        assertEquals("https://example.com/tv.apk#download", result?.link)
        assertNull(ReleaseIniParser.parse(messyIni, AppPlatform.MOBILE))
    }

    @Test
    fun `isUpdateRequired returns false when versions match and true when lower or mismatched`() {
        assertFalse(ReleaseIniParser.isUpdateRequired("1.0.5", "1.0.5"))
        assertFalse(ReleaseIniParser.isUpdateRequired("v1.0.5", "1.0.5"))
        assertTrue(ReleaseIniParser.isUpdateRequired("1.0.4", "1.0.5"))
        assertTrue(ReleaseIniParser.isUpdateRequired("1.0.1", "1.0.2"))
        assertTrue(ReleaseIniParser.isUpdateRequired("1.0.2", "1.0.5"))
    }
}

