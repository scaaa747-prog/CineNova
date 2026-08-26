package com.cinenova.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cinenova.app.ui.theme.Spacing

/** Shimmer modifier for skeleton placeholders. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )
    this.alpha(alpha)
}

@Composable
private fun SkeletonBox(modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.shapes.small,
            )
            .shimmer(),
    )
}

/** Shimmering skeleton placeholder shown while content loads. */
@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier, lines: Int = 4, hero: Boolean = true) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.md),
    ) {
        if (hero) {
            SkeletonBox(Modifier.fillMaxWidth().height(180.dp))
            Spacer(Modifier.height(Spacing.md))
        }
        repeat(lines) {
            SkeletonBox(
                Modifier
                    .fillMaxWidth(if (it == lines - 1) 0.55f else 1f)
                    .height(16.dp),
            )
            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

/** Generic friendly empty state with icon, title, description and optional CTA. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp),
        )
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(Spacing.sm))
            Button(onClick = onCta, shape = MaterialTheme.shapes.large) {
                Text(ctaLabel)
            }
        }
    }
}

/** Error state with retry affordance. */
@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    EmptyState(
        icon = Icons.Outlined.ErrorOutline,
        title = "Something went wrong",
        description = message,
        modifier = modifier,
        ctaLabel = "Retry",
        onCta = onRetry,
    )
}

/** Offline banner strip; information is conveyed by icon + text, not color alone. */
@Composable
fun OfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
    ) {
        Row(
            Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.CloudOff,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "You're offline — showing downloaded content only.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Friendly no-results state used by search. */
@Composable
fun NoResultsState(query: String, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.SearchOff,
        title = "No results found",
        description = "Nothing matched \"$query\". Try a different spelling, or search by genre like \"Sci-Fi\" or \"Drama\".",
        modifier = modifier,
    )
}
