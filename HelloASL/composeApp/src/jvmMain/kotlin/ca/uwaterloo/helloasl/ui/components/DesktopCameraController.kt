package ca.uwaterloo.helloasl.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.bytedeco.javacv.OpenCVFrameGrabber

class DesktopCameraController {

    var hasCameraHardware: Boolean by mutableStateOf(true)
        private set

    var cameraGranted: Boolean by mutableStateOf(false)
        private set

    var permissionDenied: Boolean by mutableStateOf(false)
        private set

    var cameraErrorMessage: String? by mutableStateOf(null)
        private set

    var selectedCameraIndex: Int? by mutableStateOf(null)
        private set

    init {
        refreshHardwareState()
    }

    private fun refreshHardwareState() {
        val index = findFirstWorkingCameraIndex()
        selectedCameraIndex = index
        hasCameraHardware = index != null
        cameraErrorMessage = if (index == null) "No desktop camera detected" else null
    }

    fun requestCameraPermission() {
        val index = selectedCameraIndex ?: findFirstWorkingCameraIndex()

        if (index == null) {
            hasCameraHardware = false
            cameraGranted = false
            permissionDenied = false
            cameraErrorMessage = "No desktop camera detected"
            return
        }

        val granted = tryOpenCamera(index)

        hasCameraHardware = true
        cameraGranted = granted
        permissionDenied = false
        cameraErrorMessage = if (granted) null else "Failed to open camera on this device"
        if (granted) {
            selectedCameraIndex = index
        }
    }

    private fun findFirstWorkingCameraIndex(maxIndex: Int = 5): Int? {
        for (i in 0..maxIndex) {
            if (tryOpenCamera(i)) return i
        }
        return null
    }

    private fun tryOpenCamera(index: Int): Boolean {
        var grabber: OpenCVFrameGrabber? = null
        return try {
            grabber = OpenCVFrameGrabber(index).apply {
                imageWidth = 640
                imageHeight = 480
                start()
            }
            val frame = grabber.grab()
            frame != null
        } catch (_: Throwable) {
            false
        } finally {
            try {
                grabber?.stop()
            } catch (_: Throwable) {}
            try {
                grabber?.release()
            } catch (_: Throwable) {}
        }
    }
}