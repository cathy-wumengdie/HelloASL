package ca.uwaterloo.helloasl.ui.screens.star
import ca.uwaterloo.helloasl.domain.starModel.StarItem

data class StarUiState(
    val items: List<StarItem> = emptyList()
)