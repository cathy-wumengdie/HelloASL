package ca.uwaterloo.helloasl.ui.screens.star

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

data class StarUiState(
    val items: List<StarItem> = emptyList()
)

class StarViewModel {

    private val _uiState = mutableStateOf(
        StarUiState(
            items = listOf(
                StarItem(id = "cat", label = "Cat"),
                StarItem(id = "dog", label = "Dog"),
                StarItem(id = "fish", label = "Fish")
            )
        )
    )

    val uiState: State<StarUiState> = _uiState

    fun onItemClick(item: StarItem) {
        // later: navigate to detail screen
    }

    fun onRemoveStar(item: StarItem) {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.filterNot { it.id == item.id }
        )
    }

    fun refresh() {
        // later: reload data
    }
}
