package com.cinenova.app.data

enum class MediaType { MOVIE, TV }

data class DubOption(
    val subjectId: String,
    val languageName: String,
)

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
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val dubs: List<DubOption> = emptyList(),
)

data class CastMember(
    val name: String,
    val role: String,
    val avatarUrl: String = "",
)

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
    val runtimeMinutes: Int = 0,
    val description: String = "",
    val thumbnailUrl: String = "",
)

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
    val progressPercent: Int = 0,
    val sizeMb: Long = 0L,
    val episodeLabel: String? = null,
    val title: String? = null,
    val posterUrl: String? = null,
    val qualityLabel: String? = null,
    val downloadUrl: String? = null,
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
