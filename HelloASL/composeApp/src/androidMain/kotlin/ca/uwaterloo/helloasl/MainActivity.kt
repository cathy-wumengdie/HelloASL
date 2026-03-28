package ca.uwaterloo.helloasl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import ca.uwaterloo.helloasl.data.SupabaseAppDependency
import ca.uwaterloo.helloasl.data.SupabaseClientFactory
import data.HelloAslDataStore
import kotlinx.coroutines.launch
import ca.uwaterloo.helloasl.data.notificationRepository.AndroidTokenSyncer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val supabase = SupabaseClientFactory.create(
            BuildConfig.SUPABASE_URL,
            BuildConfig.SUPABASE_ANON_KEY
        )

        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val supabaseDependency = remember {
                createSupabaseDependencyOrNull(
                    url = BuildConfig.SUPABASE_URL,
                    anonKey = BuildConfig.SUPABASE_ANON_KEY
                )
            }

            // ---- DataStore ----
            val store = remember { HelloAslDataStore(context) }
            val hasSeenPermissionGate by store.hasSeenPermissionGate.collectAsState(initial = false)

            // ---- Hardware check ----
            val hasCameraHardware = remember {
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
            }

            // ---- Current permission states ----
            var cameraGranted by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                )
            }

            var notificationGranted by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
                    else ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED
                )
            }

            var hasAskedCameraOnce by remember { mutableStateOf(false) }
            var hasAskedNotifOnce by remember { mutableStateOf(false) }

            // ---- Re-check permissions when returning from Settings ----
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        cameraGranted =
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_GRANTED
                        notificationGranted =
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
                            else ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        if (cameraGranted) hasAskedCameraOnce = false
                        if (notificationGranted) hasAskedNotifOnce = false
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // ---- Launchers ----
            val cameraLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                cameraGranted = granted
            }

            val notificationLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                notificationGranted = granted
            }

            val settingsLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {}

            // ---- Request camera or open settings ----
            val openAppSettings: () -> Unit = {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", context.packageName, null)
                )
                settingsLauncher.launch(intent)
            }
            val requestCameraOrOpenSettings = {
                if (!cameraGranted) {
                    val shouldShowRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        )

                    if (!hasAskedCameraOnce || shouldShowRationale) {
                        hasAskedCameraOnce = true
                        cameraLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        // Asked before + no rationale => likely "Don't ask again" -> open settings
                        openAppSettings()
                    }
                }
            }

            val requestNotifOrOpenSettings = {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    notificationGranted = true
                } else if (!notificationGranted) {
                    val shouldShowRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        )

                    if (!hasAskedNotifOnce || shouldShowRationale) {
                        hasAskedNotifOnce = true
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openAppSettings()
                    }
                }
            }

            App(
                hasCameraHardware = hasCameraHardware,
                cameraGranted = cameraGranted,
                notificationGranted = notificationGranted,

                requestCameraPermission = {
                    if (hasCameraHardware) {
                        requestCameraOrOpenSettings()
                    }
                },

                requestNotificationPermission = {
                    requestNotifOrOpenSettings()
                },

                hasSeenPermissionGate = hasSeenPermissionGate,
                onPermissionGateCompleted = {
                    scope.launch {
                        store.setHasSeenPermissionGate(true)
                    }
                },
                supabaseDependency = supabaseDependency,
                onLoginSuccessSyncDeviceToken = {
                    try {
                        AndroidTokenSyncer.syncCurrentToken(
                            context = this@MainActivity,
                            supabase = supabase
                        )
                    } catch (e: Exception) {
                        Log.e("HelloASL_FCM", "Failed to sync token after login", e)
                    }
                }
            )
        }
    }
}

private fun createSupabaseDependencyOrNull(url: String, anonKey: String): SupabaseAppDependency? {
    if (url.isBlank() || anonKey.isBlank()) return null
    val client = SupabaseClientFactory.create(url, anonKey)
    return SupabaseAppDependency(client, anonKey)
}
