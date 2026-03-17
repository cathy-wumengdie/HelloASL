package ca.uwaterloo.helloasl.domain.learningModel

data class Module(
    val moduleId: Long,
    val title: String,
    val category: String? = null
)