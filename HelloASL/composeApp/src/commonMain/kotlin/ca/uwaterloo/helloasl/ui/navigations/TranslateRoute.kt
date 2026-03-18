package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateView
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateViewModel

@Composable
fun TranslateRoute(
    vm: TranslateViewModel,
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    requestCameraPermission: () -> Unit
) {
    TranslateView(
        vm = vm,
        hasCameraHardware = hasCameraHardware,
        cameraGranted = cameraGranted,
        requestCameraPermission = requestCameraPermission
    )
}