package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.uwaterloo.helloasl.ui.screens.star.StarView
import ca.uwaterloo.helloasl.ui.screens.star.StarViewModel
import kotlinx.coroutines.flow.collectLatest

enum class StarDestination {
    BACK,
    DETAIL
}

data class StarNavEvent(
    val dest: StarDestination,
    val itemId: String? = null
)

@Composable
fun StarRoute(
    vm: StarViewModel,
    onBack: () -> Unit,
    onDetail: (itemId: String) -> Unit
) {
    LaunchedEffect(vm) {
        vm.navEvents.collectLatest { event ->
            when (event.dest) {
                StarDestination.BACK -> onBack()
                StarDestination.DETAIL -> onDetail(event.itemId ?: return@collectLatest)
            }
        }
    }

    StarView(vm = vm)
}