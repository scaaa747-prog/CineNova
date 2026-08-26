package com.cinenova.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cinenova.app.data.AppStore
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.ui.components.EmptyState
import com.cinenova.app.ui.components.NotificationItem
import com.cinenova.app.ui.theme.Spacing

/**
 * Notification center with read/unread, clear-all and empty state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val notifications = DemoRepository.notifications

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Notifications") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = { AppStore.clearNotifications() }) {
                    Text("Clear all", color = MaterialTheme.colorScheme.primary)
                }
            },
        )

        if (AppStore.unreadCount() == 0 && AppStore.readNotificationIds.isNotEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Notifications,
                title = "All caught up",
                description = "You have no unread notifications.",
            )
            return@Column
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            items(count = notifications.size, key = { notifications[it].id }) { index ->
                val notification = notifications[index]
                NotificationItem(notification = notification, onClick = { AppStore.markNotificationRead(notification.id) })
            }
        }
    }
}
