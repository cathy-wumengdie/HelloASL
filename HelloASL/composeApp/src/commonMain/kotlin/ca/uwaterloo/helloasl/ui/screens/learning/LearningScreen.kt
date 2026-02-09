package ca.uwaterloo.helloasl.ui.screens.learning

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ca.uwaterloo.helloasl.ui.components.ClickableSection
import ca.uwaterloo.helloasl.ui.components.HelloASLCard

@Composable
fun LearningScreen(
    state: LearningState,
    onOpenLesson: (title: String) -> Unit,
    onOpenStarred: () -> Unit,
) {
    // 最小可用：统一浅青
    val pageBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)
    val cardBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    val innerBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.40f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Starred / Signs
        HelloASLCard(cardColor = cardBg, elevationDp = 0.dp) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StarredPill(
                    value = state.starredCount,
                    onClick = onOpenStarred,
                    bg = innerBg,
                    modifier = Modifier.weight(3f)
                )
                SignsPill(
                    value = state.signsCount,
                    bg = innerBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Alphabet
        HelloASLCard(cardColor = cardBg, elevationDp = 0.dp) {
            Text("Alphabet", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AlphabetCenterItem(
                    label = "A–G",
                    leftIcon = { Icon(Icons.Filled.Check, contentDescription = null) },
                    enabled = true,
                    bg = innerBg,
                    onClick = { onOpenLesson("Alphabet: A–G") }
                )

                AlphabetCenterItem(
                    label = "H–P",
                    leftIcon = { Text(state.alphabetScore.toString(), style = MaterialTheme.typography.labelMedium) },
                    enabled = state.alphabetHPUnlocked,
                    bg = innerBg,
                    onClick = { onOpenLesson("Alphabet: H–P") }
                )

                AlphabetCenterItem(
                    label = "Q–Z",
                    leftIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    enabled = !state.alphabetQZLocked,
                    bg = innerBg,
                    onClick = { onOpenLesson("Alphabet: Q–Z") }
                )
            }
        }

        // Greetings
        HelloASLCard(cardColor = cardBg, elevationDp = 0.dp) {
            Text("Greetings", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            AlphabetRow(
                label = "Hello",
                left = { },
                enabled = !state.greetingsHelloLocked,
                bg = innerBg,
                right = { Icon(Icons.Filled.Lock, contentDescription = null) },
                onClick = { onOpenLesson("Greetings: Hello") }
            )
        }
    }
}

@Composable
private fun HelloPill(
    title: String,
    subtitle: String,
    value: String,
    bg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClickableSection(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        padding = PaddingValues(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
@Composable
private fun StarredPill(
    value: Int,
    onClick: () -> Unit,
    bg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    ClickableSection(
        onClick = onClick,
        modifier = modifier
            .clip(shape)
            .background(bg),
        shape = shape,
        padding = PaddingValues(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Starred", style = MaterialTheme.typography.titleSmall)
                Text("Review saved signs", style = MaterialTheme.typography.bodySmall)
            }
//            Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        }
    }
}
@Composable
private fun SignsPill(
    value: Int,
    bg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .padding(12.dp)
    ) {
        // 你图里是右对齐数字+“Signs”
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(value.toString(), style = MaterialTheme.typography.titleMedium)
//            Text("Signs", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun AlphabetRow(
    label: String,
    left: @Composable () -> Unit,
    enabled: Boolean,
    bg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    right: @Composable (() -> Unit)? = null
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { left() }

        ClickableSection(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            padding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label)
                if (right != null) right()
            }
        }
    }
}

@Composable
private fun AlphabetCenterItem(
    label: String,
    leftIcon: @Composable () -> Unit,
    enabled: Boolean,
    bg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            leftIcon()
        }


        ClickableSection(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .width(140.dp)
                .clip(shape)
                .background(bg),
            shape = shape,
            padding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label)
        }
    }
}

