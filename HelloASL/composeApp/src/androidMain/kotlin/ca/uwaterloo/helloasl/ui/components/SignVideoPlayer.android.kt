package ca.uwaterloo.helloasl.ui.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private const val TAG = "SignVideoPlayer"

@Composable
actual fun SignVideoPlayer(
    resourcePath: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    var isPlaying by remember(resourcePath) { mutableStateOf(false) }
    var playbackState by remember(resourcePath) { mutableStateOf(Player.STATE_IDLE) }
    var loadFailed by remember(resourcePath) { mutableStateOf(false) }

    val player = remember(resourcePath) {
        val uri = runCatching { Uri.parse(resourcePath) }.getOrNull()
        if (uri == null) {
            loadFailed = true
            null
        } else {
            Log.d(TAG, "Create ExoPlayer for $uri")
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = false // start paused until user taps
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
            }
        }
    }

    if (player == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = if (loadFailed) "Video unavailable" else "Loading video…", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    DisposableEffect(player, resourcePath) {
        val listener: Listener = object : Listener {
            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
            }

            override fun onPlayerError(error: PlaybackException) {
                loadFailed = true
                Log.e(TAG, "Player error for $resourcePath", error)
            }

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            runCatching { player.release() }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    this.player = player
                }
            },
            update = { view ->
                if (view.player !== player) view.player = player
            }
        )

        if (loadFailed) {
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                Text("Video unavailable", style = MaterialTheme.typography.bodyMedium)
            }
        } else if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_BUFFERING) {
            Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                Text("Loading video…", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                .clickable {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.playWhenReady = true
                        player.play()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.surface
            )
        }
    }
}