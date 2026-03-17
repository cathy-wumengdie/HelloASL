package ca.uwaterloo.helloasl.data

actual object AppLogger {
    actual fun d(tag: String, message: String) {
        println("[$tag] $message")
    }
}

