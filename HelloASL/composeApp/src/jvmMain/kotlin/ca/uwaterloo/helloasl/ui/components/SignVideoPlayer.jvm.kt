package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun SignVideoPlayer(resourcePath: String, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text("Video playback supported on Android only", style = MaterialTheme.typography.bodyMedium)
    }
}

