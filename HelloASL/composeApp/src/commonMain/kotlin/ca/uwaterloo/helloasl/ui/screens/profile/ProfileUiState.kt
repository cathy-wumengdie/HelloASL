package ca.uwaterloo.helloasl.ui.screens.profile

data class ProfileUiState(
    val userName: String,
    val avatarText: String,
    val wordsLearned: Int,
    val starredSigns: Int,
    val learningGoalPerDay: Int,
    val learningGoalPerWeek: Int,
    val email: String
)