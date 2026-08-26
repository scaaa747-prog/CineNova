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
import com.cinenova.app.ui.components.ContentRow
import com.cinenova.app.ui.components.ContinueWatchingCard
import com.cinenova.app.ui.components.FeaturedHero
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.components.OfflineBanner
import com.cinenova.app.ui.theme.Spacing

/**
 * Immersive streaming Home: hero carousel + content rails.
 * Demonstrates loading / success states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDetails: (String) -> Unit,
    onPlay: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenContinueWatching: () -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(600)
        loading = false
    }
    var offline by remember { mutableStateOf(false) }

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

        if (loading) {
            LoadingSkeleton()
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            item {
                FeaturedHero(
                    items = DemoRepository.trending.take(5),
                    onPlay = { onPlay(it.id) },
                    onOpenDetails = { onOpenDetails(it.id) },
                )
            }

            item {
                ContinueWatchingRow(
                    onOpenDetails = onOpenDetails,
                    onPlay = onPlay,
                    onSeeAll = onOpenContinueWatching,
                )
            }

            item {
                ContentRow("Trending Now", DemoRepository.trending, { onOpenDetails(it.id) }, landscape = true)
            }
            item { ContentRow(title = "Popular Movies", items = DemoRepository.popularMovies, onOpenDetails = { onOpenDetails(it.id) }) }
            item { ContentRow(title = "Popular TV Shows", items = DemoRepository.popularTv, onOpenDetails = { onOpenDetails(it.id) }) }
            item { ContentRow(title = "New Releases", items = DemoRepository.newReleases, onOpenDetails = { onOpenDetails(it.id) }, landscape = true) }
            item { ContentRow(title = "Top Rated", items = DemoRepository.topRated.take(10), onOpenDetails = { onOpenDetails(it.id) }) }
            item { ContentRow(title = "Recommended For You", items = DemoRepository.recommended, onOpenDetails = { onOpenDetails(it.id) }) }
            item { ContentRow(title = "Recently Added", items = DemoRepository.recentlyAdded, onOpenDetails = { onOpenDetails(it.id) }, landscape = true) }
            item { ContentRow(title = "Because You Watched Midnight Horizon", items = DemoRepository.becauseYouWatched.take(8), onOpenDetails = { onOpenDetails(it.id) }) }

            item { Spacer(Modifier.height(Spacing.lg)) }
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
                        // Demo scope: removal is a no-op placeholder in static demo data.
                        onOpenDetails(item.id)
                    },
                )
            }
        }
    }
}
