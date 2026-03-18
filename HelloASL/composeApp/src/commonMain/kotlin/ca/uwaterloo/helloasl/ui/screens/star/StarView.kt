package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.ui.components.HelloASLCard
import ca.uwaterloo.helloasl.ui.components.SignVideoPlayer

@Composable
fun StarView(vm: StarViewModel) {
    val state = vm.state

    LaunchedEffect(Unit) {
        vm.refresh()
    }

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
                onClick = { vm.onItemClick(item) },
                onRemove = { vm.onRemoveStar(item) }
            )
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
                SignVideoPlayer(
                    resourcePath = item.videoUrl,
                    modifier = Modifier.size(64.dp)
                )
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

                Text(
                    text = "Tap to review",
                    style = MaterialTheme.typography.bodySmall
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

            TextButton(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}