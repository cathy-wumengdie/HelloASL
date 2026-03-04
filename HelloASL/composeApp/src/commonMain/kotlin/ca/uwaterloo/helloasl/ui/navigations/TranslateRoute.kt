package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateView
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.Text
import androidx.compose.material3.Button

@Composable
fun TranslateRoute(
    vm: TranslateViewModel,
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    requestCameraPermission: () -> Unit
) {
    if (!hasCameraHardware) {
        Text("Camera not available")
        return
    }

    if (!cameraGranted) {
        Button(onClick = requestCameraPermission) {
            Text("Grant camera permission")
        }
        return
    }

    TranslateView(
        vm = vm,
        hasCameraHardware = hasCameraHardware,
        cameraGranted = cameraGranted,
        requestCameraPermission = requestCameraPermission
    )
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