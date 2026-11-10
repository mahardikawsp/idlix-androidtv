package com.idlix.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.idlix.data.local.PlayerAppearanceStore
import com.idlix.data.model.PlayerAppearance
import com.idlix.data.model.SubtitleSize

class PositionEditorState(
    initialX: Float,
    initialY: Float,
    val subtitleSize: SubtitleSize = SubtitleSize.MEDIUM
) {
    var posX by mutableFloatStateOf(initialX)
        private set

    var posY by mutableFloatStateOf(initialY)
        private set

    val initialX: Float = initialX
    val initialY: Float = initialY

    val isDirty: Boolean by derivedStateOf {
        kotlin.math.abs(posX - initialX) > 0.001f || kotlin.math.abs(posY - initialY) > 0.001f
    }

    fun nudge(deltaX: Float, deltaY: Float, snapCenter: Boolean = true) {
        val rawX = posX + deltaX
        val clampedX = rawX.coerceIn(SafeBounds.minX(), SafeBounds.maxX())
        posX = if (snapCenter) SafeBounds.snapX(clampedX) else clampedX

        val rawY = posY + deltaY
        posY = rawY.coerceIn(SafeBounds.minYForSize(subtitleSize), SafeBounds.maxY())
    }

    fun dragBy(
        deltaPxX: Float,
        deltaPxY: Float,
        containerWidthPx: Float,
        containerHeightPx: Float,
        snapCenter: Boolean = true
    ) {
        if (containerWidthPx <= 0f || containerHeightPx <= 0f) return

        val fractionDeltaX = deltaPxX / containerWidthPx
        val fractionDeltaY = deltaPxY / containerHeightPx

        nudge(fractionDeltaX, fractionDeltaY, snapCenter)
    }

    fun resetToDefault() {
        posX = PlayerAppearance.DEFAULT_POS_X
        posY = PlayerAppearance.DEFAULT_POS_Y
    }

    fun commit() {
        PlayerAppearanceStore.update {
            it.copy(
                subtitlePosX = posX,
                subtitlePosY = posY
            )
        }
    }
}

@Composable
fun rememberPositionEditorState(
    appearance: PlayerAppearance
): PositionEditorState {
    return remember(appearance.subtitlePosX, appearance.subtitlePosY, appearance.subtitleSize) {
        PositionEditorState(
            initialX = appearance.subtitlePosX,
            initialY = appearance.subtitlePosY,
            subtitleSize = appearance.subtitleSize
        )
    }
}

