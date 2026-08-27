package com.cinenova.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cinenova.app.ui.theme.ThemeMode

/**
 * Functional AppStore with persistent SharedPreferences backing
 * Watchlist, Downloads, User Settings, and Offline Preferences.
 */
object AppStore {

    private var prefs: SharedPreferences? = null

    // ---- Appearance / Theme ----
    var themeMode by mutableStateOf(ThemeMode.DARK)
    val glassNavBar = mutableStateOf(true)

    // ---- Playback & Streaming ----
    val streamingQuality = mutableStateOf("Auto (Best)")
    val playbackSpeed = mutableStateOf("Normal")
    val autoplayNextEpisode = mutableStateOf(true)
    val autoplayPreviews = mutableStateOf(true)

    // ---- Subtitles & Audio ----
    val subtitleLanguage = mutableStateOf("English")
    val subtitleSize = mutableStateOf("Medium")
    val audioLanguage = mutableStateOf("Default / Original")

    // ---- Downloads ----
    val downloadQuality = mutableStateOf("720p (Recommended)")
    val wifiOnlyDownloads = mutableStateOf(true)

    // ---- App Preferences ----
    val dataSaver = mutableStateOf(false)
    val appLanguage = mutableStateOf("English")
    val notifyNewReleases = mutableStateOf(true)
    val notifyNewEpisodes = mutableStateOf(true)

    // ---- Library state ----
    val watchlistIds = mutableStateListOf<String>()
    val downloads = mutableStateMapOf<String, DownloadEntry>()
    val readNotificationIds = mutableStateListOf<String>()
    val recentSearches = mutableStateListOf<String>()

    fun init(context: Context) {
        val p = context.getSharedPreferences("cinenova_user_settings", Context.MODE_PRIVATE)
        prefs = p

        themeMode = when (p.getString("theme_mode", "DARK")) {
            "LIGHT" -> ThemeMode.LIGHT
            "SYSTEM" -> ThemeMode.SYSTEM
            else -> ThemeMode.DARK
        }
        glassNavBar.value = p.getBoolean("glass_nav_bar", true)

        streamingQuality.value = p.getString("streaming_quality", "Auto (Best)") ?: "Auto (Best)"
        playbackSpeed.value = p.getString("playback_speed", "Normal") ?: "Normal"
        autoplayNextEpisode.value = p.getBoolean("autoplay_next", true)
        autoplayPreviews.value = p.getBoolean("autoplay_previews", true)

        subtitleLanguage.value = p.getString("subtitle_language", "English") ?: "English"
        subtitleSize.value = p.getString("subtitle_size", "Medium") ?: "Medium"
        audioLanguage.value = p.getString("audio_language", "Default / Original") ?: "Default / Original"

        downloadQuality.value = p.getString("download_quality", "720p (Recommended)") ?: "720p (Recommended)"
        wifiOnlyDownloads.value = p.getBoolean("wifi_only_downloads", true)
        dataSaver.value = p.getBoolean("data_saver", false)
        appLanguage.value = p.getString("app_language", "English") ?: "English"

        p.getStringSet("watchlist_ids", emptySet())?.let {
            watchlistIds.clear()
            watchlistIds.addAll(it)
        }
    }

    private fun persist() {
        prefs?.edit()?.apply {
            putString("theme_mode", themeMode.name)
            putBoolean("glass_nav_bar", glassNavBar.value)
            putString("streaming_quality", streamingQuality.value)
            putString("playback_speed", playbackSpeed.value)
            putBoolean("autoplay_next", autoplayNextEpisode.value)
            putBoolean("autoplay_previews", autoplayPreviews.value)
            putString("subtitle_language", subtitleLanguage.value)
            putString("subtitle_size", subtitleSize.value)
            putString("audio_language", audioLanguage.value)
            putString("download_quality", downloadQuality.value)
            putBoolean("wifi_only_downloads", wifiOnlyDownloads.value)
            putBoolean("data_saver", dataSaver.value)
            putString("app_language", appLanguage.value)
            putStringSet("watchlist_ids", watchlistIds.toSet())
            apply()
        }
    }

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        persist()
    }

    fun setGlassNavBar(enabled: Boolean) {
        glassNavBar.value = enabled
        persist()
    }

    fun updateStreamingQuality(q: String) {
        streamingQuality.value = q
        persist()
    }

    fun updatePlaybackSpeed(s: String) {
        playbackSpeed.value = s
        persist()
    }

    fun updateSubtitleLanguage(l: String) {
        subtitleLanguage.value = l
        persist()
    }

    fun updateSubtitleSize(s: String) {
        subtitleSize.value = s
        persist()
    }

    fun updateAudioLanguage(l: String) {
        audioLanguage.value = l
        persist()
    }

    fun updateDownloadQuality(q: String) {
        downloadQuality.value = q
        persist()
    }

    fun setWifiOnly(enabled: Boolean) {
        wifiOnlyDownloads.value = enabled
        persist()
    }

    fun setDataSaver(enabled: Boolean) {
        dataSaver.value = enabled
        persist()
    }

    fun updateAppLanguage(l: String) {
        appLanguage.value = l
        persist()
    }

    fun isInWatchlist(id: String): Boolean = id in watchlistIds

    fun toggleWatchlist(id: String) {
        if (id in watchlistIds) watchlistIds.remove(id) else watchlistIds.add(id)
        persist()
    }

    fun downloadEntry(id: String): DownloadEntry? = downloads[id]

    fun toggleDownload(
        id: String,
        title: String? = null,
        posterUrl: String? = null,
        episodeLabel: String? = null,
        sizeMb: Long = 480,
    ) {
        val existing = downloads[id]
        if (existing == null) {
            downloads[id] = DownloadEntry(
                itemId = id,
                state = DownloadState.DOWNLOADING,
                progressPercent = 35,
                sizeMb = sizeMb,
                episodeLabel = episodeLabel,
                title = title,
                posterUrl = posterUrl,
            )
        } else if (existing.state == DownloadState.COMPLETED) {
            downloads.remove(id)
        } else {
            downloads[id] = existing.copy(state = DownloadState.COMPLETED, progressPercent = 100)
        }
    }

    fun setDownloadState(id: String, state: DownloadState) {
        downloads[id]?.let { downloads[id] = it.copy(state = state) }
    }

    fun pauseDownload(id: String) {
        downloads[id]?.let { downloads[id] = it.copy(state = DownloadState.PAUSED) }
    }

    fun resumeDownload(id: String) {
        downloads[id]?.let { downloads[id] = it.copy(state = DownloadState.DOWNLOADING) }
    }

    fun removeDownload(id: String) {
        downloads.remove(id)
    }

    fun clearAllDownloads() {
        downloads.clear()
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
