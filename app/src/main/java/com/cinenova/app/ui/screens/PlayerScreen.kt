package com.cinenova.app.ui.screens

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.media.AudioManager
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.cinenova.app.data.DemoRepository
import kotlinx.coroutines.delay

private const val DEMO_STREAM =
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

/**
 * Premium streaming player. Controls auto-fade after 3s of inactivity.
 * Includes buffering + error states, ±10s, speed, PiP and fullscreen.
 */
@Composable
fun PlayerScreen(itemId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val item = remember(itemId) { DemoRepository.item(itemId) }
    val isTv = item?.type == com.cinenova.app.data.MediaType.TV
    val episodes = remember(item) { item?.let { DemoRepository.episodesOf(it) } }

    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var hasError by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    val speeds = listOf(0.5f, 1f, 1.25f, 1.5f, 2f)

    fun hideControlsDelayed() {
        controlsVisible = true
    }

    DisposableEffect(itemId) {
        val exo = ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(DEMO_STREAM))
            playWhenReady = true
            prepare()
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    hasError = true
                }
            })
        }
        player = exo
        onDispose { exo.release() }
    }

    // Progress ticker + auto-hide controls
    LaunchedEffect(player) {
        while (true) {
            player?.let {
                positionMs = it.currentPosition.coerceAtLeast(0)
                durationMs = it.duration.coerceAtLeast(0)
            }
            delay(250)
            if (isPlaying && !isBuffering && System.currentTimeMillis() % 100 < 5) {
                controlsVisible = false
            }
        }
    }

    LaunchedEffect(isPlaying, isBuffering, controlsVisible) {
        if (controlsVisible) {
            delay(3000)
            if (isPlaying && !isBuffering) controlsVisible = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    show(WindowInsetsCompat.Type.systemBars())
                }
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
                PlayerView(ctx).apply { useController = false }
            },
            update = { view -> view.player = player },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { hideControlsDelayed() },
        )

        when {
            hasError -> PlayerError(onRetry = {
                hasError = false
                player?.apply {
                    seekTo(0)
                    prepare()
                }
            })

            isBuffering -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { player?.seekBy(-10_000) }) {
                    Icon(Icons.Outlined.FastRewind, "Back 10 seconds", tint = Color.White)
                }
                IconButton(onClick = {
                    player?.takeIf { p -> p.isPlaying }?.pause() ?: player?.play()
                }) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier
                            .semantics { contentDescription = "Play or pause" },
                    )
                }
                IconButton(onClick = { player?.seekBy(10_000) }) {
                    Icon(Icons.Outlined.FastForward, "Forward 10 seconds", tint = Color.White)
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close player", tint = Color.White)
                }
                Text(
                    item?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Slider(
                    value = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f,
                    onValueChange = { fraction ->
                        positionMs = (durationMs * fraction).toLong()
                        player?.seekTo(positionMs)
                    },
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Seek bar" },
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(positionMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Text(formatTime(durationMs), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row {
                        if (isTv) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.SkipPrevious, "Previous episode", tint = Color.White)
                            }
                        }
                        // Speed cycler
                        IconButton(onClick = {
                            val next = speeds[(speeds.indexOf(playbackSpeed) + 1).mod(speeds.size)]
                            playbackSpeed = next
                            player?.setPlaybackSpeed(next)
                        }) {
                            Text(
                                "${playbackSpeed}x",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        if (isTv) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.SkipNext, "Next episode", tint = Color.White)
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = {
                            activity?.enterPictureInPictureMode(
                                PictureInPictureParams.Builder()
                                    .setAspectRatio(Rational(16, 9))
                                    .build(),
                            )
                        }) {
                            Icon(Icons.Outlined.PictureInPictureAlt, "Picture in picture", tint = Color.White)
                        }
                        IconButton(onClick = {
                            activity?.window?.let { window ->
                                WindowCompat.getInsetsController(window, window.decorView).apply {
                                    systemBarsBehavior =
                                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                                    hide(WindowInsetsCompat.Type.systemBars())
                                }
                            }
                        }) {
                            Icon(Icons.Outlined.Fullscreen, "Fullscreen", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerError(onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Playback error", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.padding(4.dp))
        Text(
            "We couldn't play this title. Check your connection and try again.",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.padding(8.dp))
        androidx.compose.material3.Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun Player.seekBy(deltaMs: Long) = seekTo(currentPosition + deltaMs)
