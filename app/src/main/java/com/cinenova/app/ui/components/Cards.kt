package com.cinenova.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.WatchProgress
import com.cinenova.app.ui.theme.CardSize
import com.cinenova.app.ui.theme.Motion
import com.cinenova.app.ui.theme.Spacing

/**
 * Vertical 2:3 poster card used in content rows and grids.
 */
@Composable
fun MoviePosterCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = CardSize.posterWidth,
    showWatchlistState: Boolean = false,
) {
    Column(
        modifier = modifier
            .width(width)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Movie poster for ${item.title}" },
    ) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Box {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                )
                RatingBadge(item.rating, Modifier.align(Alignment.TopStart).padding(Spacing.sm), dark = true)
                if (showWatchlistState && com.cinenova.app.data.AppStore.isInWatchlist(item.id)) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "In watchlist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.TopEnd).padding(Spacing.sm).size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(
            item.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                "${item.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                if (item.type == MediaType.TV) "Series" else "${item.runtimeMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Wide 16:9 landscape card for rows like "New Releases".
 */
@Composable
fun LandscapeMovieCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = CardSize.landscapeWidth,
) {
    Column(
        modifier = modifier
            .width(width)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            AsyncImage(
                model = item.backdropUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.align(Alignment.Center).size(40.dp),
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play ${item.title}",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            "${item.year} · ${item.genres.firstOrNull() ?: ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Continue-watching card with progress bar and resume affordance.
 */
@Composable
fun ContinueWatchingCard(
    item: MediaItem,
    progress: WatchProgress,
    onResume: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(CardSize.continueWatchingWidth)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onResume),
    ) {
        Box(Modifier.clip(MaterialTheme.shapes.medium)) {
            AsyncImage(
                model = item.backdropUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.Center).size(44.dp),
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Resume ${item.title}",
                    tint = Color.White,
                    modifier = Modifier.padding(10.dp),
                )
            }
            LinearProgressIndicator(
                progress = { progress.fraction },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(4.dp),
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        val subtitle = progress.episodeLabel ?: "${item.year}"
        Text(
            "$subtitle · ${progress.remainingMinutes} min left",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Row {
            Text(
                "Resume",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onResume)
                    .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
            )
            Spacer(Modifier.width(Spacing.md))
            Text(
                "Remove",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onRemove)
                    .padding(vertical = Spacing.xs),
            )
        }
    }
}

/**
 * Horizontal search-result row with poster thumb.
 */
@Composable
fun SearchResultCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Surface(shape = MaterialTheme.shapes.small, modifier = Modifier.width(56.dp)) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(
                    if (item.type == MediaType.TV) "Series" else "Movie",
                    "${item.year}",
                    item.genres.take(2).joinToString(" · "),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RatingBadge(item.rating)
    }
}
