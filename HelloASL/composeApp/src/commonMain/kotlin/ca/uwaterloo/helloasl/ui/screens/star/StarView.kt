package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarView(
    viewModel: StarViewModel
) {
    val state = viewModel.uiState.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        items(state.items) { item ->
            StarItemCard(label = item.label)
        }

        items(3) {
            HelloASLCard(
                cardColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Spacer(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}


@Composable
private fun StarItemCard(
    label: String
) {
    HelloASLCard(
        cardColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {}

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
