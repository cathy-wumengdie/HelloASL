package ca.uwaterloo.helloasl.domain.learningModel

data class Module(
    val moduleId: Int,
    val title: String,
    val category: String? = null
)