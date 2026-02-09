package ca.uwaterloo.helloasl.ui.screens.star

data class StarItem(
    val id: String,
    val label: String,
    val imageRes: String? = null
)

data class StarState(
    val items: List<StarItem> = emptyList()
)
