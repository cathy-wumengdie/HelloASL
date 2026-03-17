package ca.uwaterloo.helloasl.domain.learningModel

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val lessonId: Long,
    val moduleId: Long,
    val title: String
)