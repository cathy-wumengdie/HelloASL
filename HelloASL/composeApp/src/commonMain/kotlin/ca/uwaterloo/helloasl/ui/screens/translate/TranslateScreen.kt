package ca.uwaterloo.helloasl.ui.screens.translate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.ClickableSection

/*
Translation tab:
    - Decide which UI to show (EN->ASL or ASL->EN) based on the translation mode
    - A Card (always visible) at the top with 2 buttons, used for toggling the translation mode
*/
@Composable
fun TranslateScreen(
    // Parameters
    state: TranslateState,
    onSwitchMode: (TranslateMode) -> Unit,
    // Does nothing, for now..
    onSearch: () -> Unit,
    onSelectHistoryItem: () -> Unit,
    onStartCamera: () -> Unit
) {  // General layout of the Translate tab
    Column (
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)  // Same padding as Home + Profile
    ) {
        // Boolean to check which mode we are in
        val leftSelected = state.mode == TranslateMode.EN_TO_ASL
        val rightSelected = state.mode == TranslateMode.ASL_TO_EN

        /* ---------- Card (for alternating translation mode) ---------- */
        // Comment: maybe change the Card (one color for each Tab) later if needed
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (leftSelected) 0f else 180f,
                    animationSpec = tween(durationMillis = 300),
                    label = "swapRotation"
                )

                DirectionLabel(
                    text = "English → ASL",
                    selected = leftSelected,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val next = if (leftSelected) TranslateMode.ASL_TO_EN else TranslateMode.EN_TO_ASL
                        onSwitchMode(next)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Switch translation direction",
                        modifier = Modifier.rotate(rotation)
                    )
                }

                DirectionLabel(
                    text = "ASL → English",
                    selected = rightSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // Display different UI based on mode
        when (state.mode) {
            TranslateMode.EN_TO_ASL -> EnToAslUI(
                state = state,
                onSearch = onSearch,
                onSelectHistoryItem = onSelectHistoryItem
            )
            TranslateMode.ASL_TO_EN -> AslToEnUI(
                state = state,
                onStartCamera = onStartCamera
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
        color = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
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
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EnToAslUI (
    state: TranslateState,
    onSearch: () -> Unit,
    onSelectHistoryItem: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {

        /* ---------- Text field (for searching English words) ---------- */
        TextField(
            value = "",
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.fillMaxWidth(0.95f).align(Alignment.CenterHorizontally),
            placeholder = {Text(state.queryHint)},
            onValueChange = {},  // Comment: fill in later..
            trailingIcon = {
                IconButton(onClick = onSearch){
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
            }
        )

        Spacer(Modifier.height(16.dp))  // Space between UIs

        /* ---------- Card (to show search result) ---------- */
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Search Results for \"${state.query}\"", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                // Placeholder boxes (for images/videos)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        tonalElevation = 1.dp
                    ) {}
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp),
                        tonalElevation = 1.dp
                    ) {}
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ---------- Card (search history) ---------- */
        HelloASLCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Search History", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                // Show the list of words that the user had searched
                state.searchHistory.forEach { word ->
                    ClickableSection(
                        onClick = {} // worry about this later ;)
                    ) {Text(word)}
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

    }
}

@Composable
private fun AslToEnUI (
    state: TranslateState,
    onStartCamera: () -> Unit
) {
    /* ---------- Card (video recording) ---------- */
    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Translate ASL to English", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "Camera",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onStartCamera,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Start Camera") }
        }
    }

    Spacer(Modifier.height(16.dp))

    /* ---------- Card (displaying recoginized text from recording) ---------- */
    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Recognized Text", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(state.recoText, style = MaterialTheme.typography.titleLarge)
        }
    }

    Spacer(Modifier.height(16.dp))

    /* ---------- Card (displaying confidence score) ---------- */
    HelloASLCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Recognition Confidence", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text(state.confidenceLabel, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { state.confidence },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}