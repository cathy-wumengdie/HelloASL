package ca.uwaterloo.helloasl.ui.components

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
actual fun VideoRecorder(
    modifier: Modifier,
    isPreviewActive: Boolean,
    isRecording: Boolean,
    onVideoSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    var videoCaptureUseCase by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var currentRecording by remember { mutableStateOf<Recording?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(isPreviewActive) {
        if (!isPreviewActive) {
            currentRecording?.stop()
            currentRecording = null
        }
    }

    LaunchedEffect(isRecording, isPreviewActive, videoCaptureUseCase) {
        val videoCapture = videoCaptureUseCase ?: return@LaunchedEffect

        if (!isPreviewActive) return@LaunchedEffect

        if (isRecording && currentRecording == null) {
            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis())

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "asl_$name")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/HelloASL")
                }
            }

            val mediaStoreOutput = MediaStoreOutputOptions.Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(contentValues)
                .build()

            try {
                val pendingRecording: PendingRecording =
                    videoCapture.output.prepareRecording(context, mediaStoreOutput)

                currentRecording = pendingRecording.start(
                    ContextCompat.getMainExecutor(context)
                ) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            Log.d("AndroidVideoRecorder", "Recording started")
                        }

                        is VideoRecordEvent.Finalize -> {
                            val error = event.error
                            if (error == VideoRecordEvent.Finalize.ERROR_NONE) {
                                val savedUri = event.outputResults.outputUri
                                onVideoSaved(savedUri.toString())
                            } else {
                                onError("Recording failed: $error")
                            }
                            currentRecording = null
                        }
                    }
                }
            } catch (e: Exception) {
                currentRecording = null
                onError(e.message ?: "Failed to start recording.")
            }
        }

        if (!isRecording && currentRecording != null) {
            currentRecording?.stop()
            currentRecording = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            previewViewRef = previewView

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                Log.d("AndroidVideoRecorder", "Available camera infos: ${cameraProvider.availableCameraInfos}")

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()

                val videoCapture = VideoCapture.withOutput(recorder)

                previewUseCase = preview
                videoCaptureUseCase = videoCapture

                try {
                    cameraProvider.unbindAll()

                    if (isPreviewActive) {
                        // Pick an available camera: prefer front, then back
                        val cameraSelector = when {
                            cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }
                            cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }
                            else -> null
                        }

                        if (cameraSelector == null) {
                            Log.e("AndroidVideoRecorder", "No camera available on this device")
                            onError("No available camera found on device.")
                        } else {
                            Log.d("AndroidVideoRecorder", "Binding camera: $cameraSelector")
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                videoCapture
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AndroidVideoRecorder", "Failed to bind camera", e)
                    onError(e.message ?: "Failed to bind camera.")
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        update = { previewView ->
            previewViewRef = previewView

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                Log.d("AndroidVideoRecorder", "Available camera infos (update): ${cameraProvider.availableCameraInfos}")

                try {
                    cameraProvider.unbindAll()

                    if (isPreviewActive) {
                        val preview = previewUseCase ?: Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                            previewUseCase = it
                        }

                        val videoCapture = videoCaptureUseCase ?: run {
                            val recorder = Recorder.Builder()
                                .setQualitySelector(QualitySelector.from(Quality.HD))
                                .build()
                            VideoCapture.withOutput(recorder).also {
                                videoCaptureUseCase = it
                            }
                        }

                        // Pick an available camera for update binding as well
                        val updateCameraSelector = when {
                            cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                            cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                            else -> null
                        }

                        if (updateCameraSelector == null) {
                            Log.e("AndroidVideoRecorder", "No camera available on this device (update)")
                            onError("No available camera found on device.")
                        } else {
                            Log.d("AndroidVideoRecorder", "Binding camera (update): $updateCameraSelector")
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                updateCameraSelector,
                                preview,
                                videoCapture
                            )
                        }
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Failed to update camera.")
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}