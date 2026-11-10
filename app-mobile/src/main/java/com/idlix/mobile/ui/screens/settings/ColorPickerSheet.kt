package com.idlix.mobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idlix.mobile.R
import com.idlix.mobile.ui.theme.BackgroundDark
import com.idlix.mobile.ui.theme.IdlixRed
import com.idlix.mobile.ui.theme.SurfaceDark
import com.idlix.mobile.ui.theme.SurfaceVariantDark
import com.idlix.mobile.ui.theme.TextDim
import com.idlix.mobile.ui.theme.TextMuted
import com.idlix.mobile.ui.theme.TextWhite
import com.idlix.player.ui.AlphaBar
import com.idlix.player.ui.HsvPanel
import com.idlix.player.ui.HueBar
import com.idlix.player.ui.rememberColorPickerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerSheet(
    isOpen: Boolean,
    initialColor: Int,
    supportsAlpha: Boolean = false,
    title: String,
    recentColors: List<Int> = emptyList(),
    presetColors: List<Int> = emptyList(),
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state = rememberColorPickerState(initialColor)
    var hexInput by remember(state.currentHex) { mutableStateOf(state.currentHex) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(state.currentColor), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .pointerInput(state) {
                        detectTapGestures { offset ->
                            val s = (offset.x / size.width).coerceIn(0f, 1f)
                            val v = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                            state.saturation = s
                            state.value = v
                        }
                    }
                    .pointerInput(state) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val s = (offset.x / size.width).coerceIn(0f, 1f)
                                val v = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                                state.saturation = s
                                state.value = v
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val s = (change.position.x / size.width).coerceIn(0f, 1f)
                                val v = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                                state.saturation = s
                                state.value = v
                            }
                        )
                    }
            ) {
                HsvPanel(
                    hue = state.hue,
                    saturation = state.saturation,
                    value = state.value,
                    modifier = Modifier.matchParentSize()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Hue", color = TextDim, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .pointerInput(state) {
                            detectTapGestures { offset ->
                                val h = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                                state.hue = h
                            }
                        }
                        .pointerInput(state) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val h = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                                    state.hue = h
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val h = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                                    state.hue = h
                                }
                            )
                        }
                ) {
                    HueBar(
                        hue = state.hue,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            if (supportsAlpha) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Opacity", color = TextDim, fontSize = 12.sp)
                        Text(
                            text = "${(state.alpha * 100).toInt()}%",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .pointerInput(state) {
                                detectTapGestures { offset ->
                                    val a = (offset.x / size.width).coerceIn(0f, 1f)
                                    state.alpha = a
                                }
                            }
                            .pointerInput(state) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        val a = (offset.x / size.width).coerceIn(0f, 1f)
                                        state.alpha = a
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val a = (change.position.x / size.width).coerceIn(0f, 1f)
                                        state.alpha = a
                                    }
                                )
                            }
                    ) {
                        AlphaBar(
                            baseColor = state.currentColor,
                            alpha = state.alpha,
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }

            OutlinedTextField(
                value = hexInput,
                onValueChange = {
                    hexInput = it
                    state.setHex(it)
                },
                label = { Text("HEX Code", color = TextDim) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceVariantDark,
                    unfocusedContainerColor = SurfaceVariantDark,
                    focusedBorderColor = IdlixRed,
                    unfocusedBorderColor = SurfaceVariantDark,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                modifier = Modifier.fillMaxWidth()
            )

            val swatchesToShow = (recentColors + presetColors).distinct().take(8)
            if (swatchesToShow.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.color_recent),
                        color = TextDim,
                        fontSize = 12.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        swatchesToShow.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        state.setColor(col)
                                        hexInput = state.currentHex
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(col), CircleShape)
                                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(text = stringResource(R.string.cancel), color = TextWhite)
                }

                Button(
                    onClick = {
                        onColorSelected(state.currentColor)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IdlixRed),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(text = stringResource(R.string.apply), color = Color.White)
                }
            }
        }
    }
}
