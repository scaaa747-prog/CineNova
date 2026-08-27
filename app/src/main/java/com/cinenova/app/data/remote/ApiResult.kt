package com.cinenova.app.data.remote

import com.cinenova.app.data.DubOption

/** Lightweight API result wrapper used across the data layer. */
sealed class ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>()
    data class HttpError(val code: Int, val message: String?) : ApiResult<Nothing>()
    data class NetworkError(val cause: Throwable) : ApiResult<Nothing>()
    data object Empty : ApiResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(value))
        is HttpError -> this
        is NetworkError -> this
        is Empty -> this
    }

    fun getOrNull(): T? = (this as? Success)?.value
}

data class SubtitleTrack(
    val language: String,
    val languageName: String,
    val url: String,
)

/** A single playable stream variant returned by the resource endpoint. */
data class StreamResource(
    val url: String,
    val qualityLabel: String,
    val sizeBytes: Long?,
    val format: String?,
    val season: Int,
    val episode: Int,
    val subtitles: List<SubtitleTrack> = emptyList(),
)

/** Full resource payload exposed to the player layer. */
data class PlaybackResources(
    val subjectId: Long,
    val season: Int,
    val episode: Int,
    val sources: List<StreamResource>,
    val availableDubs: List<DubOption> = emptyList(),
) {
    fun bestSource(): StreamResource? = sources.firstOrNull()

    fun sourceFor(qualityLabel: String): StreamResource? =
        sources.firstOrNull { it.qualityLabel.contains(qualityLabel, ignoreCase = true) }
}
