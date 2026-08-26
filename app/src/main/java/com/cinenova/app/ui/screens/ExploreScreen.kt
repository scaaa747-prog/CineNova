package com.cinenova.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.data.remote.mapper.toMediaItem
import com.cinenova.app.di.ServiceLocator
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.components.ErrorState
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.components.MoviePosterCard
import com.cinenova.app.ui.components.NoResultsState
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.theme.Spacing
import com.cinenova.app.viewmodel.CatalogViewModel
import kotlinx.coroutines.delay

private enum class SortOption(val label: String) {
    RELEVANCE("Relevance"), RATING("Top rated"), YEAR("Newest"),
}

private val popularSearchSuggestions = listOf(
    "Avatar", "Inception", "Spider-Man", "Batman", "Marvel",
    "Action", "Comedy", "Drama", "Sci-Fi", "Anime"
)

private val commonGenres = listOf(
    "Action", "Adventure", "Comedy", "Drama", "Romance", "Horror", "Sci-Fi", "Thriller"
)

/**
 * 100% Live Explore & Search Screen with Material 3 Design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onOpenDetails: (String) -> Unit,
    isSearchRoute: Boolean = false,
) {
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<MediaType?>(null) }
    var genre by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableStateOf(SortOption.RELEVANCE) }
    var filtersExpanded by remember { mutableStateOf(true) }

    var liveCatalog by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoadingCatalog by remember { mutableStateOf(true) }
    var isCatalogOffline by remember { mutableStateOf(false) }

    // Live search ViewModel
    val searchVm: CatalogViewModel = viewModel()
    val apiSearchState by searchVm.search.collectAsState()

    LaunchedEffect(query) {
        delay(300)
        if (query.isNotBlank()) searchVm.search(query)
    }

    LaunchedEffect(Unit) {
        isLoadingCatalog = true
        when (val result = ServiceLocator.catalogRepository.bootstrap()) {
            is ApiResult.Success -> {
                val sections = result.value.items.orEmpty()
                val items = sections.flatMap { sec ->
                    sec.subjects.orEmpty().map { it.toMediaItem() }
                }.distinctBy { it.id }
                liveCatalog = items
                isLoadingCatalog = false
                isCatalogOffline = false
            }
            else -> {
                isCatalogOffline = true
                isLoadingCatalog = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (query.isNotBlank()) "Search" else "Explore") },
            actions = {
                IconButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Filters")
                }
                IconButton(onClick = {
                    sort = when (sort) {
                        SortOption.RELEVANCE -> SortOption.RATING
                        SortOption.RATING -> SortOption.YEAR
                        SortOption.YEAR -> SortOption.RELEVANCE
                    }
                }) {
                    Icon(Icons.Outlined.Sort, contentDescription = "Sort: ${sort.label}")
                }
            },
        )

        // Material 3 Search Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "Search movies, shows, anime...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        when {
            // ---- Active Live Search ----
            query.isNotBlank() -> {
                when (val state = apiSearchState) {
                    is CatalogViewModel.SearchUiState.Loading -> {
                        LoadingSkeleton(lines = 6, hero = false)
                    }
                    is CatalogViewModel.SearchUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { searchVm.search(query) },
                        )
                    }
                    is CatalogViewModel.SearchUiState.Results -> {
                        val results = applyFilters(state.items, typeFilter, genre, sort)
                        if (results.isEmpty()) {
                            NoResultsState(query)
                        } else {
                            FilterBar(typeFilter, onType = { typeFilter = it }, genre = genre, onGenre = { genre = it }, showGenres = filtersExpanded)
                            ResultsGrid(results, onOpenDetails)
                        }
                    }
                    else -> Unit
                }
            }

            // ---- Loading Initial Catalog ----
            isLoadingCatalog -> {
                LoadingSkeleton(lines = 8, hero = false)
            }

            // ---- Offline State ----
            isCatalogOffline && liveCatalog.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.WifiOff,
                        contentDescription = "Offline",
                        modifier = Modifier.padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text("You are offline", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Connect to internet to explore titles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---- Live Explore Catalog ----
            else -> {
                SectionHeader("Popular Searches")
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    popularSearchSuggestions.forEach { term ->
                        GenreChip(label = term, selected = false, onClick = { query = term })
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                FilterBar(typeFilter, onType = { typeFilter = it }, genre = genre, onGenre = { genre = it }, showGenres = filtersExpanded)
                val filtered = applyFilters(liveCatalog, typeFilter, genre, sort)
                ResultsGrid(filtered, onOpenDetails)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBar(
    typeFilter: MediaType?,
    onType: (MediaType?) -> Unit,
    genre: String?,
    onGenre: (String?) -> Unit,
    showGenres: Boolean,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.md)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = typeFilter == null,
                onClick = { onType(null) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            ) { Text("All") }
            SegmentedButton(
                selected = typeFilter == MediaType.MOVIE,
                onClick = { onType(MediaType.MOVIE) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            ) { Text("Movies") }
            SegmentedButton(
                selected = typeFilter == MediaType.TV,
                onClick = { onType(MediaType.TV) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            ) { Text("TV") }
        }
        if (showGenres) {
            Spacer(Modifier.height(Spacing.sm))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                GenreChip(label = "All genres", selected = genre == null, onClick = { onGenre(null) })
                commonGenres.forEach { g ->
                    GenreChip(label = g, selected = genre == g, onClick = { onGenre(g) })
                }
            }
        }
    }
}

private fun applyFilters(
    items: List<MediaItem>,
    typeFilter: MediaType?,
    genre: String?,
    sort: SortOption,
): List<MediaItem> = items
    .filter { typeFilter == null || it.type == typeFilter }
    .filter { genre == null || genre.lowercase() in it.genres.map { g -> g.lowercase() } }
    .let { list ->
        when (sort) {
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.YEAR -> list.sortedByDescending { it.year }
            SortOption.RELEVANCE -> list
        }
    }

@Composable
private fun ResultsGrid(items: List<MediaItem>, onOpenDetails: (String) -> Unit) {
    if (items.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.Search,
            title = "Nothing here yet",
            description = "Try removing a filter or searching for a title.",
        )
        return
    }
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
