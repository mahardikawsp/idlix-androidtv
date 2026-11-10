package com.idlix.tv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.idlix.data.local.PlayerAppearanceStore
import com.idlix.tv.ui.screens.player.SubtitlePositionEditorContent

@Composable
fun SubtitlePositionEditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance by PlayerAppearanceStore.appearance.collectAsState()

    // Full screen canvas using user's chosen player background color
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(appearance.backgroundColor)),
        contentAlignment = Alignment.Center
    ) {
        // Simulated 2.39:1 widescreen video surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.39f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF0F0F0F)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f))
        )

        // Interactive Editor overlay
        SubtitlePositionEditorContent(
            onSaved = onBack,
            onDismiss = onBack,
            modifier = Modifier.fillMaxSize()
        )
    }
}

