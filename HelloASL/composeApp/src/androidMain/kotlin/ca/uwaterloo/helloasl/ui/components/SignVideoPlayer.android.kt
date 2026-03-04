package ca.uwaterloo.helloasl.ui.components

import android.net.Uri
import android.os.SystemClock
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
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import helloasl.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import java.io.File

private const val TAG = "SignVideoPlayer"

/**
 * Android actual implementation using Media3 ExoPlayer (more stable than VideoView in Compose).
 * Uses TextureView to avoid SurfaceView rendering issues inside Compose/AndroidView containers.
 */
@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
actual fun SignVideoPlayer(
    resourcePath: String,
    modifier: Modifier
) {
    val context = LocalContext.current

    var cachedFile by remember(resourcePath) { mutableStateOf<File?>(null) }
    var loadFailed by remember(resourcePath) { mutableStateOf(false) }
    var reloadKey by remember(resourcePath) { mutableStateOf(0L) }

    // Cache resource into a UNIQUE file each time (avoid overwrite races)
    LaunchedEffect(resourcePath) {
        loadFailed = false
        val keyTs = SystemClock.uptimeMillis()
        reloadKey = keyTs

        cachedFile = runCatching {
            val bytes = Res.readBytes(resourcePath)

            val baseName = resourcePath.substringAfterLast('/')
            val nameNoExt = baseName.substringBeforeLast('.', baseName)
            val ext = baseName.substringAfterLast('.', "")
            val uniqueName = if (ext.isBlank()) "${nameNoExt}_$keyTs" else "${nameNoExt}_$keyTs.$ext"

            val dest = File(context.cacheDir, uniqueName)
            Log.d(TAG, "Caching '$resourcePath' -> ${dest.absolutePath} (bytes=${bytes.size})")
            dest.outputStream().use { it.write(bytes) }
            dest
        }.getOrElse { e ->
            loadFailed = true
            Log.e(TAG, "Failed to load resource: $resourcePath", e)
            null
        }
    }

    val file = cachedFile
    if (file == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(if (loadFailed) "Video unavailable" else "Loading video…", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val videoKey = "${file.absolutePath}#$reloadKey"
    val uri = remember(videoKey) { Uri.fromFile(file) }

    var isPlaying by remember(videoKey) { mutableStateOf(false) }

    // Create/own ExoPlayer per videoKey, and release on dispose
    val player = remember(videoKey) {
        Log.d(TAG, "Create ExoPlayer for $videoKey uri=$uri")
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = false // 默认暂停，待用户点击
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    DisposableEffect(videoKey) {
        val listener: Listener = object : Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }
        player.addListener(listener)
        onDispose {
            Log.d(TAG, "Dispose ExoPlayer for $videoKey (release)")
            player.removeListener(listener)
            runCatching { player.release() }
        }
    }

    key(videoKey) {
        Box(modifier = modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    Log.d(TAG, "factory: create PlayerView for $videoKey")
                    PlayerView(ctx).apply {
                        useController = false
                        this.player = player
                    }
                },
                update = { view ->
                    if (view.player !== player) {
                        Log.d(TAG, "update: re-attach player for $videoKey")
                        view.player = player
                    }
                }
            )

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
}