package com.cinenova.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DataSaverOn
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.TextFields
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cinenova.app.data.AppStore
import com.cinenova.app.ui.components.SectionHeader
import com.cinenova.app.ui.components.SettingItem
import com.cinenova.app.ui.components.SettingsDivider
import com.cinenova.app.ui.theme.Spacing
import com.cinenova.app.ui.theme.ThemeMode

/**
 * 100% Functional Settings & Preferences screen (Material 3).
 * Direct SharedPreferences persistence with real streaming & download controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onManageDownloads: () -> Unit) {
    var showStreamQualityDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }
    var showSubtitleLangDialog by remember { mutableStateOf(false) }
    var showSubtitleSizeDialog by remember { mutableStateOf(false) }
    var showAudioLangDialog by remember { mutableStateOf(false) }
    var showAppLangDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TopAppBar(title = { Text("Settings") })

        // ---- Appearance ----
        SectionHeader("Appearance")
        SettingItem(
            icon = Icons.Outlined.Contrast,
            title = "Theme",
            subtitle = when (AppStore.themeMode) {
                ThemeMode.LIGHT -> "Light Theme"
                ThemeMode.DARK -> "Dark Theme"
                ThemeMode.SYSTEM -> "Follow System"
            },
        )
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
        ) {
            SegmentedButton(
                selected = AppStore.themeMode == ThemeMode.LIGHT,
                onClick = { AppStore.setTheme(ThemeMode.LIGHT) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
            ) { Text("Light") }
            SegmentedButton(
                selected = AppStore.themeMode == ThemeMode.DARK,
                onClick = { AppStore.setTheme(ThemeMode.DARK) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
            ) { Text("Dark") }
            SegmentedButton(
                selected = AppStore.themeMode == ThemeMode.SYSTEM,
                onClick = { AppStore.setTheme(ThemeMode.SYSTEM) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
            ) { Text("System") }
        }
        SettingsDivider()

        // ---- Playback ----
        SectionHeader("Playback & Video")
        SettingItem(
            icon = Icons.Outlined.HighQuality,
            title = "Streaming Quality",
            subtitle = AppStore.streamingQuality.value,
            onClick = { showStreamQualityDialog = true },
        )
        SettingItem(
            icon = Icons.Outlined.Speed,
            title = "Default Playback Speed",
            subtitle = AppStore.playbackSpeed.value,
            onClick = { showSpeedDialog = true },
        )
        ToggleSetting(
            title = "Data Saver Mode",
            subtitle = "Prioritizes 480p and lower bitrate on mobile data",
            checked = AppStore.dataSaver.value,
            icon = Icons.Outlined.DataSaverOn,
            onCheckedChange = { AppStore.setDataSaver(it) },
        )
        SettingsDivider()

        // ---- Subtitles & Audio ----
        SectionHeader("Subtitles & Audio")
        SettingItem(
            icon = Icons.Outlined.ClosedCaption,
            title = "Preferred Subtitle Language",
            subtitle = AppStore.subtitleLanguage.value,
            onClick = { showSubtitleLangDialog = true },
        )
        SettingItem(
            icon = Icons.Outlined.TextFields,
            title = "Subtitle Size",
            subtitle = AppStore.subtitleSize.value,
            onClick = { showSubtitleSizeDialog = true },
        )
        SettingItem(
            icon = Icons.Outlined.Language,
            title = "Preferred Audio Track",
            subtitle = AppStore.audioLanguage.value,
            onClick = { showAudioLangDialog = true },
        )
        SettingsDivider()

        // ---- Downloads ----
        SectionHeader("Downloads")
        SettingItem(
            icon = Icons.Outlined.Download,
            title = "Download Quality",
            subtitle = AppStore.downloadQuality.value,
            onClick = { showDownloadQualityDialog = true },
        )
        ToggleSetting(
            title = "Download over Wi-Fi only",
            subtitle = "Prevent high data consumption on mobile network",
            checked = AppStore.wifiOnlyDownloads.value,
            icon = Icons.Outlined.Wifi,
            onCheckedChange = { AppStore.setWifiOnly(it) },
        )
        SettingItem(
            icon = Icons.Outlined.Storage,
            title = "Manage Downloads",
            subtitle = "View offline titles, pause, resume, and storage",
            onClick = onManageDownloads,
        )
        SettingsDivider()

        // ---- About & Info ----
        SectionHeader("About & Info")
        SettingItem(
            icon = Icons.Outlined.Language,
            title = "App Language",
            subtitle = AppStore.appLanguage.value,
            onClick = { showAppLangDialog = true },
        )
        SettingItem(
            icon = Icons.Outlined.Info,
            title = "About CineNova",
            subtitle = "Version 1.0.0",
            onClick = { showAboutDialog = true },
        )
        SettingItem(
            icon = Icons.Outlined.PrivacyTip,
            title = "Privacy & Storage",
            subtitle = "Zero tracking, local caching only",
            onClick = { showPrivacyDialog = true },
        )

        Spacer(Modifier.height(Spacing.xl))
    }

    // ---- Dialogs ----
    if (showStreamQualityDialog) {
        RadioDialog(
            title = "Streaming Quality",
            options = listOf("Auto (Best)", "1080p Full HD", "720p HD", "480p Data Saver"),
            selected = AppStore.streamingQuality.value,
            onSelect = {
                AppStore.updateStreamingQuality(it)
                showStreamQualityDialog = false
            },
            onDismiss = { showStreamQualityDialog = false },
        )
    }

    if (showSpeedDialog) {
        RadioDialog(
            title = "Default Playback Speed",
            options = listOf("0.5x", "0.75x", "Normal", "1.25x", "1.5x", "2.0x"),
            selected = AppStore.playbackSpeed.value,
            onSelect = {
                AppStore.updatePlaybackSpeed(it)
                showSpeedDialog = false
            },
            onDismiss = { showSpeedDialog = false },
        )
    }

    if (showDownloadQualityDialog) {
        RadioDialog(
            title = "Download Quality",
            options = listOf("1080p Full HD", "720p (Recommended)", "480p (Data Saver)"),
            selected = AppStore.downloadQuality.value,
            onSelect = {
                AppStore.updateDownloadQuality(it)
                showDownloadQualityDialog = false
            },
            onDismiss = { showDownloadQualityDialog = false },
        )
    }

    if (showSubtitleLangDialog) {
        RadioDialog(
            title = "Subtitle Language",
            options = listOf("Off", "English", "हिन्दी (Hindi)", "Español", "Français"),
            selected = AppStore.subtitleLanguage.value,
            onSelect = {
                AppStore.updateSubtitleLanguage(it)
                showSubtitleLangDialog = false
            },
            onDismiss = { showSubtitleLangDialog = false },
        )
    }

    if (showSubtitleSizeDialog) {
        RadioDialog(
            title = "Subtitle Font Size",
            options = listOf("Small", "Medium", "Large"),
            selected = AppStore.subtitleSize.value,
            onSelect = {
                AppStore.updateSubtitleSize(it)
                showSubtitleSizeDialog = false
            },
            onDismiss = { showSubtitleSizeDialog = false },
        )
    }

    if (showAudioLangDialog) {
        RadioDialog(
            title = "Preferred Audio Track",
            options = listOf("Default / Original", "Hindi", "English"),
            selected = AppStore.audioLanguage.value,
            onSelect = {
                AppStore.updateAudioLanguage(it)
                showAudioLangDialog = false
            },
            onDismiss = { showAudioLangDialog = false },
        )
    }

    if (showAppLangDialog) {
        RadioDialog(
            title = "App Language",
            options = listOf("English", "हिन्दी (Hindi)", "Español"),
            selected = AppStore.appLanguage.value,
            onSelect = {
                AppStore.updateAppLanguage(it)
                showAppLangDialog = false
            },
            onDismiss = { showAppLangDialog = false },
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
                    "CineNova v1.0.0

High-performance Material 3 streaming app with real-time video playback, offline downloads, and low-data optimization.",
                )
            },
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            },
            title = { Text("Privacy & Storage") },
            text = {
                Text(
                    "CineNova respects your privacy:

• Zero personal data tracking.
• No third-party ad SDKs.
• Offline downloads and settings stored strictly on your local device.",
                )
            },
        )
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    icon: ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingItem(
        icon = icon ?: Icons.Outlined.Notifications,
        title = title,
        subtitle = subtitle,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { onSelect(option) },
                        )
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        },
    )
}
