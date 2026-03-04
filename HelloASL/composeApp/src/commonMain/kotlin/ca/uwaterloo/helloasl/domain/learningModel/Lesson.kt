package ca.uwaterloo.helloasl.domain.learningModel

import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val id: Int,
    val title: String,
    val signIds: List<Int>,
    val level: Int = 1,
    val category: String = "",
    val locked: Boolean = false,
)