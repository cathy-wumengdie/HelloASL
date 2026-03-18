package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.ui.navigations.StarDestination
import ca.uwaterloo.helloasl.ui.navigations.StarNavEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class StarViewModel(private val model: Model) {

    var state by mutableStateOf(StarUiState())
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        refresh()
    }

    fun refresh() {
        scope.launch {
            val allItems = model.getStarredItems()
            val tags = allItems.map { it.tagName }.distinct()

            state = state.copy(
                allItems = allItems,
                items = applyFilter(allItems, state.selectedTag),
                tags = tags
            )
        }
    }

    private fun applyFilter(items: List<StarItem>, tag: String?): List<StarItem> {
        return if (tag == null) items else items.filter { it.tagName == tag }
    }

    fun onTagSelected(tag: String?) {
        state = state.copy(
            selectedTag = tag,
            items = applyFilter(state.allItems, tag)
        )
    }

    private val _navEvents = MutableSharedFlow<StarNavEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val navEvents: SharedFlow<StarNavEvent> = _navEvents.asSharedFlow()

    fun onBack() {
        _navEvents.tryEmit(StarNavEvent(StarDestination.BACK))
    }

    fun onItemClick(item: StarItem) {
        _navEvents.tryEmit(
            StarNavEvent(
                StarDestination.DETAIL,
                itemId = item.signId.toString()
            )
        )
    }

    fun onRemoveStar(item: StarItem) {
        scope.launch {
            val userId = model.getCurrentUserId() ?: return@launch
            model.removeStar(userId, item.signId)

            val newAllItems = state.allItems.filter { it.signId != item.signId }
            val newTags = newAllItems.map { it.tagName }.distinct()
            val newSelectedTag =
                if (state.selectedTag != null && newTags.contains(state.selectedTag)) {
                    state.selectedTag
                } else {
                    null
                }

            state = state.copy(
                allItems = newAllItems,
                items = applyFilter(newAllItems, newSelectedTag),
                tags = newTags,
                selectedTag = newSelectedTag
            )
        }
    }
}