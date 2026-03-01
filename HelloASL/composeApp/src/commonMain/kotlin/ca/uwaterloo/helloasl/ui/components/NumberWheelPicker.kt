package ca.uwaterloo.helloasl.ui.components

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun NumberWheelPicker(
    value: Int,
    range: IntRange,
    modifier: Modifier = Modifier,
    visibleCount: Int = 3,
    itemHeight: Dp = 44.dp,
    onValueChange: (Int) -> Unit
) {
    require(visibleCount % 2 == 1) { "visibleCount must be odd" }

    val values = remember(range) { range.toList() }
    val centerOffsetCount = visibleCount / 2

    val initialIndex = (values.indexOf(value).takeIf { it >= 0 } ?: 0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialIndex - centerOffsetCount).coerceAtLeast(0)
    )

    val scope = rememberCoroutineScope()

    fun currentCenteredIndex(): Int? {
        val layoutInfo = listState.layoutInfo
        if (layoutInfo.visibleItemsInfo.isEmpty()) return null

        val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

        val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
            val itemCenter = item.offset + item.size / 2
            abs(itemCenter - viewportCenter)
        }

        return closest?.index
    }

    // Update selected value while scrolling (based on REAL centered item)
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val idx = currentCenteredIndex() ?: return@LaunchedEffect
        val v = values.getOrNull(idx) ?: return@LaunchedEffect
        onValueChange(v)
    }

    // Snap to the centered item when scroll ends
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            } ?: return@LaunchedEffect

            val scrollDelta = (closest.offset + closest.size / 2) - viewportCenter

            scope.launch {
                // This aligns the chosen item’s center exactly to the viewport center
                listState.animateScrollBy(scrollDelta.toFloat())
            }
        }
    }

    Box(modifier = modifier.height(itemHeight * visibleCount)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * centerOffsetCount)
        ) {
            itemsIndexed(values) { index, v ->
                val isCenter = (currentCenteredIndex() == index)

                Text(
                    text = v.toString(),
                    style = if (isCenter) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        )
    }
}