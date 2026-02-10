package ca.uwaterloo.helloasl.ui.screens.profile

data class ProfileModel(
    val userName: String = "Yanjin",
    val avatarText: String = "Y",
    val wordsLearned: Int = 40,
    val starredSigns: Int = 12,
    val goalsPerDay: Int = 10,
    val goalsPerWeek: Int = 3
)