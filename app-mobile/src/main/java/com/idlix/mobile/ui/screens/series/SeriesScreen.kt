package com.idlix.mobile.ui.screens.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.idlix.mobile.R
import com.idlix.mobile.ui.components.FilterChipRow
import com.idlix.mobile.ui.components.FilterItem
import com.idlix.mobile.ui.components.MobileMovieCard
import com.idlix.mobile.ui.theme.BackgroundDark
import com.idlix.mobile.ui.theme.IdlixRed
import com.idlix.mobile.ui.theme.TextWhite

@Composable
fun SeriesScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    viewModel: SeriesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gridState = rememberLazyGridState()

    val allLabel = stringResource(R.string.filter_all)
    val seriesGenres = remember(allLabel) {
        listOf(
            FilterItem("all", allLabel),
            FilterItem("action", "Action"),
            FilterItem("animation", "Animation"),
            FilterItem("comedy", "Comedy"),
            FilterItem("crime", "Crime"),
            FilterItem("drama", "Drama"),
            FilterItem("mystery", "Mystery"),
            FilterItem("science-fiction", "Sci-Fi")
        )
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.series),
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            FilterChipRow(
                items = seriesGenres,
                selectedId = uiState.selectedGenre,
                onSelect = { viewModel.selectGenre(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading && !uiState.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IdlixRed)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.seriesList, key = { it.id }) { series ->
                        MobileMovieCard(
                            title = series.title,
                            posterUrl = series.fullPosterUrl,
                            rating = series.voteAverage?.toDoubleOrNull(),
                            width = 110.dp,
                            onClick = { onNavigateToDetail("series", series.slug) }
                        )
                    }

                    if (uiState.isLoadingMore) {
                        item(span = { GridItemSpan(3) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = IdlixRed, modifier = Modifier.height(28.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
