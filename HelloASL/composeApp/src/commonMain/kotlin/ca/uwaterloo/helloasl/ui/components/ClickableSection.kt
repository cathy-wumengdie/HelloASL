package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun ClickableSection(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role = Role.Button,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    padding: PaddingValues = PaddingValues(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clip(shape)
            .clickable(
                enabled = enabled,
                role = role,
                interactionSource = interactionSource,
                onClick = onClick
            )
            .then(Modifier) // keeps chaining clean
            .padding(padding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}