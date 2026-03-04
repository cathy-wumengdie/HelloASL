package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.starModel.StarItem
import ca.uwaterloo.helloasl.ui.navigations.StarDestination
import ca.uwaterloo.helloasl.ui.navigations.StarNavEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class StarViewModel(private val model: Model) {

    var state by mutableStateOf(buildState())
        private set

    private fun buildState(): StarUiState {
        return StarUiState(items = model.getStarredItems())
    }

    fun refresh() {
        state = buildState()
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
        _navEvents.tryEmit(StarNavEvent(StarDestination.DETAIL, itemId = item.id))
    }

    fun onRemoveStar(item: StarItem) {
        model.removeStar(item.id)
        refresh()
    }
}