package com.idlix.player.ui

import com.idlix.data.model.PlayerAppearance
import com.idlix.data.model.SubtitleSize

object SafeBounds {

    const val SAFE_MARGIN_HORIZONTAL = 0.05f
    const val SAFE_MARGIN_BOTTOM = 0.05f
    const val CENTER_X = 0.50f
    const val SNAP_THRESHOLD_X = 0.018f

    fun minYForSize(size: SubtitleSize): Float {
        return when (size) {
            SubtitleSize.SMALL -> 0.08f
            SubtitleSize.MEDIUM -> 0.10f
            SubtitleSize.LARGE -> 0.12f
            SubtitleSize.EXTRA_LARGE -> 0.15f
        }
    }

    fun maxY(): Float = 0.95f

    fun minX(): Float = 0.15f
    fun maxX(): Float = 0.85f

    fun clamp(
        x: Float,
        y: Float,
        size: SubtitleSize = SubtitleSize.MEDIUM
    ): Pair<Float, Float> {
        val clampedX = x.coerceIn(minX(), maxX())
        val clampedY = y.coerceIn(minYForSize(size), maxY())
        return Pair(clampedX, clampedY)
    }

    fun snapX(x: Float): Float {
        return if (kotlin.math.abs(x - CENTER_X) <= SNAP_THRESHOLD_X) {
            CENTER_X
        } else {
            x
        }
    }
}

