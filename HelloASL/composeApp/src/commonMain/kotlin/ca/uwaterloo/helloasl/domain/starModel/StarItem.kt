package ca.uwaterloo.helloasl.domain.starModel

data class StarItem(
    val signId: Long,
    val label: String,
    val videoUrl: String?,
    val tagName: String
)