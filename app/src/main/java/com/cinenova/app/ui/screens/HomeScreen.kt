package com.cinenova.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.WatchProgress
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.data.remote.dto.TabSectionDto
import com.cinenova.app.data.remote.mapper.toMediaItem
import com.cinenova.app.di.ServiceLocator
import com.cinenova.app.ui.components.ContentRow
import com.cinenova.app.ui.components.ContinueWatchingCard
import com.cinenova.app.ui.components.FeaturedHero
import com.cinenova.app.ui.components.OfflineBanner
import com.cinenova.app.ui.theme.Spacing

/**
 * Immersive streaming Home: hero carousel + dynamic live API content rails.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDetails: (String) -> Unit,
    onPlay: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenContinueWatching: () -> Unit,
) {
    var offline by remember { mutableStateOf(false) }
    var liveSections by remember { mutableStateOf<List<TabSectionDto>>(emptyList()) }
    var heroItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    // Fallback demo rails
    val trending = remember { DemoRepository.trending }
    val popularMovies = remember { DemoRepository.popularMovies }
    val popularTv = remember { DemoRepository.popularTv }
    val newReleases = remember { DemoRepository.newReleases }
    val topRated = remember { DemoRepository.topRated.take(10) }
    val recommended = remember { DemoRepository.recommended }
    val recentlyAdded = remember { DemoRepository.recentlyAdded }
    val becauseYouWatched = remember { DemoRepository.becauseYouWatched.take(8) }

    LaunchedEffect(Unit) {
        when (val result = ServiceLocator.catalogRepository.bootstrap()) {
            is ApiResult.Success -> {
                offline = false
                val sections = result.value.items.orEmpty()
                liveSections = sections

                // Extract banner items for hero
                val banners = sections.firstOrNull { it.type == "BANNER" }?.banner?.banners.orEmpty()
                val bannerMedia = banners.mapNotNull { b ->
                    b.subject?.toMediaItem() ?: b.subjectId?.let { id ->
                        MediaItem(
                            id = id,
                            title = b.content.orEmpty(),
                            posterUrl = b.image?.url.orEmpty(),
                            backdropUrl = b.image?.url.orEmpty(),
                        )
                    }
                }
                if (bannerMedia.isNotEmpty()) {
                    heroItems = bannerMedia
                }
            }
            is ApiResult.NetworkError -> {
                offline = true
            }
            else -> Unit
        }
    }

    val displayHero = if (heroItems.isNotEmpty()) heroItems else trending.take(5)

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("CineNova", style = MaterialTheme.typography.headlineSmall)
            },
            actions = {
                IconButton(onClick = onOpenNotifications) {
                    BadgedIcon(unread = AppStore.unreadCount())
                }
            },
        )
        OfflineBanner(visible = offline)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item(key = "hero") {
                FeaturedHero(
                    items = displayHero,
                    onPlay = { onPlay(it.id) },
                    onOpenDetails = { onOpenDetails(it.id) },
                )
            }

            item(key = "continue-watching") {
                ContinueWatchingRow(
                    onOpenDetails = onOpenDetails,
                    onPlay = onPlay,
                    onSeeAll = onOpenContinueWatching,
                )
            }

            // Live API Sections if available
            val movieSections = liveSections.filter {
                it.type == "SUBJECTS_MOVIE" && !it.subjects.isNullOrEmpty()
            }

            if (movieSections.isNotEmpty()) {
                items(movieSections, key = { it.title ?: it.position.toString() }) { sec ->
                    val items = sec.subjects.orEmpty().map { it.toMediaItem() }
                    val isLandscape = sec.position == 1 || sec.position == 3
                    ContentRow(
                        title = sec.title ?: "Trending",
                        items = items,
                        onOpenDetails = { onOpenDetails(it.id) },
                        landscape = isLandscape,
                    )
                }
            } else {
                // Fallback default rails
                item(key = "trending") {
                    ContentRow("Trending Now", trending, { onOpenDetails(it.id) }, landscape = true)
                }
                item(key = "popular-movies") { ContentRow("Popular Movies", popularMovies, onOpenDetails = { onOpenDetails(it.id) }) }
                item(key = "popular-tv") { ContentRow("Popular TV Shows", popularTv, onOpenDetails = { onOpenDetails(it.id) }) }
                item(key = "new-releases") {
                    ContentRow("New Releases", newReleases, { onOpenDetails(it.id) }, landscape = true)
                }
                item(key = "top-rated") { ContentRow("Top Rated", topRated, onOpenDetails = { onOpenDetails(it.id) }) }
                item(key = "recommended") { ContentRow("Recommended For You", recommended, onOpenDetails = { onOpenDetails(it.id) }) }
                item(key = "recently-added") {
                    ContentRow("Recently Added", recentlyAdded, { onOpenDetails(it.id) }, landscape = true)
                }
                item(key = "because-watched") {
                    ContentRow("Because You Watched Midnight Horizon", becauseYouWatched, onOpenDetails = { onOpenDetails(it.id) })
                }
            }

            item(key = "spacer") { Spacer(Modifier.height(Spacing.lg)) }
        }
    }
}

@Composable
private fun BadgedIcon(unread: Int) {
    if (unread > 0) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications ($unread unread)")
            Badge { Text("$unread") }
        }
    } else {
        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
    }
}

@Composable
private fun ContinueWatchingRow(
    onOpenDetails: (String) -> Unit,
    onPlay: (String) -> Unit,
    onSeeAll: () -> Unit,
) {
    val entries: List<Pair<WatchProgress, MediaItem>> = DemoRepository.continueWatching
        .mapNotNull { p -> DemoRepository.item(p.itemId)?.let { p to it } }

    Column {
        com.cinenova.app.ui.components.SectionHeader(
            title = "Continue Watching",
            actionLabel = "See all",
            onAction = onSeeAll,
        )
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            items(count = entries.size, key = { entries[it].first.itemId }) { index ->
                val (progress, item) = entries[index]
                ContinueWatchingCard(
                    item = item,
                    progress = progress,
                    onResume = { onPlay(item.id) },
                    onRemove = {
                        onOpenDetails(item.id)
                    },
                )
            }
        }
    }
}
