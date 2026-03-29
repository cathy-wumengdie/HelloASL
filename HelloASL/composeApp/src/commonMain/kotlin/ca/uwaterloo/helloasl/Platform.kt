package ca.uwaterloo.helloasl

interface Platform {
    val name: String
    val isAndroid: Boolean
    val isDesktop: Boolean
}

expect fun getPlatform(): Platform