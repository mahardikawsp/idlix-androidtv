package com.idlix.tv.ui.screens.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.idlix.data.model.SubdlItem
import com.idlix.data.model.SubdlQuery
import com.idlix.data.model.SubdlSearchResult
import com.idlix.data.model.SubdlSubtitle
import com.idlix.data.model.SubtitleTrack
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSubtitlePickerModal(
    isVisible: Boolean,
    availableSubtitles: List<SubtitleTrack>,
    selectedSubtitle: SubtitleTrack?,
    isSubtitleEnabled: Boolean,
    subdlSubtitles: List<SubdlSubtitle>,
    isSearchingSubdl: Boolean,
    subdlCandidates: List<SubdlItem> = emptyList(),
    subdlError: SubdlSearchResult.Error? = null,
    subdlActiveQuery: SubdlQuery? = null,
    isManualSearchOpen: Boolean = false,
    manualTitle: String = "",
    manualYear: String = "",
    manualSeason: String = "",
    manualEpisode: String = "",
    isUsingSessionOverride: Boolean = false,
    isAppearancePanelOpen: Boolean = false,
    isSeries: Boolean = false,
    onSelectTrack: (SubtitleTrack?) -> Unit,
    onSelectSubdl: (SubdlSubtitle) -> Unit,
    onSearchSubdlClick: () -> Unit,
    onOpenManualSearch: () -> Unit,
    onCloseManualSearch: () -> Unit,
    onManualTitleChange: (String) -> Unit,
    onManualYearChange: (String) -> Unit,
    onManualSeasonChange: (String) -> Unit,
    onManualEpisodeChange: (String) -> Unit,
    onSubmitManualSearch: () -> Unit,
    onSelectCandidate: (SubdlItem) -> Unit,
    onRestoreOriginalTitle: () -> Unit,
    onOpenAppearancePanel: () -> Unit,
    onCloseAppearancePanel: () -> Unit,
    onOpenPositionEditor: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val firstItemFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible, isManualSearchOpen, isAppearancePanelOpen) {
        if (isVisible && !isManualSearchOpen && !isAppearancePanelOpen) {
            try {
                firstItemFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Slide-out Drawer Panel from Right side
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(440.dp)
                .background(Color(0xF0181818))
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ClosedCaption,
                        contentDescription = null,
                        tint = NetflixRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.audio_and_subtitles),
                        color = NetflixWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSearchingSubdl) {
                        TvLoadingSpinner(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Toggle Button: Tampilan Subtitle shortcut
                    Button(
                        onClick = {
                            if (isAppearancePanelOpen) {
                                onCloseAppearancePanel()
                            } else {
                                onOpenAppearancePanel()
                            }
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = if (isAppearancePanelOpen) NetflixRed else NetflixDarkCard,
                            focusedContainerColor = NetflixWhite,
                            contentColor = NetflixWhite,
                            focusedContentColor = NetflixBlack
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAppearancePanelOpen) stringResource(R.string.subtitle_tracks_tab) else stringResource(R.string.subtitle_appearance_shortcut),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                // Mode 1: Appearance Quick Panel (Bagian C)
                isAppearancePanelOpen -> {
                    TvSubtitleAppearanceQuickPanel(
                        onOpenPositionEditor = onOpenPositionEditor,
                        onBack = onCloseAppearancePanel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Mode 2: Manual Search Panel (Bagian B)
                isManualSearchOpen -> {
                    TvSubdlManualSearchPanel(
                        title = manualTitle,
                        year = manualYear,
                        season = manualSeason,
                        episode = manualEpisode,
                        isSeries = isSeries,
                        isUsingOverride = isUsingSessionOverride,
                        onTitleChange = onManualTitleChange,
                        onYearChange = onManualYearChange,
                        onSeasonChange = onManualSeasonChange,
                        onEpisodeChange = onManualEpisodeChange,
                        onSubmit = onSubmitManualSearch,
                        onRestoreOriginal = onRestoreOriginalTitle,
                        onCancel = onCloseManualSearch,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Mode 3: Standard Subtitle Track List
                else -> {
                    TvLazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            val isOffSelected = !isSubtitleEnabled || selectedSubtitle == null
                            SubtitleOptionCard(
                                title = stringResource(R.string.subtitles_off),
                                subtitle = null,
                                badge = null,
                                isSelected = isOffSelected,
                                onClick = { onSelectTrack(null) },
                                modifier = Modifier.focusRequester(firstItemFocusRequester)
                            )
                        }

                        if (availableSubtitles.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.default_subtitle),
                                    color = NetflixLightGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                )
                            }

                            items(availableSubtitles) { track ->
                                val isSelected = isSubtitleEnabled && selectedSubtitle?.path == track.path
                                SubtitleOptionCard(
                                    title = track.label.ifBlank { track.lang.uppercase() },
                                    subtitle = null,
                                    badge = track.lang.uppercase(),
                                    isSelected = isSelected,
                                    onClick = { onSelectTrack(track) }
                                )
                            }
                        }

                        item {
                            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = stringResource(R.string.subdl_subtitles_header),
                                        color = NetflixLightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        // Type Manually button
                                        Button(
                                            onClick = onOpenManualSearch,
                                            colors = ButtonDefaults.colors(
                                                containerColor = NetflixDarkCard,
                                                focusedContainerColor = NetflixWhite,
                                                contentColor = NetflixLightGray,
                                                focusedContentColor = NetflixBlack
                                            ),
                                            shape = ButtonDefaults.shape(RoundedCornerShape(4.dp)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = stringResource(R.string.subdl_manual_search), fontSize = 10.sp)
                                            }
                                        }

                                        // Refresh search button
                                        if (!isSearchingSubdl) {
                                            Button(
                                                onClick = onSearchSubdlClick,
                                                colors = ButtonDefaults.colors(
                                                    containerColor = Color.Transparent,
                                                    focusedContainerColor = NetflixWhite,
                                                    contentColor = NetflixRed,
                                                    focusedContentColor = NetflixBlack
                                                ),
                                                shape = ButtonDefaults.shape(RoundedCornerShape(4.dp)),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }
                                }

                                if (subdlActiveQuery?.filmName != null) {
                                    val yearSuffix = subdlActiveQuery.year?.let { " ($it)" } ?: ""
                                    Text(
                                        text = stringResource(R.string.subdl_results_for, "${subdlActiveQuery.filmName}$yearSuffix"),
                                        color = NetflixLightGray.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // SubDL Error State
                        if (subdlError != null) {
                            item {
                                val errorMsg = when (subdlError.reason) {
                                    SubdlSearchResult.Error.Reason.INVALID_KEY -> stringResource(R.string.subdl_error_invalid_key)
                                    SubdlSearchResult.Error.Reason.QUOTA -> stringResource(R.string.subdl_error_quota)
                                    SubdlSearchResult.Error.Reason.NETWORK -> stringResource(R.string.subdl_error_network)
                                    else -> stringResource(R.string.subdl_error_generic, subdlError.message ?: "")
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x33E50914), RoundedCornerShape(6.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = NetflixRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = errorMsg, color = NetflixWhite, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = onOpenManualSearch,
                                        colors = ButtonDefaults.colors(
                                            containerColor = NetflixRed,
                                            focusedContainerColor = NetflixWhite,
                                            contentColor = NetflixWhite,
                                            focusedContentColor = NetflixBlack
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(4.dp)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(text = stringResource(R.string.subdl_type_title_btn), fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // SubDL Candidate Titles (from API results field)
                        if (subdlCandidates.isNotEmpty() && subdlSubtitles.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.subdl_pick_title),
                                    color = NetflixLightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(subdlCandidates) { candidate ->
                                val name = candidate.name.orEmpty()
                                val year = candidate.year?.let { " ($it)" } ?: ""
                                val typeBadge = candidate.type?.uppercase() ?: "MOVIE"
                                SubtitleOptionCard(
                                    title = "$name$year",
                                    subtitle = "SubDL ID: ${candidate.sd_id ?: ""}",
                                    badge = typeBadge,
                                    isSelected = false,
                                    onClick = { onSelectCandidate(candidate) }
                                )
                            }
                        }

                        // SubDL Loading or Empty
                        if (isSearchingSubdl && subdlSubtitles.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.searching_subdl),
                                    color = NetflixLightGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        } else if (subdlSubtitles.isEmpty() && subdlError == null && subdlCandidates.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NetflixDarkCard, RoundedCornerShape(6.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val activeName = subdlActiveQuery?.filmName ?: ""
                                    Text(
                                        text = stringResource(R.string.subdl_not_found_hint, activeName),
                                        color = NetflixLightGray,
                                        fontSize = 11.sp
                                    )
                                    Button(
                                        onClick = onOpenManualSearch,
                                        colors = ButtonDefaults.colors(
                                            containerColor = NetflixRed,
                                            focusedContainerColor = NetflixWhite,
                                            contentColor = NetflixWhite,
                                            focusedContentColor = NetflixBlack
                                        ),
                                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(R.string.subdl_type_title_btn),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(subdlSubtitles) { sub ->
                                val isIndo = sub.language.equals("ID", ignoreCase = true) || sub.lang.equals("indonesian", ignoreCase = true)
                                val badgeText = if (isIndo) "ID" else (sub.language ?: "EN")
                                val authorText = sub.author?.let { "by $it" } ?: ""
                                val releaseText = sub.releaseName ?: sub.name ?: ""

                                SubtitleOptionCard(
                                    title = releaseText,
                                    subtitle = authorText.ifBlank { null },
                                    badge = badgeText,
                                    isSelected = false,
                                    onClick = { onSelectSubdl(sub) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SubtitleOptionCard(
    title: String,
    subtitle: String?,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = CardDefaults.shape(shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = if (isSelected) NetflixRed.copy(alpha = 0.25f) else NetflixDarkCard,
            focusedContainerColor = NetflixRed
        ),
        scale = CardDefaults.scale(focusedScale = 1.04f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, NetflixWhite)
            ),
            border = if (isSelected) {
                Border(border = BorderStroke(1.dp, NetflixRed))
            } else {
                Border.None
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (badge == "ID") NetflixRed else Color(0x66FFFFFF),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = NetflixWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = NetflixWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = NetflixLightGray,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(NetflixRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = NetflixWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
