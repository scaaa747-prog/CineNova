package com.cinenova.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.MediaType
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.components.ErrorState
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.components.MoviePosterCard
import com.cinenova.app.ui.components.NoResultsState
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.theme.Spacing

private enum class SortOption(val label: String) {
    RELEVANCE("Relevance"), RATING("Top rated"), YEAR("Newest"),
}

/**
 * Explore + Search: discovery grid with filters, segmented control, sort,
 * and complete search experience (empty / active / no results).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onOpenDetails: (String) -> Unit,
    isSearchRoute: Boolean = false,
) {
    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(isSearchRoute) }
    var typeFilter by remember { mutableStateOf<MediaType?>(null) }
    var genre by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableStateOf(SortOption.RELEVANCE) }
    var filtersExpanded by remember { mutableStateOf(true) }

    // ---- Live API search (debounced) ----
    val searchVm: com.cinenova.app.viewmodel.CatalogViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val apiSearchState by searchVm.search.collectAsState()
    androidx.compose.runtime.LaunchedEffect(query) {
        kotlinx.coroutines.delay(300)
        if (query.isNotBlank()) searchVm.search(query)
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (active) "Search" else "Explore") },
            actions = {
                IconButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Filters")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Sort, contentDescription = "Sort: ${sort.label}")
                }
            },
        )

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                active = true
            },
            placeholder = { Text("Search movies and shows") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        active = false
                    }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
        )

        Spacer(Modifier.height(Spacing.sm))

        when {
            // ---- Active search (live API + local filters) ----
            query.isNotBlank() -> {
                val state = apiSearchState
                when (state) {
                    is com.cinenova.app.viewmodel.CatalogViewModel.SearchUiState.Loading -> {
                        LoadingSkeleton(lines = 6, hero = false)
                    }
                    is com.cinenova.app.viewmodel.CatalogViewModel.SearchUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            onRetry = { searchVm.search(query) },
                        )
                    }
                    is com.cinenova.app.viewmodel.CatalogViewModel.SearchUiState.Results -> {
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

            // ---- Empty (pre-search) ----
            else -> {
                SectionHeader("Trending searches")
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    DemoRepository.trendingSearches.forEach { term ->
                        GenreChip(label = term, selected = false, onClick = { query = term; active = true })
                    }
                }

                if (AppStore.recentSearches.isNotEmpty()) {
                    SectionHeader(
                        title = "Recent searches",
                        actionLabel = "Clear",
                        onAction = { AppStore.clearRecentSearches() },
                    )
                    Column(Modifier.padding(horizontal = Spacing.md)) {
                        AppStore.recentSearches.forEach { recent ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.xs),
                            ) {
                                Icon(
                                    Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(recent, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                SectionHeader("Suggested searches")
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    DemoRepository.suggestedSearches.forEach { term ->
                        GenreChip(label = term, selected = false, onClick = { query = term; active = true })
                    }
                }

                Spacer(Modifier.height(Spacing.sm))

                // Browse-all discovery area
                FilterBar(typeFilter, onType = { typeFilter = it }, genre = genre, onGenre = { genre = it }, showGenres = filtersExpanded)
                val catalog = DemoRepository.byGenre(genre)
                    .filter { typeFilter == null || it.type == typeFilter }
                    .let { list ->
                        when (sort) {
                            SortOption.RATING -> list.sortedByDescending { it.rating }
                            SortOption.YEAR -> list.sortedByDescending { it.year }
                            SortOption.RELEVANCE -> list
                        }
                    }
                ResultsGrid(catalog, onOpenDetails)
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
                DemoRepository.allGenres.forEach { g ->
                    GenreChip(label = g, selected = genre == g, onClick = { onGenre(g) })
                }
            }
        }
    }
}

/**
 * Applies type / genre / sort controls to a result list client-side.
 */
private fun applyFilters(
    items: List<com.cinenova.app.data.MediaItem>,
    typeFilter: MediaType?,
    genre: String?,
    sort: SortOption,
): List<com.cinenova.app.data.MediaItem> = items
    .filter { typeFilter == null || it.type == typeFilter }
    .filter { genre == null || genre in it.genres }
    .let { list ->
        when (sort) {
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.YEAR -> list.sortedByDescending { it.year }
            SortOption.RELEVANCE -> list
        }
    }

/**
 * Adaptive poster grid — 2 columns on phones up to 7+ on desktop.
 */
@Composable
private fun ResultsGrid(items: List<com.cinenova.app.data.MediaItem>, onOpenDetails: (String) -> Unit) {
    if (items.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.Search,
            title = "Nothing here yet",
            description = "Try removing a filter to see more titles.",
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 110.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.id }) { item ->
            MoviePosterCard(item = item, onClick = { onOpenDetails(item.id) })
        }
    }
}
