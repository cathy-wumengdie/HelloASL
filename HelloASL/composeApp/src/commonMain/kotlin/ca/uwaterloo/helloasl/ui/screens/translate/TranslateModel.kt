package ca.uwaterloo.helloasl.ui.screens.translate

enum class TranslateMode { EN_TO_ASL, ASL_TO_EN }

data class TranslateModel(
    val mode: TranslateMode = TranslateMode.EN_TO_ASL,

    // English -> ASL
    val queryHint: String = "Search an English word",
    val query: String = "Cat",
    val searchHistory: List<String> = listOf("Cat", "Dog"),

    // ASL -> English
    val recoText: String = "Cat",  // Recognized Text from the video captured
    val confidenceLabel: String = "80%",
    val confidence: Float = 0.8f
)
