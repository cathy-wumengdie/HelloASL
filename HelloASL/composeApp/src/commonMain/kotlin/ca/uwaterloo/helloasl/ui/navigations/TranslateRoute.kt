package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateView
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TranslateRoute(
    vm: TranslateViewModel
) {
    TranslateView(vm = vm)
}

//enum class TranslateDestination {
//    HISTORY,
//    SETTINGS
//}
//
//data class TranslateNavEvent(val dest: TranslateDestination)
//
//@Composable
//fun TranslateRoute(
//    vm: TranslateViewModel,
//    onHistory: () -> Unit,
//    onSettings: () -> Unit
//) {
//    LaunchedEffect(vm) {
//        vm.navEvents.collectLatest { event ->
//            when (event.dest) {
//                TranslateDestination.HISTORY -> onHistory()
//                TranslateDestination.SETTINGS -> onSettings()
//            }
//        }
//    }
//
//    TranslateView(vm = vm)
//}