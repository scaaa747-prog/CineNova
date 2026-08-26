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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinenova.app.ui.components.GenreChip
import com.cinenova.app.ui.components.LoadingSkeleton
import com.cinenova.app.ui.components.MoviePosterCard
import com.cinenova.app.ui.components.NoResultsState
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.theme.Spacing
import com.cinenova.app.viewmodel.CatalogViewModel
import kotlinx.coroutines.delay

private val popularSearchSuggestions = listOf(
    "Avatar", "Inception", "Spider-Man", "Batman", "Marvel",
    "Action", "Comedy", "Drama", "Sci-Fi", "Anime", "Hindi", "Horror"
)

/**
 * Dedicated Full-Page Search Screen with Material 3 Design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenDetails: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val searchVm: CatalogViewModel = viewModel()
    val searchState by searchVm.search.collectAsState()

    LaunchedEffect(query) {
        delay(300)
        if (query.isNotBlank()) {
            searchVm.search(query)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Search") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        // Material 3 Elevated Pill Search Field
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        "Search movies, series, anime...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }

        Spacer(Modifier.height(Spacing.sm))

        when {
            // ---- Active Search Results ----
            query.isNotBlank() -> {
                when (val state = searchState) {
                    is CatalogViewModel.SearchUiState.Loading -> {
                        LoadingSkeleton(lines = 6, hero = false)
                    }
                    is CatalogViewModel.SearchUiState.Error -> {
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
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                            Text("Network is slow or unreachable", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Please check your network coverage.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { searchVm.search(query) }) {
                                Text("Retry Search")
                            }
                        }
                    }
                    is CatalogViewModel.SearchUiState.Results -> {
                        if (state.items.isEmpty()) {
                            NoResultsState(query)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 110.dp),
                                contentPadding = PaddingValues(Spacing.md),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(state.items, key = { it.id }) { item ->
                                    MoviePosterCard(item = item, onClick = { onOpenDetails(item.id) })
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }

            // ---- Initial Suggestions State ----
            else -> {
                SectionHeader("Popular Searches")
                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    popularSearchSuggestions.forEach { term ->
                        GenreChip(
                            label = term,
                            selected = false,
                            onClick = { query = term },
                        )
                    }
                }
            }
        }
    }
}
