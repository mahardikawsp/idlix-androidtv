package com.idlix.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import com.idlix.mobile.R
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class MobileScreen(val route: String) {
    object Home : MobileScreen("home")
    object Movies : MobileScreen("movies")
    object Series : MobileScreen("series")
    object Search : MobileScreen("search")
    object Library : MobileScreen("library")
    object Settings : MobileScreen("settings")
    object SubtitlePositionEditor : MobileScreen("settings/subtitle_position")

    object Detail : MobileScreen("detail/{contentType}/{slug}") {
        fun createRoute(contentType: String, slug: String): String {
            val encodedSlug = URLEncoder.encode(slug, StandardCharsets.UTF_8.toString())
            return "detail/$contentType/$encodedSlug"
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem(MobileScreen.Home.route, R.string.home, Icons.Filled.Home)
    object Movies : BottomNavItem(MobileScreen.Movies.route, R.string.movies, Icons.Filled.Movie)
    object Series : BottomNavItem(MobileScreen.Series.route, R.string.series, Icons.Filled.Tv)
    object Search : BottomNavItem(MobileScreen.Search.route, R.string.search, Icons.Filled.Search)
    object Library : BottomNavItem(MobileScreen.Library.route, R.string.library, Icons.Filled.VideoLibrary)

    companion object {
        val items = listOf(Home, Movies, Series, Search, Library)
    }
}
