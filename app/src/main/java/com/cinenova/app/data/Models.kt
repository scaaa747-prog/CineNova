package com.cinenova.app.data

enum class MediaType { MOVIE, TV }

data class MediaItem(
    val id: String,
    val title: String,
    val year: Int,
    val rating: Double,
    val ageRating: String,
    val runtimeMinutes: Int,
    val genres: List<String>,
    val description: String,
    val type: MediaType,
) {
    val posterUrl: String get() = "https://picsum.photos/seed/$id-p/400/600"
    val backdropUrl: String get() = "https://picsum.photos/seed/$id-b/960/540"
}

data class CastMember(
    val name: String,
    val role: String,
) {
    val avatarUrl: String get() = "https://picsum.photos/seed/$name-avatar/200/200"
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
) {
    val thumbnailUrl: String get() = "https://picsum.photos/seed/$id-ep/320/180"
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
