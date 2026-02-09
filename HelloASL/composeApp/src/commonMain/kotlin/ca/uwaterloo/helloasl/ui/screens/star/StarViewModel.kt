package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class StarViewModel {

    private val _state = mutableStateOf(
        StarState(
            items = listOf(
                StarItem(id = "cat", label = "Cat"),
                StarItem(id = "dog", label = "Dog"),
                StarItem(id = "fish", label = "Fish")
            )
        )
    )

    val state: State<StarState> = _state

    fun onItemClick(item: StarItem) {
        // later: navigate to sign detail screen
    }

    fun onRemoveStar(item: StarItem) {
        _state.value = _state.value.copy(
            items = _state.value.items.filterNot { it.id == item.id }
        )
    }

    fun refresh() {
        // later: reload starred items
    }
}
