package ca.uwaterloo.helloasl.domain.translateModel

data class AslRecognitionResult(
    val recognizedText: String,
    val confidence: Float
)