package com.cinenova.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.ui.components.EpisodeCard
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.RatingBadge
import com.cinenova.app.ui.components.SeasonSelector
import com.cinenova.app.ui.theme.Spacing

/**
 * Cinematic details screen for movies and TV shows.
 * Phones use a vertical layout; wide screens get a two-column layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    onPlay: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    val item = remember(itemId) { DemoRepository.item(itemId) } ?: return
    var inWatchlist by remember(itemId) { mutableStateOf(AppStore.isInWatchlist(item.id)) }
    var downloadState by remember(itemId) {
        mutableStateOf(AppStore.downloadEntry(item.id)?.state)
    }
    var selectedSeason by remember { mutableIntStateOf(1) }
    val seasons = remember(itemId) { DemoRepository.episodesOf(item) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                // ---- Hero backdrop ----
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (LocalConfiguration.current.screenWidthDp >= 600) 21f / 9f else 16f / 9f),
                ) {
                    AsyncImage(
                        model = item.backdropUrl,
                        contentDescription = "Cinematic backdrop for ${item.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                                    startY = 300f,
                                ),
                            ),
                    )
                }
            }

            item {
                Column(Modifier.padding(horizontal = Spacing.md)) {
                    // ---- Poster + meta ----
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(shape = MaterialTheme.shapes.medium, modifier = Modifier.width(110.dp)) {
                            AsyncImage(
                                model = item.posterUrl,
                                contentDescription = "Movie poster for ${item.title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(2f / 3f),
                            )
                        }
                        Spacer(Modifier.width(Spacing.md))
                        Column {
                            RatingBadge(item.rating)
                            Spacer(Modifier.height(Spacing.xs))
                            Text(
                                listOfNotNull(
                                    "${item.year}",
                                    if (item.type == MediaType.TV) "Series" else "${item.runtimeMinutes} min",
                                    item.ageRating,
                                ).joinToString("  ·  "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                item.genres.joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.md))
                    Text(item.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(Spacing.sm))
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(Spacing.md))

                    // ---- Genre chips ----
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        item.genres.forEach { GenreChip(it, selected = false, onClick = {}) }
                    }

                    Spacer(Modifier.height(Spacing.md))

                    // ---- Primary actions ----
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        Button(onClick = { onPlay(item.id) }, shape = MaterialTheme.shapes.large) {
                            Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                            Text("Play", Modifier.padding(start = Spacing.xs))
                        }
                        FilledTonalButton(
                            onClick = {
                                AppStore.toggleWatchlist(item.id)
                                inWatchlist = AppStore.isInWatchlist(item.id)
                            },
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Text(if (inWatchlist) "✓ In Watchlist" else "+ Watchlist")
                        }
                        IconButton(onClick = {
                            AppStore.toggleDownload(item.id)
                            downloadState = AppStore.downloadEntry(item.id)?.state
                        }) {
                            Icon(
                                if (downloadState == com.cinenova.app.data.DownloadState.COMPLETED)
                                    Icons.Outlined.DownloadDone else Icons.Outlined.Download,
                                contentDescription = "Toggle download for ${item.title}",
                                tint = if (downloadState != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share ${item.title}")
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    // ---- TV episodes ----
                    if (item.type == MediaType.TV && seasons.isNotEmpty()) {
                        Text("Episodes", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(Spacing.sm))
                        SeasonSelector(seasons, selectedSeason, onSelect = { selectedSeason = it })
                        Spacer(Modifier.height(Spacing.sm))
                        seasons.firstOrNull { it.number == selectedSeason }?.episodes?.forEach { episode ->
                            EpisodeCard(
                                episode = episode,
                                watched = episode.episodeNumber < selectedSeason + 1,
                                onPlay = { onPlay(item.id) },
                            )
                        }
                        Spacer(Modifier.height(Spacing.lg))
                    }

                    // ---- Cast & Crew ----
                    Text("Cast & Crew", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        DemoRepository.castFor.getValue(item.id).forEach { member ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = member.avatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape),
                                )
                                Spacer(Modifier.height(Spacing.xs))
                                Text(member.name, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                Text(
                                    member.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    // ---- Trailers ----
                    Text("Trailers & Extras", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Spacing.sm))
                    DemoRepository.trailersFor.getValue(item.id).forEach { trailer ->
                        Row(
                            Modifier.padding(vertical = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            Icon(
                                Icons.Outlined.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text("${trailer.title} · ${trailer.durationMinutes}:${if (trailer.durationMinutes < 10) "0${trailer.durationMinutes}" else "00"}",
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))

                    // ---- Reviews ----
                    Text("Reviews", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Spacing.sm))
                    DemoRepository.reviewsFor.getValue(item.id).forEach { review ->
                        Column(Modifier.padding(vertical = Spacing.xs)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(review.author, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.width(Spacing.sm))
                                RatingBadge(review.rating)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    review.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(review.text, style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider(
                            Modifier.padding(vertical = Spacing.sm),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }

                    // ---- More Like This ----
                    Text("More Like This", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        DemoRepository.catalog.filter { it.id != item.id }.take(8).forEach { similar ->
                            com.cinenova.app.ui.components.MoviePosterCard(
                                item = similar,
                                onClick = { onOpenDetails(similar.id) },
                                width = 110.dp,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xl))
                }
            }
        }

        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
    }
}
