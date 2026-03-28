package ca.uwaterloo.helloasl

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ca.uwaterloo.helloasl.data.SupabaseAppDependency
import ca.uwaterloo.helloasl.data.SupabaseClientFactory
import ca.uwaterloo.helloasl.ui.components.DesktopCameraController

fun main() = application {
    val supabaseUrl = System.getProperty("SUPABASE_URL")
        ?: error("Missing SUPABASE_URL")

    val supabaseAnonKey = System.getProperty("SUPABASE_ANON_KEY")
        ?: error("Missing SUPABASE_ANON_KEY")

    val supabaseDependency = createSupabaseDependencyOrNull(
        supabaseUrl,
        supabaseAnonKey
    ) ?: error("Failed to create Supabase dependency")

    Window(
        onCloseRequest = ::exitApplication,
        title = "helloasl",
    ) {
        val hasSeenPermissionGate = remember { mutableStateOf(false) }
        val desktopCameraController = remember { DesktopCameraController() }

        App(
            hasCameraHardware = desktopCameraController.hasCameraHardware,
            cameraGranted = desktopCameraController.cameraGranted,
            cameraErrorMessage = desktopCameraController.cameraErrorMessage,
            notificationGranted = false,
            requestCameraPermission = {
                desktopCameraController.requestCameraPermission()
            },
            requestNotificationPermission = {},
            hasSeenPermissionGate = hasSeenPermissionGate.value,
            onPermissionGateCompleted = {
                hasSeenPermissionGate.value = true
            },
            supabaseDependency = supabaseDependency,
            onLoginSuccessSyncDeviceToken = {}
        )
    }
}

private fun createSupabaseDependencyOrNull(
    url: String,
    anonKey: String
): SupabaseAppDependency? {
    if (url.isBlank() || anonKey.isBlank()) return null
    val client = SupabaseClientFactory.create(url, anonKey)
    return SupabaseAppDependency(client, anonKey)
}