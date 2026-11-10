package com.idlix.player.ui

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import com.idlix.data.model.PlayerAppearance

@Composable
fun rememberPlayerCues(player: Player?): State<List<Cue>> {
    val cuesState = remember { mutableStateOf<List<Cue>>(emptyList()) }

    DisposableEffect(player) {
        if (player == null) {
            cuesState.value = emptyList()
            return@DisposableEffect onDispose {}
        }

        val listener = object : Player.Listener {
            override fun onCues(cueGroup: CueGroup) {
                cuesState.value = cueGroup.cues
            }
        }
        player.addListener(listener)

        // Initial cues if already playing/paused
        try {
            cuesState.value = player.currentCues.cues
        } catch (_: Exception) {}

        onDispose {
            player.removeListener(listener)
            cuesState.value = emptyList()
        }
    }

    return cuesState
}

@Composable
fun SubtitleOverlay(
    cues: List<Cue>,
    appearance: PlayerAppearance,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            SubtitleView(ctx).apply {
                setApplyEmbeddedStyles(false)
                setApplyEmbeddedFontSizes(false)
                setBottomPaddingFraction(0f)
            }
        },
        update = { subtitleView ->
            val style = CaptionStyleCompat(
                appearance.subtitleTextColor,
                appearance.subtitleBoxColor,
                AndroidColor.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                AndroidColor.BLACK,
                null
            )
            subtitleView.setStyle(style)
            subtitleView.setFractionalTextSize(appearance.subtitleSize.fraction)

            val repositioned = CueRepositioner.reposition(cues, appearance)
            subtitleView.setCues(repositioned)
        },
        modifier = modifier
    )
}

