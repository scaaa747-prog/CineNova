package com.cinenova.app.ui.screens

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.cinenova.app.data.DemoRepository
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.data.remote.PlaybackResources
import com.cinenova.app.data.remote.StreamResource
import com.cinenova.app.di.ServiceLocator
import kotlinx.coroutines.delay

private const val ANDROID_USER_AGENT =
    "com.community.oneroom/50020045 (Linux; U; Android 13; en_US; 22101316G; Build/TQ2A.230405.003; Cronet/135.0.7012.3)"

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

/**
 * Premium landscape video player with live Quality, Speed, Dub, and Subtitle controls.
 */
@Composable
fun PlayerScreen(itemId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val demoItem = remember(itemId) { DemoRepository.item(itemId) }
    var videoTitle by remember(itemId) { mutableStateOf(demoItem?.title ?: "") }
    var isTv by remember(itemId) { mutableStateOf(demoItem?.type == MediaType.TV) }

    // Lock screen to sensor landscape during video playback
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    var playbackResources by remember { mutableStateOf<PlaybackResources?>(null) }
    var currentSource by remember { mutableStateOf<StreamResource?>(null) }

    var showQualityDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    fun switchStream(newSource: StreamResource) {
        currentSource = newSource
        player?.let { p ->
            val curPos = p.currentPosition
            val isPlay = p.isPlaying
            val mediaItem = ExoMediaItem.fromUri(newSource.url)
            p.setMediaItem(mediaItem)
            p.prepare()
            p.seekTo(curPos)
            if (isPlay) p.play()
        }
    }

    // Resolve real live streaming resource from MovieBox API
    LaunchedEffect(itemId) {
        val subjectId = itemId.toLongOrNull()
        if (subjectId != null) {
            when (val detailRes = ServiceLocator.catalogRepository.subjectDetail(subjectId)) {
                is ApiResult.Success -> {
                    if (detailRes.value.title.isNotBlank()) {
                        videoTitle = detailRes.value.title
                    }
                    isTv = detailRes.value.type == MediaType.TV
                }
                else -> Unit
            }

            when (val result = ServiceLocator.catalogRepository.playbackResources(subjectId, season = 0, episode = 0)) {
                is ApiResult.Success -> {
                    playbackResources = result.value
                    val best = result.value.bestSource()
                    if (best != null && best.url.isNotBlank()) {
                        currentSource = best
                    }
                }
                else -> Unit
            }
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4000)
            controlsVisible = false
        }
    }

    // ExoPlayer lifecycle
    DisposableEffect(currentSource?.url) {
        val stream = currentSource?.url
        if (stream.isNullOrBlank()) {
            onDispose { }
        } else {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(ANDROID_USER_AGENT)
                .setConnectTimeoutMs(30_000)
                .setReadTimeoutMs(45_000)
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(mapOf("Range" to "bytes=0-"))

            val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

            val exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .apply {
                    val mediaItem = ExoMediaItem.fromUri(stream)
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                    setPlaybackSpeed(playbackSpeed)
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }

                        override fun onPlaybackStateChanged(state: Int) {
                            isBuffering = state == Player.STATE_BUFFERING
                            if (state == Player.STATE_READY) {
                                durationMs = duration.coerceAtLeast(0L)
                                hasError = false
                            }
                        }

                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            hasError = true
                            isBuffering = false
                        }
                    })
                }
            player = exoPlayer

            onDispose {
                exoPlayer.release()
                player = null
            }
        }
    }

    // Periodic position tracker
    LaunchedEffect(player, isPlaying) {
        while (true) {
            player?.let {
                if (it.isPlaying) {
                    positionMs = it.currentPosition.coerceAtLeast(0L)
                    durationMs = it.duration.coerceAtLeast(0L)
                }
            }
            delay(500)
        }
    }

    // Immersive system bars in landscape
    DisposableEffect(Unit) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            val w = activity?.window
            if (w != null) {
                val controller = WindowCompat.getInsetsController(w, w.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                }
            },
            update = { it.player = player },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { controlsVisible = !controlsVisible },
        )

        if (isBuffering && !hasError) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }

        if (hasError) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Video stream unavailable", color = Color.White)
                Spacer(Modifier.padding(4.dp))
                IconButton(onClick = { player?.prepare(); player?.play() }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Retry", tint = Color.White)
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(
                        videoTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )

                    // Quality Switcher Button
                    IconButton(onClick = { showQualityDialog = true }) {
                        Icon(
                            Icons.Outlined.HighQuality,
                            contentDescription = "Quality: ${currentSource?.qualityLabel ?: "Auto"}",
                            tint = Color.White,
                        )
                    }

                    // Playback Speed Button
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(
                            Icons.Outlined.Speed,
                            contentDescription = "Speed: ${playbackSpeed}x",
                            tint = Color.White,
                        )
                    }

                    // PiP Button
                    IconButton(onClick = {
                        activity?.enterPictureInPictureMode(
                            PictureInPictureParams.Builder()
                                .setAspectRatio(Rational(16, 9))
                                .build(),
                        )
                    }) {
                        Icon(Icons.Outlined.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
                    }
                }

                // Center Play/Pause & Skip Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { player?.seekTo((positionMs - 10_000).coerceAtLeast(0L)) }) {
                        Icon(Icons.Outlined.FastRewind, contentDescription = "-10s", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    IconButton(
                        onClick = {
                            player?.let {
                                if (it.isPlaying) it.pause() else it.play()
                            }
                        },
                        modifier = Modifier.size(64.dp),
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp),
                        )
                    }

                    IconButton(onClick = { player?.seekTo((positionMs + 10_000).coerceAtMost(durationMs)) }) {
                        Icon(Icons.Outlined.FastForward, contentDescription = "+10s", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }

                // Bottom Progress Bar & Time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatTime(positionMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
                        currentSource?.let {
                            Text(it.qualityLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(formatTime(durationMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                    Slider(
                        value = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f,
                        onValueChange = { frac ->
                            player?.seekTo((frac * durationMs).toLong())
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Quality Switcher Dialog
        if (showQualityDialog) {
            val sources = playbackResources?.sources.orEmpty()
            AlertDialog(
                onDismissRequest = { showQualityDialog = false },
                title = { Text("Select Video Quality") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (sources.isEmpty()) {
                            Text("Default HD Stream")
                        } else {
                            sources.forEach { source ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            switchStream(source)
                                            showQualityDialog = false
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        selected = currentSource?.url == source.url,
                                        onClick = {
                                            switchStream(source)
                                            showQualityDialog = false
                                        },
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
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQualityDialog = false }) { Text("Close") }
                },
            )
        }

        // Speed Dialog
        if (showSpeedDialog) {
            val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Playback Speed") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        speedOptions.forEach { spd ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playbackSpeed = spd
                                        player?.setPlaybackSpeed(spd)
                                        showSpeedDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = playbackSpeed == spd,
                                    onClick = {
                                        playbackSpeed = spd
                                        player?.setPlaybackSpeed(spd)
                                        showSpeedDialog = false
                                    },
                                )
                                Text(
                                    if (spd == 1.0f) "1.0x (Normal)" else "${spd}x",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeedDialog = false }) { Text("Close") }
                },
            )
        }
    }
}
