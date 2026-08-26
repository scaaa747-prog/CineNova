package com.cinenova.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.AppNotification
import com.cinenova.app.data.NotificationKind
import com.cinenova.app.ui.theme.Spacing

@Composable
private fun kindIcon(kind: NotificationKind) = when (kind) {
    NotificationKind.NEW_RELEASE -> Icons.Outlined.NewReleases
    NotificationKind.NEW_EPISODE -> Icons.Outlined.Tv
    NotificationKind.RECOMMENDATION -> Icons.Outlined.Recommend
    NotificationKind.DOWNLOAD_COMPLETE -> Icons.Outlined.DownloadDone
    NotificationKind.APP -> Icons.Outlined.Notifications
}

/**
 * Notification center row with read/unread affordance.
 */
@Composable
fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unread = notification.unread && !com.cinenova.app.data.AppStore.readNotificationIds.contains(notification.id)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(if (unread) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface)
            .padding(Spacing.md)
            .semantics {
                contentDescription =
                    "${notification.title}. ${notification.body}. ${if (unread) "Unread" else "Read"}."
            },
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            kindIcon(notification.kind),
            contentDescription = null,
            tint = if (unread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(notification.title, style = MaterialTheme.typography.titleSmall)
            Text(
                notification.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                notification.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (unread) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
