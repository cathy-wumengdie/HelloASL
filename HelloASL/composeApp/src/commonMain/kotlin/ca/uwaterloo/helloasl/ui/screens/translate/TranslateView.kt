package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.CameraPreview
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.SignVideoPlayer

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
                onStartCamera = vm::onStartCamera,
                onStopCamera = vm::onStopCamera
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
                    SignVideoPlayer(
                        resourcePath = videoUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
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
    onStartCamera: () -> Unit,
    onStopCamera: () -> Unit,
) {
    Spacer(Modifier.height(16.dp))

    if (!hasCameraHardware) {
        Text(
            "This device has no camera. ASL -> English is unavailable.",
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
        Spacer(Modifier.height(40.dp))
    }

    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Translate ASL to English", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            val cameraReady = hasCameraHardware && cameraGranted

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                if (cameraReady && state.isCameraRunning) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Camera",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    when {
                        !cameraGranted -> onRequestCameraPermission()
                        state.isCameraRunning -> onStopCamera()
                        else -> onStartCamera()
                    }
                },
                enabled = hasCameraHardware,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    when {
                        !hasCameraHardware -> "Camera Not Available"
                        !cameraGranted -> "Grant Camera Permission"
                        state.isCameraRunning -> "Stop Camera"
                        else -> "Start Camera"
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Recognized Text", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(state.recoText, style = MaterialTheme.typography.titleLarge)
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