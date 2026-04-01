package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.SignVideoPlayer

@Composable
fun StarView(vm: StarViewModel) {
    val state = vm.state
    var selectedItem by remember { mutableStateOf<StarItem?>(null) }

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
                    onClick = {
                        selectedItem = item
                    },
                    onRemove = { vm.onRemoveStar(item) }
                )
            }
        }

        if (selectedItem != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                    .clickable { selectedItem = null },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    selectedItem?.videoUrl?.let {
                        SignVideoPlayer(
                            resourcePath = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable(enabled = false) {}
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = selectedItem!!.label,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = selectedItem!!.tagName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0D47A1)
                    )

                    Spacer(Modifier.height(16.dp))

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

@Composable
private fun StarItemCard(
    item: StarItem,
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
            if (item.videoUrl != null) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SignVideoPlayer(
                        resourcePath = item.videoUrl,
                        modifier = Modifier.matchParentSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                onClick()
                            }
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Text(
                    text = item.tagName,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

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

