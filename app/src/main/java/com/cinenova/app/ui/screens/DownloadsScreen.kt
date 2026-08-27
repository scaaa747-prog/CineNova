package com.cinenova.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.DownloadEntry
import com.cinenova.app.data.DownloadState
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.theme.Spacing

/**
 * 100% Material 3 Downloads Manager:
 * Supports pause, resume, cancel, delete, clear all, and offline playback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
    onExplore: () -> Unit,
) {
    val entries = AppStore.downloads.values.toList()
    val downloading = entries.filter {
        it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED || it.state == DownloadState.PAUSED
    }
    val completed = entries.filter { it.state == DownloadState.COMPLETED }
    val totalGb = String.format("%.1f", completed.sumOf { it.sizeMb } / 1024.0)

    var showClearAllDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Delete all downloads?") },
            text = { Text("This will remove all downloaded movies and episodes from your device.") },
            confirmButton = {
                TextButton(onClick = {
                    AppStore.clearAllDownloads()
                    showClearAllDialog = false
                }) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Downloads") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (entries.isNotEmpty()) {
                    IconButton(onClick = { showClearAllDialog = true }) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = "Clear all downloads",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
        )

        if (entries.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Downloading,
                title = "No downloads yet",
                description = "Save movies and episodes to your device to watch them offline anywhere.",
                ctaLabel = "Find something to download",
                onCta = onExplore,
            )
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // ---- Storage usage card ----
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Icon(
                            Icons.Outlined.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Storage used", style = MaterialTheme.typography.titleSmall)
                            LinearProgressIndicator(
                                progress = { (completed.size.toFloat() / 10).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                            )
                            Text(
                                "$totalGb GB of 16 GB used · ${completed.size} titles offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ---- Downloading / In-Progress Queue ----
            if (downloading.isNotEmpty()) {
                item { SectionHeader("Downloading (${downloading.size})") }
                items(downloading, key = { "dl-" + it.itemId }) { entry ->
                    DownloadingCard(
                        entry = entry,
                        onPauseResume = {
                            if (entry.state == DownloadState.PAUSED) {
                                AppStore.resumeDownload(entry.itemId)
                            } else {
                                AppStore.pauseDownload(entry.itemId)
                            }
                        },
                        onCancel = { AppStore.removeDownload(entry.itemId) },
                    )
                }
            }

            // ---- Completed / Available Offline ----
            if (completed.isNotEmpty()) {
                item { SectionHeader("Available Offline (${completed.size})") }
                items(completed, key = { "done-" + it.itemId }) { entry ->
                    CompletedCard(
                        entry = entry,
                        onPlay = { onOpenDetails(entry.itemId) },
                        onDelete = { AppStore.removeDownload(entry.itemId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadingCard(
    entry: DownloadEntry,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit,
) {
    val isPaused = entry.state == DownloadState.PAUSED
    val title = entry.title ?: DemoRepository.item(entry.itemId)?.title ?: "Movie #${entry.itemId}"

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(Modifier.width(80.dp)) {
                if (!entry.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = entry.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(MaterialTheme.shapes.small),
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    ) {
                        Icon(
                            Icons.Outlined.Downloading,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    if (isPaused) "Paused" else "${entry.progressPercent}% of ${entry.sizeMb} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPaused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { entry.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            IconButton(onClick = onPauseResume) {
                Icon(
                    if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (isPaused) "Resume download" else "Pause download",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Cancel download",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun CompletedCard(
    entry: DownloadEntry,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
) {
    val title = entry.title ?: DemoRepository.item(entry.itemId)?.title ?: "Movie #${entry.itemId}"

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(Modifier.width(80.dp)) {
                if (!entry.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = entry.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(MaterialTheme.shapes.small),
                    )
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    ) {
                        Icon(
                            Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    listOfNotNull(entry.episodeLabel, "${entry.sizeMb} MB · Ready to watch offline").joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onPlay) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play offline",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete download",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
