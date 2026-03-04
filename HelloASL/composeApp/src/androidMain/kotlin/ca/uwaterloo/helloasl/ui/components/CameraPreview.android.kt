package ca.uwaterloo.helloasl.ui.components

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
actual fun CameraPreview(modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val providerState = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                val cameraProvider = future.get()
                providerState.value = cameraProvider

                val preview = Preview.Builder()
                    .setTargetResolution(Size(640, 480))
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                try {
                    cameraProvider.unbindAll()

                    val boundFront = runCatching {
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview
                        )
                    }.isSuccess

                    if (!boundFront) {
                        val boundBack = runCatching {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        }.isSuccess

                        if (!boundBack) {
                            val anySelector = CameraSelector.Builder().build()
                            runCatching {
                                cameraProvider.bindToLifecycle(lifecycleOwner, anySelector, preview)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { providerState.value?.unbindAll() }
            providerState.value = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}