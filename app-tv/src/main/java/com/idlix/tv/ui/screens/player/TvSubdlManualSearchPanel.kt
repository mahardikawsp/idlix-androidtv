package com.idlix.tv.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSubdlManualSearchPanel(
    title: String,
    year: String,
    season: String,
    episode: String,
    isSeries: Boolean,
    isUsingOverride: Boolean,
    onTitleChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onSeasonChange: (String) -> Unit,
    onEpisodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRestoreOriginal: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val titleFocusRequester = remember { FocusRequester() }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isYearFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            titleFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Back row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.colors(
                    containerColor = Color.Transparent,
                    focusedContainerColor = NetflixWhite,
                    contentColor = NetflixLightGray,
                    focusedContentColor = NetflixBlack
                ),
                shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.back_to_list), fontSize = 12.sp)
                }
            }
        }

        // Title Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.subdl_title_label),
                color = NetflixLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(NetflixBlack, RoundedCornerShape(6.dp))
                    .border(
                        width = if (isTitleFocused) 2.dp else 1.dp,
                        color = if (isTitleFocused) NetflixWhite else NetflixDarkCard,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    textStyle = TextStyle(
                        color = NetflixWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(NetflixRed),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        onSubmit()
                    }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester)
                        .onFocusChanged { isTitleFocused = it.isFocused },
                    decorationBox = { inner ->
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g. Avatar",
                                color = NetflixLightGray.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        inner()
                    }
                )
            }
        }

        // Year Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.subdl_year_label),
                color = NetflixLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .background(NetflixBlack, RoundedCornerShape(6.dp))
                    .border(
                        width = if (isYearFocused) 2.dp else 1.dp,
                        color = if (isYearFocused) NetflixWhite else NetflixDarkCard,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = year,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) onYearChange(it) },
                    textStyle = TextStyle(
                        color = NetflixWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(NetflixRed),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        onSubmit()
                    }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isYearFocused = it.isFocused },
                    decorationBox = { inner ->
                        if (year.isEmpty()) {
                            Text(
                                text = "e.g. 2022",
                                color = NetflixLightGray.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        inner()
                    }
                )
            }
        }

        // Season & Episode (for Series)
        if (isSeries) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Season counter
                val sNum = season.toIntOrNull() ?: 1
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.subdl_season_label),
                        color = NetflixLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onSeasonChange((sNum - 1).coerceAtLeast(1).toString()) },
                            colors = ButtonDefaults.colors(
                                containerColor = NetflixDarkCard,
                                focusedContainerColor = NetflixWhite,
                                focusedContentColor = NetflixBlack
                            ),
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = sNum.toString(),
                            color = NetflixWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Button(
                            onClick = { onSeasonChange((sNum + 1).toString()) },
                            colors = ButtonDefaults.colors(
                                containerColor = NetflixDarkCard,
                                focusedContainerColor = NetflixWhite,
                                focusedContentColor = NetflixBlack
                            ),
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Episode counter
                val eNum = episode.toIntOrNull() ?: 1
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.subdl_episode_label),
                        color = NetflixLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onEpisodeChange((eNum - 1).coerceAtLeast(1).toString()) },
                            colors = ButtonDefaults.colors(
                                containerColor = NetflixDarkCard,
                                focusedContainerColor = NetflixWhite,
                                focusedContentColor = NetflixBlack
                            ),
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = eNum.toString(),
                            color = NetflixWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Button(
                            onClick = { onEpisodeChange((eNum + 1).toString()) },
                            colors = ButtonDefaults.colors(
                                containerColor = NetflixDarkCard,
                                focusedContainerColor = NetflixWhite,
                                focusedContentColor = NetflixBlack
                            ),
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons
        Button(
            onClick = {
                keyboardController?.hide()
                onSubmit()
            },
            enabled = title.trim().length >= 2,
            colors = ButtonDefaults.colors(
                containerColor = NetflixRed,
                focusedContainerColor = Color(0xFFE50914).copy(alpha = 0.8f),
                contentColor = NetflixWhite,
                focusedContentColor = NetflixWhite
            ),
            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.subdl_search_action),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        if (isUsingOverride) {
            Button(
                onClick = onRestoreOriginal,
                colors = ButtonDefaults.colors(
                    containerColor = NetflixBlack,
                    focusedContainerColor = NetflixWhite,
                    contentColor = NetflixLightGray,
                    focusedContentColor = NetflixBlack
                ),
                shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.subdl_restore_original), fontSize = 12.sp)
                }
            }
        }
    }
}
