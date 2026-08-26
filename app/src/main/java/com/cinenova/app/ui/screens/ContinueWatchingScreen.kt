package com.cinenova.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.WatchProgress
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.theme.Spacing

/**
 * Dedicated Continue Watching screen with resume / remove affordances.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinueWatchingScreen(
    onBack: () -> Unit,
    onResume: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    val entries = remember {
        DemoRepository.continueWatching.mapNotNull { p ->
            DemoRepository.item(p.itemId)?.let { p to it }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Continue Watching") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        if (entries.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.PlayCircle,
                title = "Nothing in progress",
                description = "Start watching something and pick up right where you left off.",
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(count = entries.size, key = { entries[it].first.itemId }) { index ->
                    val (progress, item) = entries[index]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = item.backdropUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(140.dp)
                                .aspectRatio(16f / 9f)
                                .clip(MaterialTheme.shapes.small),
                        )
                        Spacer(Modifier.width(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                progress.episodeLabel ?: "${item.year} · ${item.genres.firstOrNull()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LinearProgressIndicator(
                                progress = { progress.fraction },
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                            )
                            Text(
                                "${progress.remainingMinutes} min left",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onResume(item.id) }) {
                            Icon(Icons.Outlined.PlayCircle, contentDescription = "Resume ${item.title}")
                        }
                        IconButton(onClick = { onOpenDetails(item.id) }) {
                            Icon(
                                Icons.Outlined.RemoveCircleOutline,
                                contentDescription = "Remove ${item.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
