package com.cinenova.app.data.remote

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

/** A single playable stream variant returned by the resource endpoint. */
data class StreamResource(
    val url: String,
    val qualityLabel: String,
    val sizeBytes: Long?,
    val format: String?,
    val season: Int,
    val episode: Int,
)

/** Full resource payload exposed to the player layer. */
data class PlaybackResources(
    val subjectId: Long,
    val season: Int,
    val episode: Int,
    val sources: List<StreamResource>,
) {
    /** Default pick: highest-quality source listed first by upstream. */
    fun bestSource(): StreamResource? = sources.firstOrNull()

    fun sourceFor(qualityLabel: String): StreamResource? =
        sources.firstOrNull { it.qualityLabel.equals(qualityLabel, ignoreCase = true) }
}
