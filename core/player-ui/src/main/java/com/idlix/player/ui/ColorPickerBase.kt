package com.idlix.player.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.idlix.data.util.ColorMath

class ColorPickerState(
    val initialColor: Int,
    initialAlpha: Float = ((initialColor ushr 24) and 0xFF) / 255f
) {
    private val initialHsv = ColorMath.argbToHsv(initialColor)

    var hue by mutableFloatStateOf(initialHsv[0])
    var saturation by mutableFloatStateOf(initialHsv[1])
    var value by mutableFloatStateOf(initialHsv[2])
    var alpha by mutableFloatStateOf(initialAlpha.coerceIn(0f, 1f))

    val currentColor: Int by derivedStateOf {
        ColorMath.hsvToArgb(hue, saturation, value, alpha)
    }

    val currentHex: String by derivedStateOf {
        ColorMath.toHex(currentColor, includeAlpha = alpha < 1.0f)
    }

    fun setColor(color: Int) {
        val hsv = ColorMath.argbToHsv(color)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        alpha = hsv[3]
    }

    fun setHex(hex: String): Boolean {
        val parsed = ColorMath.parseHex(hex) ?: return false
        setColor(parsed)
        return true
    }
}

@Composable
fun rememberColorPickerState(
    initialColor: Int,
    initialAlpha: Float = ((initialColor ushr 24) and 0xFF) / 255f
): ColorPickerState {
    return remember(initialColor) {
        ColorPickerState(initialColor, initialAlpha)
    }
}

@Composable
fun HsvPanel(
    hue: Float,
    saturation: Float,
    value: Float,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false
) {
    val pureHueColor = Color(ColorMath.hsvToArgb(hue, 1f, 1f, 1f))

    Canvas(modifier = modifier) {
        // Horizontal saturation gradient: White -> Pure Hue Color
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, pureHueColor)
            )
        )

        // Vertical value gradient: Transparent -> Black
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black)
            )
        )

        // Selector ring
        val selX = (saturation.coerceIn(0f, 1f) * size.width).coerceIn(0f, size.width)
        val selY = ((1f - value.coerceIn(0f, 1f)) * size.height).coerceIn(0f, size.height)

        drawCircle(
            color = Color.Black,
            radius = 11.dp.toPx(),
            center = Offset(selX, selY),
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = Offset(selX, selY),
            style = Stroke(width = 2.dp.toPx())
        )

        if (isFocused) {
            drawRoundRect(
                color = Color.White,
                size = size,
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun HueBar(
    hue: Float,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false
) {
    val hueColors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF00FFFF),
        Color(0xFF0000FF),
        Color(0xFFFF00FF),
        Color(0xFFFF0000)
    )

    Canvas(modifier = modifier) {
        drawRoundRect(
            brush = Brush.horizontalGradient(hueColors),
            size = size,
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )

        val selX = ((hue % 360f) / 360f) * size.width

        drawCircle(
            color = Color.White,
            radius = size.height / 2f + 2.dp.toPx(),
            center = Offset(selX.coerceIn(0f, size.width), size.height / 2f),
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.Black,
            radius = size.height / 2f - 1.dp.toPx(),
            center = Offset(selX.coerceIn(0f, size.width), size.height / 2f),
            style = Stroke(width = 2.dp.toPx())
        )

        if (isFocused) {
            drawRoundRect(
                color = Color.White,
                size = size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun AlphaBar(
    baseColor: Int,
    alpha: Float,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false
) {
    val opaqueColor = Color(baseColor or 0xFF000000.toInt())

    Canvas(modifier = modifier) {
        // Transparent to opaque gradient
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, opaqueColor)
            ),
            size = size,
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )

        val selX = alpha.coerceIn(0f, 1f) * size.width

        drawCircle(
            color = Color.White,
            radius = size.height / 2f + 2.dp.toPx(),
            center = Offset(selX.coerceIn(0f, size.width), size.height / 2f),
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.Black,
            radius = size.height / 2f - 1.dp.toPx(),
            center = Offset(selX.coerceIn(0f, size.width), size.height / 2f),
            style = Stroke(width = 2.dp.toPx())
        )

        if (isFocused) {
            drawRoundRect(
                color = Color.White,
                size = size,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

