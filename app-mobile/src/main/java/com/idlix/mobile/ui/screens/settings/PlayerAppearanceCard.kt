package com.idlix.mobile.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idlix.data.local.PlayerAppearanceStore
import com.idlix.data.model.PlayerAppearancePresets
import com.idlix.data.model.SubtitleSize
import com.idlix.data.util.ColorMath
import com.idlix.mobile.R
import com.idlix.mobile.ui.theme.IdlixRed
import com.idlix.mobile.ui.theme.SurfaceDark
import com.idlix.mobile.ui.theme.SurfaceVariantDark
import com.idlix.mobile.ui.theme.TextDim
import com.idlix.mobile.ui.theme.TextMuted
import com.idlix.mobile.ui.theme.TextWhite

private enum class ActivePickerTarget {
    NONE, BACKGROUND, TEXT, BOX
}

@Composable
fun PlayerAppearanceCard(
    onOpenPositionEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance by PlayerAppearanceStore.appearance.collectAsState()
    val recentColors by PlayerAppearanceStore.recentCustomColors.collectAsState()

    var activePicker by remember { mutableStateOf(ActivePickerTarget.NONE) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.player_appearance_header),
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.player_appearance_desc),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(appearance.backgroundColor))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2.39f)
                        .align(Alignment.Center)
                        .background(Color(0xFF1E222A))
                ) {
                    Text(
                        text = "2.39:1 Video Frame",
                        color = Color.White.copy(alpha = 0.25f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val boxW = maxWidth
                    val boxH = maxHeight

                    val subX = boxW * appearance.subtitlePosX.coerceIn(0.05f, 0.95f)
                    val subY = boxH * appearance.subtitlePosY.coerceIn(0.10f, 0.95f)

                    val fontSizeSp = when (appearance.subtitleSize) {
                        SubtitleSize.SMALL -> 11.sp
                        SubtitleSize.MEDIUM -> 13.sp
                        SubtitleSize.LARGE -> 16.sp
                        SubtitleSize.EXTRA_LARGE -> 19.sp
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = (subX - 100.dp).coerceAtLeast(8.dp),
                                top = (subY - 20.dp).coerceAtLeast(8.dp)
                            )
                            .background(
                                color = Color(appearance.subtitleBoxColor),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.subtitle_preview_sample_1),
                            color = Color(appearance.subtitleTextColor),
                            fontSize = fontSizeSp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.player_bg_color),
                    color = TextDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isCustomBg = appearance.backgroundColor !in PlayerAppearancePresets.backgroundColors
                    PlayerAppearancePresets.backgroundColors.forEach { col ->
                        val isSelected = appearance.backgroundColor == col
                        MobileColorSwatch(
                            color = col,
                            isSelected = isSelected,
                            onClick = {
                                PlayerAppearanceStore.update { it.copy(backgroundColor = col) }
                            }
                        )
                    }
                    CustomColorSwatch(
                        currentColor = appearance.backgroundColor,
                        isCustomSelected = isCustomBg,
                        onClick = { activePicker = ActivePickerTarget.BACKGROUND }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.subtitle_text_size),
                    color = TextDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionPillButton(
                            text = stringResource(R.string.size_small),
                            isSelected = appearance.subtitleSize == SubtitleSize.SMALL,
                            onClick = {
                                PlayerAppearanceStore.update { it.copy(subtitleSize = SubtitleSize.SMALL) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OptionPillButton(
                            text = stringResource(R.string.size_medium),
                            isSelected = appearance.subtitleSize == SubtitleSize.MEDIUM,
                            onClick = {
                                PlayerAppearanceStore.update { it.copy(subtitleSize = SubtitleSize.MEDIUM) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OptionPillButton(
                            text = stringResource(R.string.size_large),
                            isSelected = appearance.subtitleSize == SubtitleSize.LARGE,
                            onClick = {
                                PlayerAppearanceStore.update { it.copy(subtitleSize = SubtitleSize.LARGE) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        OptionPillButton(
                            text = stringResource(R.string.size_extra_large),
                            isSelected = appearance.subtitleSize == SubtitleSize.EXTRA_LARGE,
                            onClick = {
                                PlayerAppearanceStore.update { it.copy(subtitleSize = SubtitleSize.EXTRA_LARGE) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.subtitle_text_color),
                    color = TextDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isCustomText = appearance.subtitleTextColor !in PlayerAppearancePresets.subtitleTextColors
                    PlayerAppearancePresets.subtitleTextColors.forEach { col ->
                        val isSelected = appearance.subtitleTextColor == col
                        MobileColorSwatch(
                            color = col,
                            isSelected = isSelected,
                            onClick = {
                                PlayerAppearanceStore.update { it.copy(subtitleTextColor = col) }
                            }
                        )
                    }
                    CustomColorSwatch(
                        currentColor = appearance.subtitleTextColor,
                        isCustomSelected = isCustomText,
                        onClick = { activePicker = ActivePickerTarget.TEXT }
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.subtitle_box_color),
                    color = TextDim,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isTransSelected = appearance.subtitleBoxColor == 0x00000000
                        OptionPillButton(
                            text = stringResource(R.string.box_transparent),
                            isSelected = isTransSelected,
                            onClick = { PlayerAppearanceStore.update { it.copy(subtitleBoxColor = 0x00000000) } },
                            modifier = Modifier.weight(1f)
                        )
                        val is50Selected = appearance.subtitleBoxColor == 0x80000000.toInt()
                        OptionPillButton(
                            text = stringResource(R.string.box_black_50),
                            isSelected = is50Selected,
                            onClick = { PlayerAppearanceStore.update { it.copy(subtitleBoxColor = 0x80000000.toInt()) } },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val is75Selected = appearance.subtitleBoxColor == 0xC0000000.toInt()
                        OptionPillButton(
                            text = stringResource(R.string.box_black_75),
                            isSelected = is75Selected,
                            onClick = { PlayerAppearanceStore.update { it.copy(subtitleBoxColor = 0xC0000000.toInt()) } },
                            modifier = Modifier.weight(1f)
                        )
                        val isSolidSelected = appearance.subtitleBoxColor == 0xFF000000.toInt()
                        OptionPillButton(
                            text = stringResource(R.string.box_black_solid),
                            isSelected = isSolidSelected,
                            onClick = { PlayerAppearanceStore.update { it.copy(subtitleBoxColor = 0xFF000000.toInt()) } },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val isCustomBox = appearance.subtitleBoxColor !in PlayerAppearancePresets.subtitleBoxColors
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustomBox) IdlixRed else SurfaceVariantDark)
                            .border(
                                width = if (isCustomBox) 1.5.dp else 1.dp,
                                color = if (isCustomBox) IdlixRed else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { activePicker = ActivePickerTarget.BOX },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isCustomBox) Color.White else TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCustomBox) {
                                    "${stringResource(R.string.color_custom)} (${ColorMath.toHex(appearance.subtitleBoxColor, includeAlpha = true)})"
                                } else {
                                    stringResource(R.string.color_custom)
                                },
                                color = if (isCustomBox) Color.White else TextWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isCustomBox) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.subtitle_position),
                        color = TextDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "X: ${(appearance.subtitlePosX * 100).toInt()}% • Y: ${(appearance.subtitlePosY * 100).toInt()}%",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onOpenPositionEditor,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.adjust_subtitle_position),
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            TextButton(
                onClick = { PlayerAppearanceStore.resetAll() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = TextDim
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.reset_all_appearance), color = TextDim, fontSize = 12.sp)
            }
        }
    }

    ColorPickerSheet(
        isOpen = activePicker != ActivePickerTarget.NONE,
        initialColor = when (activePicker) {
            ActivePickerTarget.BACKGROUND -> appearance.backgroundColor
            ActivePickerTarget.TEXT -> appearance.subtitleTextColor
            ActivePickerTarget.BOX -> appearance.subtitleBoxColor
            ActivePickerTarget.NONE -> 0
        },
        supportsAlpha = activePicker == ActivePickerTarget.BOX,
        title = when (activePicker) {
            ActivePickerTarget.BACKGROUND -> stringResource(R.string.player_bg_color)
            ActivePickerTarget.TEXT -> stringResource(R.string.subtitle_text_color)
            ActivePickerTarget.BOX -> stringResource(R.string.subtitle_box_color)
            ActivePickerTarget.NONE -> ""
        },
        recentColors = recentColors,
        onColorSelected = { selected ->
            when (activePicker) {
                ActivePickerTarget.BACKGROUND -> PlayerAppearanceStore.update { it.copy(backgroundColor = selected or 0xFF000000.toInt()) }
                ActivePickerTarget.TEXT -> PlayerAppearanceStore.update { it.copy(subtitleTextColor = selected or 0xFF000000.toInt()) }
                ActivePickerTarget.BOX -> PlayerAppearanceStore.update { it.copy(subtitleBoxColor = selected) }
                ActivePickerTarget.NONE -> {}
            }
            PlayerAppearanceStore.addRecentCustomColor(selected)
            activePicker = ActivePickerTarget.NONE
        },
        onDismiss = { activePicker = ActivePickerTarget.NONE }
    )
}

@Composable
fun OptionPillButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) IdlixRed else SurfaceVariantDark)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) IdlixRed else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextWhite,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MobileColorSwatch(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val r = (color ushr 16) and 0xFF
    val g = (color ushr 8) and 0xFF
    val b = color and 0xFF
    val isLight = (0.299 * r + 0.587 * g + 0.114 * b) > 150

    Box(
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(color))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) IdlixRed else Color.White.copy(alpha = 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isLight) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CustomColorSwatch(
    currentColor: Int,
    isCustomSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val r = (currentColor ushr 16) and 0xFF
    val g = (currentColor ushr 8) and 0xFF
    val b = currentColor and 0xFF
    val isLight = (0.299 * r + 0.587 * g + 0.114 * b) > 150

    Box(
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (isCustomSelected) Color(currentColor) else SurfaceVariantDark)
                .border(
                    width = if (isCustomSelected) 2.5.dp else 1.dp,
                    color = if (isCustomSelected) IdlixRed else Color.White.copy(alpha = 0.25f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCustomSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isLight) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Custom Color",
                    tint = TextWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
