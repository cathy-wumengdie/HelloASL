package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun VideoRecorder(
    modifier: Modifier,
    isPreviewActive: Boolean,
    isRecording: Boolean,
    onVideoSaved: (String) -> Unit,
    onError: (String) -> Unit
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(
            "Video Recorder supported on Android only",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}