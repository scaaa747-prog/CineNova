package com.cinenova.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.data.remote.dto.TabSectionDto
import com.cinenova.app.data.remote.mapper.toMediaItem
import com.cinenova.app.di.ServiceLocator
import com.cinenova.app.ui.components.ContentRow
import com.cinenova.app.ui.components.FeaturedHero
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * 100% Live Streaming Home Screen with Dedicated Search Navigation and Cache Fallback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSearch: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onPlay: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenContinueWatching: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }
    var liveSections by remember { mutableStateOf<List<TabSectionDto>>(emptyList()) }
    var heroItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun loadHomeFeed() {
        isLoading = true
        isOffline = false
        scope.launch {
            when (val result = ServiceLocator.catalogRepository.bootstrap()) {
                is ApiResult.Success -> {
                    isOffline = false
                    val sections = result.value.items.orEmpty()
                    liveSections = sections

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
                    isLoading = false
                }
                else -> {
                    if (liveSections.isEmpty()) {
                        isOffline = true
                    }
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadHomeFeed()
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("CineNova", style = MaterialTheme.typography.headlineSmall)
            },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
                IconButton(onClick = onOpenNotifications) {
                    BadgedIcon(unread = AppStore.unreadCount())
                }
            },
        )

        // Material 3 Pill Search Launcher
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSearch,
                ),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Search movies, series, anime...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(Spacing.xs))

        when {
            // ---- Loading Home State ----
            isLoading && liveSections.isEmpty() -> {
                LoadingSkeleton(lines = 8, hero = true)
            }

            // ---- Offline State (No dummy demo data) ----
            isOffline && liveSections.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.WifiOff,
                        contentDescription = "Offline",
                        modifier = Modifier.padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "You are offline",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Please check your network connection and retry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { loadHomeFeed() }) {
                        Text("Retry")
                    }
                }
            }

            // ---- Live Feeds State ----
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    if (heroItems.isNotEmpty()) {
                        item(key = "hero") {
                            FeaturedHero(
                                items = heroItems.take(5),
                                onPlay = { onPlay(it.id) },
                                onOpenDetails = { onOpenDetails(it.id) },
                            )
                        }
                    }

                    val movieSections = liveSections.filter {
                        it.type == "SUBJECTS_MOVIE" && !it.subjects.isNullOrEmpty()
                    }

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

                    item(key = "spacer") { Spacer(Modifier.height(Spacing.lg)) }
                }
            }
        }
    }
}

@Composable
private fun BadgedIcon(unread: Int) {
    if (unread > 0) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            Badge { Text(unread.toString()) }
        }
    } else {
        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
    }
}
