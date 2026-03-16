package ca.uwaterloo.helloasl.domain.learningModel

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val lessonId: Int,
    val moduleId: Int,
    val title: String
)