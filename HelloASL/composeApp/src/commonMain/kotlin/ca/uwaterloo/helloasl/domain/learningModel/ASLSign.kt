package ca.uwaterloo.helloasl.domain.learningModel

import kotlinx.serialization.Serializable

@Serializable
data class ASLSign(
    val signId: Int,
    val lessonId: Int? = null,
    val gloss: String,
    val videoUrl1: String,
    val videoUrl2: String? = null
)
