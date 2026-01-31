package ca.uwaterloo.helloasl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform