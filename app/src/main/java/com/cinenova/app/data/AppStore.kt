package com.cinenova.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cinenova.app.ui.theme.ThemeMode

/**
 * Single observable app store (demo scope). Backs watchlist, downloads,
 * continue-watching, notifications and user preferences with Compose state.
 */
object AppStore {

    // ---- Appearance / preferences ----
    var themeMode by mutableStateOf(ThemeMode.DARK)

    val streamingQuality = mutableStateOf("Auto (up to 4K)")
    val autoplayNextEpisode = mutableStateOf(true)
    val autoplayPreviews = mutableStateOf(true)
    val playbackSpeed = mutableStateOf("Normal")
    val subtitleLanguage = mutableStateOf("English")
    val audioLanguage = mutableStateOf("Original")
    val subtitleSize = mutableStateOf("Medium")
    val downloadQuality = mutableStateOf("Standard")
    val wifiOnlyDownloads = mutableStateOf(true)
    val dataSaver = mutableStateOf(false)
    val appLanguage = mutableStateOf("English")

    val notifyNewReleases = mutableStateOf(true)
    val notifyNewEpisodes = mutableStateOf(true)
    val notifyRecommendations = mutableStateOf(false)
    val notifyDownloads = mutableStateOf(true)

    // ---- Library state ----
    val watchlistIds = mutableStateListOf<String>()
    val downloads = mutableStateMapOf<String, DownloadEntry>()
    val readNotificationIds = mutableStateListOf<String>()
    val recentSearches = mutableStateListOf<String>()

    fun isInWatchlist(id: String): Boolean = id in watchlistIds

    fun toggleWatchlist(id: String) {
        if (id in watchlistIds) watchlistIds.remove(id) else watchlistIds.add(id)
    }

    fun downloadEntry(id: String): DownloadEntry? = downloads[id]

    fun toggleDownload(id: String, episodeLabel: String? = null, sizeMb: Long = 1_400) {
        val existing = downloads[id]
        downloads[id] = when (existing) {
            null -> DownloadEntry(id, DownloadState.DOWNLOADING, 0, sizeMb, episodeLabel)
            else -> {
                if (existing.state == DownloadState.COMPLETED) {
                    downloads.remove(id)
                    return
                }
                existing.copy(state = DownloadState.COMPLETED, progressPercent = 100)
            }
        }
    }

    fun setDownloadState(id: String, state: DownloadState) {
        downloads[id]?.let { downloads[id] = it.copy(state = state) }
    }

    fun removeDownload(id: String) {
        downloads.remove(id)
    }

    fun markNotificationRead(id: String) {
        if (id !in readNotificationIds) readNotificationIds.add(id)
    }

    fun clearNotifications() {
        DemoRepository.notifications.forEach { markNotificationRead(it.id) }
    }

    fun unreadCount(): Int =
        DemoRepository.notifications.count { it.unread && it.id !in readNotificationIds }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        recentSearches.remove(query)
        recentSearches.add(0, query)
        while (recentSearches.size > 8) recentSearches.removeAt(recentSearches.size - 1)
    }

    fun clearRecentSearches() = recentSearches.clear()
}
