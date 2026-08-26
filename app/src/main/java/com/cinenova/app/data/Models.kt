package com.cinenova.app.data

enum class MediaType { MOVIE, TV }

data class MediaItem(
    val id: String,
    val title: String,
    val year: Int = 0,
    val rating: Double = 0.0,
    val ageRating: String = "NR",
    val runtimeMinutes: Int = 0,
    val genres: List<String> = emptyList(),
    val description: String = "",
    val type: MediaType = MediaType.MOVIE,
    val posterUrlOverride: String? = null,
    val backdropUrlOverride: String? = null,
) {
    val posterUrl: String
        get() = posterUrlOverride?.takeIf { it.isNotBlank() } ?: "https://picsum.photos/seed/$id-p/400/600"

    val backdropUrl: String
        get() = backdropUrlOverride?.takeIf { it.isNotBlank() } ?: "https://picsum.photos/seed/$id-b/960/540"

    constructor(
        id: String,
        title: String,
        year: Int = 0,
        rating: Double = 0.0,
        ageRating: String = "NR",
        runtimeMinutes: Int = 0,
        genres: List<String> = emptyList(),
        description: String = "",
        type: MediaType = MediaType.MOVIE,
        posterUrl: String? = null,
        backdropUrl: String? = null,
    ) : this(
        id = id,
        title = title,
        year = year,
        rating = rating,
        ageRating = ageRating,
        runtimeMinutes = runtimeMinutes,
        genres = genres,
        description = description,
        type = type,
        posterUrlOverride = posterUrl,
        backdropUrlOverride = backdropUrl,
    )
}

data class CastMember(
    val name: String,
    val role: String,
    val avatarUrlOverride: String? = null,
) {
    val avatarUrl: String
        get() = avatarUrlOverride?.takeIf { it.isNotBlank() } ?: "https://picsum.photos/seed/$name-avatar/200/200"

    constructor(name: String, role: String, avatarUrl: String? = null) : this(
        name = name,
        role = role,
        avatarUrlOverride = avatarUrl,
    )
}

data class Review(
    val author: String,
    val rating: Double,
    val date: String,
    val text: String,
)

data class Episode(
    val id: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val runtimeMinutes: Int,
    val description: String,
    val thumbnailUrlOverride: String? = null,
) {
    val thumbnailUrl: String
        get() = thumbnailUrlOverride?.takeIf { it.isNotBlank() } ?: "https://picsum.photos/seed/$id-ep/320/180"

    constructor(
        id: String,
        seasonNumber: Int,
        episodeNumber: Int,
        title: String,
        runtimeMinutes: Int = 0,
        description: String = "",
        thumbnailUrl: String? = null,
    ) : this(
        id = id,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        title = title,
        runtimeMinutes = runtimeMinutes,
        description = description,
        thumbnailUrlOverride = thumbnailUrl,
    )
}

data class Season(
    val number: Int,
    val episodes: List<Episode>,
)

data class Trailer(
    val title: String,
    val durationMinutes: Int,
)

data class WatchProgress(
    val itemId: String,
    val positionMinutes: Int,
    val durationMinutes: Int,
    val episodeLabel: String? = null,
) {
    val fraction: Float get() = if (durationMinutes <= 0) 0f else positionMinutes.toFloat() / durationMinutes
    val remainingMinutes: Int get() = (durationMinutes - positionMinutes).coerceAtLeast(0)
}

enum class DownloadState { QUEUED, DOWNLOADING, PAUSED, COMPLETED }

data class DownloadEntry(
    val itemId: String,
    val state: DownloadState,
    val progressPercent: Int,
    val sizeMb: Long,
    val episodeLabel: String? = null,
)

enum class NotificationKind { NEW_RELEASE, NEW_EPISODE, RECOMMENDATION, DOWNLOAD_COMPLETE, APP }

data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val time: String,
    val unread: Boolean,
)
