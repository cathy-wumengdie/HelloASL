package ca.uwaterloo.helloasl.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun SignVideoPlayer(resourcePath: String, modifier: Modifier = Modifier)

