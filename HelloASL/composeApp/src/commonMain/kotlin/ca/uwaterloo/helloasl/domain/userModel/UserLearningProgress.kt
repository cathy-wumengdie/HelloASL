package ca.uwaterloo.helloasl.domain.userModel

data class UserLearningProgress(
    val userId: Int,
    val moduleId: Int,
    val lessonId: Int,
    val wordsLearned: Int,
    val starredSigns: Int,
)