package ca.uwaterloo.helloasl.data.translateRepository

data class TranslateResponse(
    val status: String,
    val gloss: String?,
    val confidence: Double?,
    val message: String?
)