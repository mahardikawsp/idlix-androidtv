package com.idlix.player.ui

import com.idlix.data.model.SubtitleSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeBoundsTest {

    @Test
    fun testClampEnforcesMinAndMaxBounds() {
        val (clampedMinX, clampedMinY) = SafeBounds.clamp(0.01f, 0.01f, SubtitleSize.MEDIUM)
        assertEquals(SafeBounds.minX(), clampedMinX, 0.001f)
        assertEquals(SafeBounds.minYForSize(SubtitleSize.MEDIUM), clampedMinY, 0.001f)

        val (clampedMaxX, clampedMaxY) = SafeBounds.clamp(0.99f, 0.99f, SubtitleSize.MEDIUM)
        assertEquals(SafeBounds.maxX(), clampedMaxX, 0.001f)
        assertEquals(SafeBounds.maxY(), clampedMaxY, 0.001f)
    }

    @Test
    fun testMinYIncreasesWithLargerSize() {
        val smallMin = SafeBounds.minYForSize(SubtitleSize.SMALL)
        val medMin = SafeBounds.minYForSize(SubtitleSize.MEDIUM)
        val largeMin = SafeBounds.minYForSize(SubtitleSize.LARGE)
        val xlMin = SafeBounds.minYForSize(SubtitleSize.EXTRA_LARGE)

        assertTrue(smallMin < medMin)
        assertTrue(medMin < largeMin)
        assertTrue(largeMin < xlMin)
    }

    @Test
    fun testSnapCenter() {
        val snapped = SafeBounds.snapX(0.505f)
        assertEquals(0.50f, snapped, 0.0001f)

        val notSnapped = SafeBounds.snapX(0.60f)
        assertEquals(0.60f, notSnapped, 0.0001f)
    }
}

