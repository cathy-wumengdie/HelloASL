package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.getPlatform
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.SignVideoPlayer
import ca.uwaterloo.helloasl.getPlatform

@Composable
fun StarView(vm: StarViewModel) {
    val state = vm.state
    var selectedItem by remember { mutableStateOf<StarItem?>(null) }
    val showInlineVideo = !platform.isDesktop
    val platform = remember { getPlatform() }

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedTag == null,
                            onClick = { vm.onTagSelected(null) },
                            label = { Text("All") }
                        )
                    }

                    items(state.tags) { tag ->
                        FilterChip(
                            selected = state.selectedTag == tag,
                            onClick = { vm.onTagSelected(tag) },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            items(state.items) { item ->
                StarItemCard(
                    item = item,
                    showInlineVideo = showInlineVideo,
                    onClick = {
                        selectedItem = item
                    },
                    onRemove = { vm.onRemoveStar(item) }
                )
            }
        }

        selectedItem?.let { item ->
            val popupModifier =
                if (platform.isDesktop) {
                    Modifier
                        .fillMaxWidth(0.42f)
                        .widthIn(min = 420.dp, max = 560.dp)
                } else {
                    Modifier
                        .fillMaxWidth(0.92f)
                }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { selectedItem = null }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 0.dp,
                    shadowElevation = 6.dp,
                    modifier = popupModifier
                        .heightIn(max = maxHeight * 0.9f)
                        .clickable(
                            enabled = false,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item.videoUrl?.let { videoUrl ->
                            key(videoUrl) {
                                SignVideoPlayer(
                                    resourcePath = videoUrl,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 220.dp, max = 420.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.tagName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0D47A1)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { selectedItem = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D47A1),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarItemCard(
    item: StarItem,
    showInlineVideo: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    HelloASLCard(
        cardColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clickable { onClick() },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "▶",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = item.tagName,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onRemove,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF0D47A1)
                )
            ) {
                Text("Remove")
            }
        }
    }
}
