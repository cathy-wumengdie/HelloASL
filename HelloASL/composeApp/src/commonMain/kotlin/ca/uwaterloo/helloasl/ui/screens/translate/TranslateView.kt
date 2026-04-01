package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ca.uwaterloo.helloasl.getPlatform
import ca.uwaterloo.helloasl.ui.components.VideoRecorder
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.SignVideoPlayer
import ca.uwaterloo.helloasl.ui.utils.cameraNoHardwareMessage

@Composable
fun TranslateView(
    vm: TranslateViewModel,
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    requestCameraPermission: () -> Unit,
) {
    val state = vm.state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val leftSelected = state.mode == TranslateMode.EN_TO_ASL
        val rightSelected = state.mode == TranslateMode.ASL_TO_EN

        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (leftSelected) 0f else 180f,
                    animationSpec = tween(durationMillis = 300),
                    label = "swapRotation"
                )

                DirectionLabel(
                    text = "English -> ASL",
                    selected = leftSelected,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val next =
                            if (leftSelected) TranslateMode.ASL_TO_EN else TranslateMode.EN_TO_ASL
                        vm.onSwitchMode(next)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Switch translation direction",
                        modifier = Modifier.rotate(rotation)
                    )
                }

                DirectionLabel(
                    text = "ASL -> English",
                    selected = rightSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        when (state.mode) {
            TranslateMode.EN_TO_ASL -> EnToAslUI(
                state = state,
                onSearch = vm::onSearch,
                onSelectHistoryItem = vm::onSelectHistoryItem,
                onQueryChange = vm::onQueryChange
            )

            TranslateMode.ASL_TO_EN -> AslToEnUI(
                state = state,
                hasCameraHardware = hasCameraHardware,
                cameraGranted = cameraGranted,
                onRequestCameraPermission = requestCameraPermission,
                onStartPreview = vm::onStartPreview,
                onStopPreview = vm::onStopPreview,
                onStartRecording = vm::onStartRecording,
                onStopRecording = vm::onStopRecording,
                onInterpretRecording = vm::onInterpretRecording,
                onClearRecording = vm::onClearRecording,
                onRecordingSaved = vm::onRecordingSaved,
                onRecordingError = vm::onRecordingError
            )
        }
    }
}

@Composable
private fun DirectionLabel(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 6.dp),
        shape = RoundedCornerShape(60.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun EnToAslUI(
    state: TranslateUiState,
    onSearch: () -> Unit,
    onSelectHistoryItem: (String) -> Unit,
    onQueryChange: (String) -> Unit
) {
    Spacer(Modifier.height(40.dp))

    Column(Modifier.fillMaxWidth()) {
        TextField(
            value = state.query,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .align(Alignment.CenterHorizontally),
            placeholder = { Text(state.queryHint) },
            onValueChange = onQueryChange,
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                state.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                }

                val result = state.lastResult
                val videoUrl = result?.videoUrl1

                if (!videoUrl.isNullOrBlank()) {
                    key(videoUrl) {
                        SignVideoPlayer(
                            resourcePath = videoUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 300.dp, max = 420.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Search History", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(12.dp))

                if (state.searchHistory.isEmpty()) {
                    Text(
                        "No search history yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.searchHistory.take(5).forEach { item ->
                        ClickableSection(onClick = { onSelectHistoryItem(item.query) }) {
                            Text(item.query)
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AslToEnUI(
    state: TranslateUiState,
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    onRequestCameraPermission: () -> Unit,
    onStartPreview: () -> Unit,
    onStopPreview: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onInterpretRecording: () -> Unit,
    onClearRecording: () -> Unit,
    onRecordingSaved: (String) -> Unit,
    onRecordingError: (String) -> Unit,
) {
    var showPreview by remember { mutableStateOf(false) }
    val platform = getPlatform()
    val osName = System.getProperty("os.name")

    Spacer(Modifier.height(16.dp))

    if (!hasCameraHardware) {
        Text(
            text = cameraNoHardwareMessage(platform, osName),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
    } else if (!cameraGranted) {
        Text(
            "Grant camera permission to use ASL -> English translation.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
    } else {
        Spacer(Modifier.height(24.dp))
    }

    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Translate ASL to English",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            val cameraReady = hasCameraHardware && cameraGranted

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                if (cameraReady && state.isPreviewActive) {
                    VideoRecorder(
                        modifier = Modifier.fillMaxSize(),
                        isPreviewActive = state.isPreviewActive,
                        isRecording = state.isRecording,
                        onVideoSaved = onRecordingSaved,
                        onError = onRecordingError
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = "Camera",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = when {
                                    !hasCameraHardware -> "Camera unavailable"
                                    !cameraGranted -> "Camera permission needed"
                                    else -> "Start the camera to record a sign"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            when {
                                !cameraGranted -> onRequestCameraPermission()
                                state.isPreviewActive -> onStopPreview()
                                else -> onStartPreview()
                            }
                        },
                        enabled = hasCameraHardware && !state.isRecording,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = when {
                                !hasCameraHardware -> "No Camera"
                                !cameraGranted -> "Grant Permission"
                                state.isPreviewActive -> "Stop Camera"
                                else -> "Start Camera"
                            },
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            if (state.isRecording) onStopRecording() else onStartRecording()
                        },
                        enabled = hasCameraHardware &&
                                cameraGranted &&
                                state.isPreviewActive,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (state.isRecording) "Stop Recording" else "Record Video",
                            maxLines = 1
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPreview = true },
                        enabled = !state.recordedVideoUri.isNullOrBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Preview", maxLines = 1)
                    }

                    Button(
                        onClick = onInterpretRecording,
                        enabled = !state.recordedVideoUri.isNullOrBlank() && !state.isRecognizing,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (state.isRecognizing) "Interpreting..." else "Interpret",
                            maxLines = 1
                        )
                    }

                    TextButton(
                        onClick = onClearRecording,
                        enabled = !state.recordedVideoUri.isNullOrBlank() && !state.isRecording,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("Clear", maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Text(
                    text = when {
                        state.isRecording -> "Recording in progress..."
                        !state.recordedVideoUri.isNullOrBlank() -> "Video recorded and ready."
                        state.isPreviewActive -> "Camera preview is active."
                        else -> "Camera is idle."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }

            state.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showPreview) {
        val uri = state.recordedVideoUri
        if (!uri.isNullOrBlank()) {
            Dialog(onDismissRequest = { showPreview = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(500.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recorded Preview",
                                style = MaterialTheme.typography.titleMedium
                            )

                            TextButton(onClick = { showPreview = false }) {
                                Text("Close")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            SignVideoPlayer(
                                resourcePath = uri,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        } else {
            showPreview = false
        }
    }

    Spacer(Modifier.height(16.dp))

    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Recognized Text", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(
                text = if (state.recoText.isBlank()) "No result yet." else state.recoText,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Recognition Confidence", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(state.confidenceLabel, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { state.confidence.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}