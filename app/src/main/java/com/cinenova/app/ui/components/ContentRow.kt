package com.cinenova.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.MediaItem
import com.cinenova.app.ui.theme.Spacing

/**
 * Horizontally scrolling poster row used for Home content rails.
 */
@Composable
fun ContentRow(
    title: String,
    items: List<MediaItem>,
    onOpenDetails: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    landscape: Boolean = false,
    viewAllAction: (() -> Unit)? = null,
) {
    Column(modifier) {
        SectionHeader(title = title, actionLabel = if (viewAllAction != null) "See all" else null, onAction = viewAllAction)
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            items(count = items.size, key = { items[it].id }) { index ->
                val item = items[index]
                if (landscape) {
                    LandscapeMovieCard(item = item, onClick = { onOpenDetails(item) })
                } else {
                    MoviePosterCard(item = item, onClick = { onOpenDetails(item) }, showWatchlistState = true)
                }
            }
        }
    }
}
