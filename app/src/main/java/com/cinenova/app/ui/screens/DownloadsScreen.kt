package com.cinenova.app.ui.screens

import android.app.DownloadManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.DownloadState
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.theme.Motion
import com.cinenova.app.ui.theme.Spacing

/**
 * Downloads hub: downloading queue, completed library, storage management,
 * and empty state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(onBack: () -> Unit, onOpenDetails: (String) -> Unit, onExplore: () -> Unit) {
    val entries = AppStore.downloads.values.toList()
    val downloading = entries.filter { it.state == DownloadState.DOWNLOADING || it.state == DownloadState.QUEUED || it.state == DownloadState.PAUSED }
    val completed = entries.filter { it.state == DownloadState.COMPLETED }
    val totalGb = String.format("%.1f", completed.sumOf { it.sizeMb } / 1024.0)

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Downloads") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            // ---- Storage usage card ----
            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Icon(
                            Icons.Outlined.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(Modifier.weight(1f)) {
                            Text("Storage used", style = MaterialTheme.typography.titleSmall)
                            LinearProgressIndicator(
                                progress = { (completed.size.toFloat() / 10).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                            )
                            Text(
                                "$totalGb GB of 16 GB available · ${completed.size} titles offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (downloading.isNotEmpty()) {
                item { SectionHeader("Downloading") }
                items(count = downloading.size, key = { "dl-" + downloading[it].itemId }) { index ->
                    val entry = downloading[index]
                    val item = DemoRepository.item(entry.itemId)
                    DownloadRow(
                        icon = when (entry.state) {
                            DownloadState.PAUSED -> Icons.Filled.PlayArrow
                            else -> Icons.Filled.Pause
                        },
                        progressPercent = entry.progressPercent,
                        label = "${entry.state.name.lowercase().replaceFirstChar { it.uppercase() }} — ${item?.title ?: entry.itemId}",
                        iconDescription = if (entry.state == DownloadState.PAUSED) "Resume" else "Pause",
                        onIconClick = {
                            AppStore.setDownloadState(
                                entry.itemId,
                                if (entry.state == DownloadState.PAUSED) DownloadState.DOWNLOADING else DownloadState.PAUSED,
                            )
                        },
                        onCancel = { AppStore.removeDownload(entry.itemId) },
                    )
                }
            }

            if (completed.isNotEmpty()) {
                item { SectionHeader("Available offline") }
                items(count = completed.size, key = { "done-" + completed[it].itemId + completed[it].episodeLabel }) { index ->
                    val entry = completed[index]
                    val item = DemoRepository.item(entry.itemId)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item?.title ?: entry.itemId, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                listOfNotNull(entry.episodeLabel, "${entry.sizeMb} MB").joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onOpenDetails(entry.itemId) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play ${item?.title}")
                        }
                        IconButton(onClick = { AppStore.removeDownload(entry.itemId) }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Delete download ${item?.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    icon: ImageVector,
    progressPercent: Int,
    label: String,
    iconDescription: String,
    onIconClick: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                LinearProgressIndicator(progress = { progressPercent / 100f }, modifier = Modifier.weight(1f))
                Text("$progressPercent%", style = MaterialTheme.typography.labelMedium)
            }
        }
        IconButton(
            onClick = onIconClick,
            modifier = Modifier.semantics { contentDescription = iconDescription },
        ) {
            Icon(icon, contentDescription = null)
        }
        IconButton(onClick = onCancel) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = "Cancel",
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
