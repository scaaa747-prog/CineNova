package com.cinenova.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.MediaType
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.MoviePosterCard
import com.cinenova.app.ui.theme.Spacing

private enum class ListType(val label: String) { ALL("All"), MOVIES("Movies"), TV("TV") }

/**
 * Watchlist screen: filter chips + adaptive poster grid + empty state CTA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(onOpenDetails: (String) -> Unit, onExplore: () -> Unit) {
    var listType by remember { mutableStateOf(ListType.ALL) }
    var sortNewestFirst by remember { mutableStateOf(true) }

    val items = remember(AppStore.watchlistIds.size, listType, sortNewestFirst) {
        DemoRepository.items(AppStore.watchlistIds.toList())
            .filter { when (listType) {
                ListType.ALL -> true
                ListType.MOVIES -> it.type == MediaType.MOVIE
                ListType.TV -> it.type == MediaType.TV
            } }
            .let { if (sortNewestFirst) it.sortedByDescending { m -> m.year } else it.sortedBy { m -> m.title } }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Watchlist") })

        androidx.compose.foundation.layout.Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ListType.entries.forEach { type ->
                GenreChip(label = type.label, selected = listType == type, onClick = { listType = type })
            }
            GenreChip(
                label = if (sortNewestFirst) "Newest first" else "A – Z",
                selected = false,
                onClick = { sortNewestFirst = !sortNewestFirst },
            )
        }

        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.BookmarkBorder,
                title = "Your watchlist is empty",
                description = "Save movies and shows you want to watch by tapping the + on any title.",
                ctaLabel = "Explore movies",
                onCta = onExplore,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items, key = { it.id }) { item ->
                    MoviePosterCard(item = item, onClick = { onOpenDetails(item.id) })
                }
            }
        }
    }
}
