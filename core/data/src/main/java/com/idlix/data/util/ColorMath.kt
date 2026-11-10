package com.idlix.data.util

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ColorMath {

    fun hsvToArgb(
        hue: Float,
        saturation: Float,
        value: Float,
        alpha: Float = 1.0f
    ): Int {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val a = (alpha.coerceIn(0f, 1f) * 255f).roundToInt()

        val c = v * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = v - c

        val (rPrime, gPrime, bPrime) = when ((h / 60f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = ((rPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        val g = ((gPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        val b = ((bPrime + m) * 255f).roundToInt().coerceIn(0, 255)

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun argbToHsv(color: Int): FloatArray {
        val a = ((color ushr 24) and 0xFF) / 255f
        val r = ((color ushr 16) and 0xFF) / 255f
        val g = ((color ushr 8) and 0xFF) / 255f
        val b = (color and 0xFF) / 255f

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        val delta = max - min

        val v = max
        val s = if (max == 0f) 0f else delta / max

        val h = if (delta == 0f) {
            0f
        } else {
            val rawH = when (max) {
                r -> ((g - b) / delta) % 6f
                g -> ((b - r) / delta) + 2f
                else -> ((r - g) / delta) + 4f
            } * 60f
            if (rawH < 0f) rawH + 360f else rawH
        }

        return floatArrayOf(h, s, v, a)
    }

    fun withAlpha(color: Int, alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255f).roundToInt()
        return (color and 0x00FFFFFF) or (a shl 24)
    }

    fun toHex(color: Int, includeAlpha: Boolean = false): String {
        return if (includeAlpha) {
            String.format("#%08X", color)
        } else {
            String.format("#%06X", color and 0x00FFFFFF)
        }
    }

    fun parseHex(hex: String): Int? {
        val cleaned = hex.trim().removePrefix("#")
        return try {
            when (cleaned.length) {
                6 -> {
                    val rgb = cleaned.toLong(16).toInt()
                    0xFF000000.toInt() or rgb
                }
                8 -> cleaned.toLong(16).toInt()
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

