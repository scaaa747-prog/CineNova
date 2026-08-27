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
import androidx.compose.material.icons.outlined.WifiOff
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.cinenova.app.data.CastMember
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.di.ServiceLocator
import com.cinenova.app.ui.components.EpisodeCard
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.components.RatingBadge
import com.cinenova.app.ui.components.SeasonSelector
import com.cinenova.app.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Cinematic details screen for movies and TV shows.
 * Supports live API subjects with graceful offline/error states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    onPlay: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    var liveItem by remember(itemId) { mutableStateOf(DemoRepository.item(itemId)) }
    var liveSeasons by remember(itemId) { mutableStateOf<List<Season>>(emptyList()) }
    var liveCast by remember(itemId) { mutableStateOf<List<CastMember>>(emptyList()) }
    var isLoading by remember(itemId) { mutableStateOf(liveItem == null) }
    val scope = rememberCoroutineScope()

    fun loadDetails() {
        val subjectId = itemId.toLongOrNull()
        if (subjectId != null) {
            isLoading = true
            scope.launch {
                when (val result = ServiceLocator.catalogRepository.subjectDetail(subjectId)) {
                    is ApiResult.Success -> {
                        if (result.value.title.isNotBlank()) {
                            liveItem = result.value
                        }
                        val seasonsRes = ServiceLocator.catalogRepository.seasonsOf(subjectId)
                        val castRes = ServiceLocator.catalogRepository.castOf(subjectId)
                        liveSeasons = seasonsRes.getOrNull().orEmpty()
                        liveCast = castRes.getOrNull().orEmpty()
                        isLoading = false
                    }
                    else -> {
                        isLoading = false
                    }
                }
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(itemId) {
        loadDetails()
    }

    if (isLoading && liveItem == null) {
        Box(Modifier.fillMaxSize()) {
            LoadingSkeleton(lines = 8, hero = true)
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
        return
    }

    if (liveItem == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Outlined.WifiOff,
                contentDescription = "Error",
                modifier = Modifier.padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Could not load details",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Please check your internet connection and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onBack) {
                    Text("Go Back")
                }
                Button(onClick = { loadDetails() }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    val item = liveItem!!
    var inWatchlist by remember(itemId) { mutableStateOf(AppStore.isInWatchlist(item.id)) }
    var downloadState by remember(itemId) {
        mutableStateOf(AppStore.downloadEntry(item.id)?.state)
    }
    var selectedSeason by remember { mutableIntStateOf(1) }
    val seasons = if (liveSeasons.isNotEmpty()) liveSeasons else DemoRepository.episodesOf(item)
    val castMembers = if (liveCast.isNotEmpty()) liveCast else DemoRepository.castFor[item.id].orEmpty()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                // ---- Hero backdrop ----
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f),
                ) {
                    val imageUrl = item.backdropUrl.ifBlank { item.posterUrl }
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                ),
                            ),
                    )
                }

                // ---- Metadata section ----
                Column(Modifier.padding(horizontal = Spacing.md)) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        if (item.year > 0) {
                            Text("${item.year}", style = MaterialTheme.typography.labelLarge)
                        }
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                item.ageRating,
                                Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        if (item.runtimeMinutes > 0) {
                            Text("${item.runtimeMinutes} min", style = MaterialTheme.typography.labelLarge)
                        }
                        if (item.rating > 0.0) {
                            RatingBadge(item.rating)
                        }
                    }

                    Spacer(Modifier.height(Spacing.sm))

                    if (item.description.isNotBlank()) {
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Spacing.md))
                    }

                    // ---- Genre chips ----
                    if (item.genres.isNotEmpty()) {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            item.genres.forEach { GenreChip(it, selected = false, onClick = {}) }
                        }
                        Spacer(Modifier.height(Spacing.md))
                    }

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
                    }

                    Spacer(Modifier.height(Spacing.lg))
                }
            }

            // ---- TV Seasons / Episodes ----
            if (item.type == MediaType.TV && seasons.isNotEmpty()) {
                if (seasons.size > 1) {
                    item {
                        SeasonSelector(
                            seasons = seasons,
                            selectedSeason = selectedSeason,
                            onSelect = { selectedSeason = it },
                            modifier = Modifier.padding(horizontal = Spacing.md),
                        )
                        Spacer(Modifier.height(Spacing.sm))
                    }
                }
                val currentSeason = seasons.firstOrNull { it.number == selectedSeason } ?: seasons.firstOrNull()
                items(currentSeason?.episodes?.size ?: 0) { index ->
                    val ep = currentSeason!!.episodes[index]
                    EpisodeCard(
                        episode = ep,
                        watched = false,
                        onPlay = { onPlay(item.id) },
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    )
                }
            }

            // ---- Cast horizontal rail ----
            if (castMembers.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        "Cast & Crew",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = Spacing.md),
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        castMembers.forEach { member ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(72.dp),
                            ) {
                                if (member.avatarUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = member.avatarUrl,
                                        contentDescription = member.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape),
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    )
                                }
                                Spacer(Modifier.height(Spacing.xs))
                                Text(
                                    member.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    member.role,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.xl))
                }
            }
        }

        // Top bar back button overlay
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )
    }
}
