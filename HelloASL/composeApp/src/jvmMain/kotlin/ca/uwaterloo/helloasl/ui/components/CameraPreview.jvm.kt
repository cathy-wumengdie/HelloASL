package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage

@Composable
actual fun CameraPreview(modifier: Modifier) {
    var frameBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val converter = Java2DFrameConverter()
        var grabber: OpenCVFrameGrabber? = null

        try {
            var started = false

            for (index in 0..5) {
                try {
                    grabber = OpenCVFrameGrabber(index).apply {
                        imageWidth = 640
                        imageHeight = 480
                        start()
                    }
                    started = true
                    break
                } catch (_: Throwable) {
                    try {
                        grabber?.stop()
                    } catch (_: Throwable) {}
                    try {
                        grabber?.release()
                    } catch (_: Throwable) {}
                    grabber = null
                }
            }

            if (!started || grabber == null) {
                errorMessage = "No camera available"
                return@LaunchedEffect
            }

            while (isActive) {
                val image: BufferedImage? = withContext(Dispatchers.IO) {
                    val frame = grabber?.grab()
                    if (frame == null) null else converter.convert(frame)
                }

                if (image != null) {
                    frameBitmap = image.toComposeImageBitmap()
                }

                delay(33)
            }
        } catch (_: Throwable) {
            errorMessage = "Failed to start camera"
        } finally {
            try {
                grabber?.stop()
            } catch (_: Throwable) {}
            try {
                grabber?.release()
            } catch (_: Throwable) {}
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            frameBitmap != null -> {
                Image(
                    bitmap = frameBitmap!!,
                    contentDescription = "Camera preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            errorMessage != null -> {
                Text(errorMessage!!, style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                Text("Starting camera...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}