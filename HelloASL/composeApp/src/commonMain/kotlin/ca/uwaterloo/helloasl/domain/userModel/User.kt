package ca.uwaterloo.helloasl.domain.userModel

data class User (
    val id: String,
    val name: String,
    val email: String,
) {
    val avatarText: String
        get() = name.toAvatarText()

    fun String.toAvatarText(): String {
        val parts = trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }

        if (parts.isEmpty()) return ""

        val first = parts.first().first().uppercaseChar()

        val last = if (parts.size > 1)
            parts.last().first().uppercaseChar()
        else
            null

        return if (last != null) "$first$last" else "$first"
    }
}