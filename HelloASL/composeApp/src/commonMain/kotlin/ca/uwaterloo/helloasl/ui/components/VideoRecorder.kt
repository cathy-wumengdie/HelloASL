package ca.uwaterloo.helloasl.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoRecorder(
    modifier: Modifier = Modifier,
    isPreviewActive: Boolean,
    isRecording: Boolean,
    onVideoSaved: (String) -> Unit,
    onError: (String) -> Unit
)
