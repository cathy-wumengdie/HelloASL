package ca.uwaterloo.helloasl.ui.utils

import ca.uwaterloo.helloasl.Platform

private fun isMacOs(osName: String?): Boolean =
    osName?.lowercase()?.contains("mac") == true

fun cameraNoHardwareMessage(platform: Platform, osName: String?): String =
    when {
        platform.isAndroid -> "This device has no camera. ASL -> English is unavailable."
        platform.isDesktop && !isMacOs(osName) -> "Currently only support camera function on macOS"
        else -> "This device has no camera. ASL -> English is unavailable."
    }

fun cameraUnavailableMessage(platform: Platform, osName: String?): String =
    if (platform.isDesktop && !isMacOs(osName)) {
        "Currently only support camera function on macOS"
    } else {
        "Camera unavailable on this desktop build."
    }

