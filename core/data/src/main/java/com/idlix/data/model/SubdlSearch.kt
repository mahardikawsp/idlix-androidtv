package com.idlix.data.model

data class SubdlQuery(
    val filmName: String? = null,
    val sdId: Long? = null,
    val tmdbId: Long? = null,
    val imdbId: String? = null,
    val year: Int? = null,
    val type: String = "movie",
    val season: Int? = null,
    val episode: Int? = null,
    val languages: String = "ID,EN"
)

sealed interface SubdlSearchResult {
    data class Found(
        val subtitles: List<SubdlSubtitle>,
        val candidates: List<SubdlItem> = emptyList()
    ) : SubdlSearchResult

    data class NotFound(
        val candidates: List<SubdlItem> = emptyList()
    ) : SubdlSearchResult

    data class Error(
        val reason: Reason,
        val message: String? = null
    ) : SubdlSearchResult {
        enum class Reason {
            NETWORK,
            INVALID_KEY,
            QUOTA,
            HTTP,
            PARSE
        }
    }
}

