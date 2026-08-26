package com.cinenova.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cinenova.app.data.AppStore
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.components.SettingItem
import com.cinenova.app.ui.components.SettingsDivider
import com.cinenova.app.ui.theme.ThemeMode
import com.cinenova.app.ui.theme.Spacing

/**
 * Profile = app settings & preferences. No accounts, no login.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onManageDownloads: () -> Unit) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(title = { Text("Profile") })

        // ---- Appearance ----
        SectionHeader("Appearance")
        SettingItem(
            icon = Icons.Outlined.Contrast,
            title = "Theme",
            subtitle = when (AppStore.themeMode) {
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
                ThemeMode.SYSTEM -> "System default"
            },
        )
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
        ) {
            SegmentedButton(
                selected = AppStore.themeMode == ThemeMode.LIGHT,
                onClick = { AppStore.themeMode = ThemeMode.LIGHT },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            ) { Text("Light") }
            SegmentedButton(
                selected = AppStore.themeMode == ThemeMode.DARK,
                onClick = { AppStore.themeMode = ThemeMode.DARK },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            ) { Text("Dark") }
            SegmentedButton(
                selected = AppStore.themeMode == ThemeMode.SYSTEM,
                onClick = { AppStore.themeMode = ThemeMode.SYSTEM },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            ) { Text("System") }
        }
        SettingsDivider()

        // ---- Playback ----
        SectionHeader("Playback")
        SettingItem(
            icon = Icons.Outlined.HighQuality,
            title = "Streaming quality",
            subtitle = AppStore.streamingQuality.value,
            onClick = {
                val options = listOf("Auto (up to 4K)", "1080p", "720p", "480p")
                val current = AppStore.streamingQuality.value
                AppStore.streamingQuality.value =
                    options[(options.indexOf(current) + 1).mod(options.size)]
            },
        )
        ToggleSetting("Autoplay next episode", AppStore.autoplayNextEpisode)
        ToggleSetting("Autoplay previews", AppStore.autoplayPreviews)
        SettingItem(
            icon = Icons.Outlined.Speed,
            title = "Playback speed",
            subtitle = AppStore.playbackSpeed.value,
            onClick = {
                val options = listOf("0.5x", "Normal", "1.25x", "1.5x", "2x")
                val current = AppStore.playbackSpeed.value
                AppStore.playbackSpeed.value =
                    options[(options.indexOf(current) + 1).mod(options.size)]
            },
        )
        SettingsDivider()

        // ---- Subtitles & Audio ----
        SectionHeader("Subtitles & Audio")
        SettingItem(
            icon = Icons.Outlined.ClosedCaption,
            title = "Subtitle language",
            subtitle = AppStore.subtitleLanguage.value,
            onClick = { showSubtitleDialog = true },
        )
        SettingItem(icon = Icons.Outlined.ClosedCaption, title = "Subtitle size", subtitle = AppStore.subtitleSize.value, onClick = {
            val options = listOf("Small", "Medium", "Large")
            val current = AppStore.subtitleSize.value
            AppStore.subtitleSize.value = options[(options.indexOf(current) + 1).mod(options.size)]
        })
        SettingItem(
            icon = Icons.Outlined.Language,
            title = "Audio language",
            subtitle = AppStore.audioLanguage.value,
            onClick = { showAudioDialog = true },
        )
        SettingsDivider()

        // ---- Downloads ----
        SectionHeader("Downloads")
        SettingItem(
            icon = Icons.Outlined.Download,
            title = "Download quality",
            subtitle = AppStore.downloadQuality.value,
            onClick = {
                val options = listOf("Standard", "High", "Maximum")
                val current = AppStore.downloadQuality.value
                AppStore.downloadQuality.value =
                    options[(options.indexOf(current) + 1).mod(options.size)]
            },
        )
        ToggleSetting("Download over Wi-Fi only", AppStore.wifiOnlyDownloads, icon = Icons.Outlined.Wifi)
        SettingItem(
            icon = Icons.Outlined.Storage,
            title = "Manage downloads",
            subtitle = "Storage usage and offline titles",
            onClick = onManageDownloads,
        )
        SettingsDivider()

        // ---- Notifications ----
        SectionHeader("Notifications")
        ToggleSetting("New releases", AppStore.notifyNewReleases, icon = Icons.Outlined.Notifications)
        ToggleSetting("New episodes", AppStore.notifyNewEpisodes)
        ToggleSetting("Recommendations", AppStore.notifyRecommendations)
        ToggleSetting("Download notifications", AppStore.notifyDownloads)
        SettingsDivider()

        // ---- App ----
        SectionHeader("App")
        ToggleSetting("Data saver", AppStore.dataSaver)
        SettingItem(
            icon = Icons.Outlined.Language,
            title = "App language",
            subtitle = AppStore.appLanguage.value,
            onClick = { showLanguageDialog = true },
        )
        SettingItem(
            icon = Icons.Outlined.Info,
            title = "About CineNova",
            subtitle = "Version 1.0.0",
            onClick = { showAboutDialog = true },
        )
        SettingItem(icon = Icons.Outlined.PrivacyTip, title = "Privacy policy", onClick = {})
        SettingItem(icon = Icons.Outlined.Info, title = "Terms of use", onClick = {})

        Spacer(Modifier.height(Spacing.xl))
    }

    if (showLanguageDialog) {
        RadioDialog(
            title = "App language",
            options = listOf("English", "Español", "Français", "العربية"),
            selected = AppStore.appLanguage.value,
            onSelect = {
                AppStore.appLanguage.value = it
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false },
        )
    }
    if (showSubtitleDialog) {
        RadioDialog(
            title = "Subtitle language",
            options = listOf("Off", "English", "Español", "Français", "Arabic"),
            selected = AppStore.subtitleLanguage.value,
            onSelect = {
                AppStore.subtitleLanguage.value = it
                showSubtitleDialog = false
            },
            onDismiss = { showSubtitleDialog = false },
        )
    }
    if (showAudioDialog) {
        RadioDialog(
            title = "Audio language",
            options = listOf("Original", "English", "Español", "Français"),
            selected = AppStore.audioLanguage.value,
            onSelect = {
                AppStore.audioLanguage.value = it
                showAudioDialog = false
            },
            onDismiss = { showAudioDialog = false },
        )
    }
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            },
            title = { Text("CineNova") },
            text = {
                Text(
                    "A Material You streaming experience.\nVersion 1.0.0\n\nCineNova is a demo product — all artwork is placeholder imagery.",
                )
            },
        )
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    state: androidx.compose.runtime.MutableState<Boolean>,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    SettingItem(
        icon = icon ?: Icons.Outlined.PlayCircle,
        title = title,
        trailing = {
            Switch(
                checked = state.value,
                onCheckedChange = { state.value = it },
            )
        },
    )
}

@Composable
private fun RadioDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = { onSelect(option) })
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}
