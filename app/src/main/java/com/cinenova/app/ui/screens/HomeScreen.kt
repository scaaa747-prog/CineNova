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
 * 0ms Instant Home Feed: Pre-cached with silent background revalidation.
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
    val initialCached = remember { ServiceLocator.catalogRepository.getCachedBootstrap() }
    val initialSections = remember { initialCached?.items.orEmpty() }
    val initialBanners = remember {
        val banners = initialSections.firstOrNull { it.type == "BANNER" }?.banner?.banners.orEmpty()
        banners.mapNotNull { b ->
            b.subject?.toMediaItem() ?: b.subjectId?.let { id ->
                MediaItem(
                    id = id,
                    title = b.content.orEmpty(),
                    posterUrl = b.image?.url.orEmpty(),
                    backdropUrl = b.image?.url.orEmpty(),
                )
            }
        }
    }

    var isLoading by remember { mutableStateOf(initialSections.isEmpty()) }
    var isOffline by remember { mutableStateOf(false) }
    var liveSections by remember { mutableStateOf(initialSections) }
    var heroItems by remember { mutableStateOf(initialBanners) }
    val scope = rememberCoroutineScope()

    fun refreshHomeFeed(silent: Boolean = false) {
        if (!silent && liveSections.isEmpty()) {
            isLoading = true
        }
        scope.launch {
            when (val result = ServiceLocator.catalogRepository.bootstrap(forceRefresh = true)) {
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
        // Silent background update without blocking UI
        refreshHomeFeed(silent = liveSections.isNotEmpty())
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "CineNova",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search movies and series")
                }
                IconButton(onClick = onOpenNotifications) {
                    val unread = AppStore.unreadCount()
                    Box {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                        if (unread > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.TopEnd),
                            ) {
                                Text("$unread")
                            }
                        }
                    }
                }
            },
        )

        // Material 3 Search Launcher Bar
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Search movies, anime, series, actors...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isLoading && liveSections.isEmpty()) {
            LoadingSkeleton(lines = 10, hero = true)
        } else if (isOffline && liveSections.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Icon(
                        Icons.Outlined.WifiOff,
                        contentDescription = "Offline",
                        modifier = Modifier.padding(bottom = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "You are offline",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        "Please check your internet connection and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = { refreshHomeFeed(silent = false) }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                // ---- Hero Featured Billboard ----
                if (heroItems.isNotEmpty()) {
                    item {
                        FeaturedHero(
                            items = heroItems,
                            onPlay = { heroItems.firstOrNull()?.let { onPlay(it.id) } },
                            onDetails = { heroItems.firstOrNull()?.let { onOpenDetails(it.id) } },
                        )
                    }
                }

                // ---- Dynamic Sections from API ----
                val validSections = liveSections.filter {
                    it.type != "BANNER" && !it.subjects.isNullOrEmpty()
                }

                items(validSections) { section ->
                    val mediaList = section.subjects.orEmpty().map { it.toMediaItem() }
                    ContentRow(
                        title = section.title ?: "Trending Now",
                        items = mediaList,
                        onItemClick = { onOpenDetails(it.id) },
                    )
                }

                item {
                    Spacer(Modifier.height(Spacing.xl))
                }
            }
        }
    }
}
