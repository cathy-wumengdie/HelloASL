package ca.uwaterloo.helloasl.domain.translateModel

data class TranslateResult(
    val query: String,
    val videoUrls: List<String> = emptyList()
)