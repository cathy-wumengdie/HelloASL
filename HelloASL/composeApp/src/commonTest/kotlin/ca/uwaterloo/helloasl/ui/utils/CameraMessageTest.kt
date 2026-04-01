package ca.uwaterloo.helloasl.ui.utils

import ca.uwaterloo.helloasl.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

private data class TestPlatform(
    override val name: String = "test",
    override val isAndroid: Boolean,
    override val isDesktop: Boolean
) : Platform

class CameraMessageTest {

    @Test
    fun android_no_camera_message_uses_default_text() {
        val platform = TestPlatform(isAndroid = true, isDesktop = false)
        val message = cameraNoHardwareMessage(platform, "Android")
        assertEquals("This device has no camera. ASL -> English is unavailable.", message)
    }

    @Test
    fun mac_desktop_no_camera_message_uses_default_text() {
        val platform = TestPlatform(isAndroid = false, isDesktop = true)
        val message = cameraNoHardwareMessage(platform, "Mac OS X")
        assertEquals("This device has no camera. ASL -> English is unavailable.", message)
    }

    @Test
    fun windows_desktop_no_camera_message_uses_macos_only_text() {
        val platform = TestPlatform(isAndroid = false, isDesktop = true)
        val message = cameraNoHardwareMessage(platform, "Windows 11")
        assertEquals("Currently only support camera function on macOS", message)
    }

    @Test
    fun desktop_camera_unavailable_uses_macos_only_for_non_mac() {
        val platform = TestPlatform(isAndroid = false, isDesktop = true)
        val message = cameraUnavailableMessage(platform, "Windows 10")
        assertEquals("Currently only support camera function on macOS", message)
    }

    @Test
    fun desktop_camera_unavailable_uses_default_on_mac() {
        val platform = TestPlatform(isAndroid = false, isDesktop = true)
        val message = cameraUnavailableMessage(platform, "Mac OS X")
        assertEquals("Camera unavailable on this desktop build.", message)
    }
}

