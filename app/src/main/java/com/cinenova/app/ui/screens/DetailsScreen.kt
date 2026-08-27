package com.cinenova.app.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.CastMember
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.Episode
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.data.remote.PlaybackResources
import com.cinenova.app.di.ServiceLocator
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.components.RatingBadge
import com.cinenova.app.ui.components.SeasonSelector
import com.cinenova.app.ui.theme.Spacing
import kotlinx.coroutines.launch

private fun startSystemDownload(
    context: Context,
    url: String,
    title: String,
    filename: String,
) {
    try {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setTitle(title)
            .setDescription("Downloading $title")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CineNova/$filename.mp4")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .addRequestHeader("User-Agent", "com.community.oneroom/50020045 (Linux; U; Android 13; en_US; 22101316G; Build/TQ2A.230405.003; Cronet/135.0.7012.3)")

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        dm?.enqueue(request)
    } catch (_: Exception) {}
}

/**
 * Details screen with Dub Selector, Season/Episode list, and advanced Episode/Quality Download Modal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    itemId: String,
    onBack: () -> Unit,
    onPlay: (String) -> Unit,
    onOpenDetails: (String) -> Unit,
    onNavigateDownloads: () -> Unit = {},
) {
    val context = LocalContext.current
    var currentItemId by remember(itemId) { mutableStateOf(itemId) }
    var liveItem by remember(currentItemId) { mutableStateOf(DemoRepository.item(currentItemId)) }
    var liveSeasons by remember(currentItemId) { mutableStateOf<List<Season>>(emptyList()) }
    var liveCast by remember(currentItemId) { mutableStateOf<List<CastMember>>(emptyList()) }
    var isLoading by remember(currentItemId) { mutableStateOf(liveItem == null) }
    val scope = rememberCoroutineScope()

    // Download modal state
    var showDownloadSheet by remember { mutableStateOf(false) }
    var playbackResources by remember { mutableStateOf<PlaybackResources?>(null) }
    var selectedQuality by remember { mutableStateOf("1080P Full HD") }
    var showDownloadStartedPopup by remember { mutableStateOf(false) }

    val selectedEpisodesToDownload = remember { mutableStateListOf<String>() }

    fun loadDetails(targetId: String) {
        val subjectId = targetId.toLongOrNull()
        if (subjectId != null) {
            isLoading = true
            scope.launch {
                when (val result = ServiceLocator.catalogRepository.subjectDetail(subjectId)) {
                    is ApiResult.Success -> {
                        if (result.value.title.isNotBlank()) {
                            liveItem = result.value
                        }
                    }
                    else -> Unit
                }

                val seasonsRes = ServiceLocator.catalogRepository.seasonsOf(subjectId)
                val castRes = ServiceLocator.catalogRepository.castOf(subjectId)
                val resRes = ServiceLocator.catalogRepository.playbackResources(subjectId, 0, 0)

                liveSeasons = seasonsRes.getOrNull().orEmpty()
                liveCast = castRes.getOrNull().orEmpty()
                playbackResources = resRes.getOrNull()
                playbackResources?.bestSource()?.let {
                    selectedQuality = it.qualityLabel
                }
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(currentItemId) {
        loadDetails(currentItemId)
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
                Button(onClick = { loadDetails(currentItemId) }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    val item = liveItem!!
    var inWatchlist by remember(currentItemId) { mutableStateOf(AppStore.isInWatchlist(item.id)) }
    var selectedSeason by remember { mutableIntStateOf(1) }
    val seasons = if (liveSeasons.isNotEmpty()) liveSeasons else DemoRepository.episodesOf(item)
    val castMembers = if (liveCast.isNotEmpty()) liveCast else DemoRepository.castFor[item.id].orEmpty()
    val isSeries = item.type == MediaType.TV || seasons.isNotEmpty()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
        ) {
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
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
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
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                if (isSeries) "TV Series" else item.ageRating,
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

                    // ---- Dubbed Audio Language Selector ----
                    if (item.dubs.isNotEmpty()) {
                        Text(
                            "Available Audio Dubs:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = Spacing.xs),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            item.dubs.forEach { dub ->
                                val isSelected = dub.subjectId == currentItemId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            currentItemId = dub.subjectId
                                        }
                                    },
                                    label = { Text(dub.languageName) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Language,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                )
                            }
                        }
                        Spacer(Modifier.height(Spacing.xs))
                    }

                    if (item.description.isNotBlank()) {
                        Text(
                            item.description,
                            style = MaterialTheme.typography.bodyMedium,
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
                        Button(
                            onClick = { showDownloadSheet = true },
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = "Download")
                            Text("Download", Modifier.padding(start = Spacing.xs))
                        }
                    }

                    Spacer(Modifier.height(Spacing.lg))
                }
            }

            // ---- TV Seasons & Episodes Grid ----
            if (isSeries && seasons.isNotEmpty()) {
                item {
                    Text(
                        "Seasons & Episodes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = Spacing.md),
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    if (seasons.size > 1) {
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
                val epList = currentSeason?.episodes.orEmpty()
                items(epList) { ep ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Episode ${ep.episodeNumber}: ${ep.title}", style = MaterialTheme.typography.titleSmall)
                                Text("${ep.runtimeMinutes} min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onPlay(item.id) }) {
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = "Play Episode", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    selectedEpisodesToDownload.clear()
                                    selectedEpisodesToDownload.add(ep.id)
                                    showDownloadSheet = true
                                }) {
                                    Icon(Icons.Outlined.Download, contentDescription = "Download Episode", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
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
                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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

        // Download Started Popup Card
        if (showDownloadStartedPopup) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text("Download Started", style = MaterialTheme.typography.titleSmall)
                            Text("Saving to device storage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(onClick = {
                        showDownloadStartedPopup = false
                        onNavigateDownloads()
                    }) {
                        Text("Go to Downloads")
                    }
                }
            }
        }

        // ---- Download Modal Bottom Sheet ----
        if (showDownloadSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDownloadSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Text(
                        "Download ${item.title}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.height(16.dp))

                    // Quality Selection
                    Text("Select Video Quality:", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    val sources = playbackResources?.sources.orEmpty()
                    if (sources.isNotEmpty()) {
                        sources.forEach { source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedQuality = source.qualityLabel }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedQuality == source.qualityLabel,
                                    onClick = { selectedQuality = source.qualityLabel },
                                )
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(source.qualityLabel, style = MaterialTheme.typography.bodyLarge)
                                    source.sizeBytes?.let { s ->
                                        if (s > 0) {
                                            val mb = s / (1024 * 1024)
                                            Text("$mb MB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        listOf("1080P Full HD (~1.2 GB)", "720P HD (~700 MB)", "480P Data Saver (~350 MB)").forEach { q ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedQuality = q }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedQuality == q,
                                    onClick = { selectedQuality = q },
                                )
                                Text(q, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }

                    // Series Episode Selection
                    if (isSeries && seasons.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Select Episodes to Download:", style = MaterialTheme.typography.titleSmall)
                        val allEp = seasons.flatMap { it.episodes }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedEpisodesToDownload.size == allEp.size) {
                                        selectedEpisodesToDownload.clear()
                                    } else {
                                        selectedEpisodesToDownload.clear()
                                        selectedEpisodesToDownload.addAll(allEp.map { it.id })
                                    }
                                },
                        ) {
                            Checkbox(
                                checked = selectedEpisodesToDownload.size == allEp.size,
                                onCheckedChange = { chk ->
                                    selectedEpisodesToDownload.clear()
                                    if (chk) selectedEpisodesToDownload.addAll(allEp.map { it.id })
                                },
                            )
                            Text("Select All (${allEp.size} episodes)", style = MaterialTheme.typography.bodyMedium)
                        }

                        LazyColumn(modifier = Modifier.height(140.dp)) {
                            items(allEp) { ep ->
                                val isSelected = ep.id in selectedEpisodesToDownload
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedEpisodesToDownload.remove(ep.id)
                                            else selectedEpisodesToDownload.add(ep.id)
                                        },
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { chk ->
                                            if (chk) selectedEpisodesToDownload.add(ep.id)
                                            else selectedEpisodesToDownload.remove(ep.id)
                                        },
                                    )
                                    Text("S${ep.seasonNumber}:E${ep.episodeNumber} · ${ep.title}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            showDownloadSheet = false
                            val chosenSource = playbackResources?.sources?.firstOrNull { it.qualityLabel == selectedQuality }
                                ?: playbackResources?.bestSource()
                            val downloadUrl = chosenSource?.url ?: "https://bcdn.hakunaymatata.com/sample.mp4"
                            val sizeMb = chosenSource?.sizeBytes?.let { it / (1024 * 1024) } ?: 480L

                            if (isSeries && selectedEpisodesToDownload.isNotEmpty()) {
                                selectedEpisodesToDownload.forEach { epId ->
                                    startSystemDownload(
                                        context = context,
                                        url = downloadUrl,
                                        title = "${item.title} ($epId)",
                                        filename = "${item.title.replace(" ", "_")}_$epId",
                                    )
                                }
                            } else {
                                startSystemDownload(
                                    context = context,
                                    url = downloadUrl,
                                    title = item.title,
                                    filename = "${item.title.replace(" ", "_")}_${selectedQuality.take(5)}",
                                )
                            }

                            // Register in AppStore downloads
                            AppStore.toggleDownload(
                                id = item.id,
                                title = item.title,
                                posterUrl = item.posterUrl,
                                sizeMb = sizeMb,
                            )
                            showDownloadStartedPopup = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                        val btnLabel = if (isSeries && selectedEpisodesToDownload.isNotEmpty()) {
                            "Download ${selectedEpisodesToDownload.size} Episodes"
                        } else "Start Download"
                        Text(btnLabel, Modifier.padding(start = 8.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
