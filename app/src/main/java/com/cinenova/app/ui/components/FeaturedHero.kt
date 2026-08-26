package com.cinenova.app.ui.components

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.ui.theme.IconSize
import com.cinenova.app.ui.theme.Spacing

/**
 * Immersive cinematic hero carousel for the Home screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturedHero(
    items: List<MediaItem>,
    onPlay: (MediaItem) -> Unit,
    onOpenDetails: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { items.size })

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(state = pagerState) { page ->
            val item = items[page]
            Column(Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f)
                        .clip(MaterialTheme.shapes.extraLarge),
                ) {
                    AsyncImage(
                        model = item.backdropUrl,
                        contentDescription = "Cinematic backdrop for ${item.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize()
                            .bottomScrim()
                            .clickableArea(onOpenDetails, item),
                    )
                    // Meta block overlaid at bottom-left
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(Spacing.lg),
                    ) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RatingBadge(item.rating, dark = true)
                            HeroMetaText("${item.year}")
                            HeroMetaText(formatRuntime(item))
                            HeroMetaText(item.ageRating)
                            HeroMetaText(item.genres.take(2).joinToString(" · "))
                        }
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.width(560.dp),
                        )
                        Spacer(Modifier.height(Spacing.md))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(height = 44.dp, width = 132.dp),
                                onClick = { onPlay(item) },
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.Filled.PlayArrow, null, Modifier.size(IconSize.medium))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Play", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Spacer(Modifier.width(Spacing.md))
                            Surface(
                                onClick = { onOpenDetails(item) },
                                shape = MaterialTheme.shapes.large,
                                color = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White,
                                modifier = Modifier.size(height = 44.dp, width = 150.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Outlined.Add,
                                        null,
                                        Modifier.size(IconSize.medium),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Watchlist", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Carousel indicator dots
        Row(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(items.size) { i ->
                val selected = pagerState.currentPage == i
                Box(
                    Modifier
                        .size(if (selected) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)),
                )
            }
        }
    }
}

private fun formatRuntime(item: MediaItem): String =
    if (item.type == MediaType.TV) "Series" else "${item.runtimeMinutes} min"

private fun Modifier.clickableArea(onOpenDetails: (MediaItem) -> Unit, item: MediaItem): Modifier =
    this.clickable { onOpenDetails(item) }

@Composable
private fun HeroMetaText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.8f),
    )
}
