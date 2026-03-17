package ca.uwaterloo.helloasl.data

import android.util.Log

actual object AppLogger {
    actual fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
}

