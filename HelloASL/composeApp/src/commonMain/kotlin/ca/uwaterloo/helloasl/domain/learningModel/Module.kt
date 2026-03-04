package ca.uwaterloo.helloasl.domain.learningModel

data class Module(
    val id: Int,
    val title: String,
    val lessonIds: List<Int>,
    val category: String,
    val locked: Boolean = false,
)