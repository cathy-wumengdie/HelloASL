package ca.uwaterloo.helloasl.domain.learningModel

import kotlinx.serialization.Serializable

@Serializable
data class QuizChoice(
    val choiceId: Long,
    val signId: Long,
    val choiceText: String,
    val isCorrect: Boolean
)

