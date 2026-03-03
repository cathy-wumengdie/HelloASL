package ca.uwaterloo.helloasl.domain.learning

import kotlinx.serialization.Serializable

@Serializable
data class ASLSign(
    val id: Int,
    val word: String,
    val description: String = "",
    val videoUrls: List<String> = emptyList(),
    val tags: Set<String> = emptySet()
)

//todo: 加一些function
