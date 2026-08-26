package com.cinenova.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.DownloadState
import com.cinenova.app.ui.theme.Motion

/** Small rating pill with star icon. */
@Composable
fun RatingBadge(rating: Double, modifier: Modifier = Modifier, dark: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (dark) Color.Black.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (dark) Color.White else MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                String.format("%.1f", rating),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

/** Genre chip used in rows and filter bars. */
@Composable
fun GenreChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        onClick = onClick,
        label = { Text(label) },
        selected = selected,
        modifier = modifier,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    )
}

/** Primary filled play button with cinematic emphasis. */
@Composable
fun PlayButton(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = "Play $title" },
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null)
        Text("Play", Modifier.padding(start = 4.dp))
    }
}

/** Watchlist toggle with animated state change. */
@Composable
fun WatchlistButton(inWatchlist: Boolean, title: String, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.semantics { contentDescription = "Toggle watchlist for $title" },
    ) {
        AnimatedContent(
            targetState = inWatchlist,
            transitionSpec = { fadeIn(Motion.normal()) togetherWith fadeOut(Motion.fast()) },
            label = "watchlistAnim",
        ) { added ->
            Icon(
                if (added) Icons.Filled.CheckCircle else Icons.Outlined.Add,
                contentDescription = null,
                tint = if (added) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Download button reflecting queued / downloading / paused / completed states. */
@Composable
fun DownloadButton(
    state: DownloadState?,
    title: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.semantics { contentDescription = "Toggle download for $title" },
    ) {
        when (state) {
            null -> Icon(Icons.Outlined.Download, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            DownloadState.QUEUED, DownloadState.DOWNLOADING ->
                Icon(Icons.Filled.Downloading, null, tint = MaterialTheme.colorScheme.primary)
            DownloadState.PAUSED ->
                Icon(Icons.Outlined.PauseCircle, null, tint = MaterialTheme.colorScheme.tertiary)
            DownloadState.COMPLETED ->
                Icon(Icons.Filled.DownloadDone, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Bottom scrim gradient to keep text readable over artwork. */
fun Modifier.bottomScrim(): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
            startY = size.height * 0.45f,
        ),
    )
}
