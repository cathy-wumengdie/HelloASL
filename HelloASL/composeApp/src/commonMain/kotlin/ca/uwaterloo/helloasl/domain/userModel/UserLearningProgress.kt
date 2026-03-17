package ca.uwaterloo.helloasl.domain.userModel

data class UserLearningProgress(
    val userId: String,
    val moduleId: Long?,
    val lessonId: Long?,
    val completedAllLessons: Boolean,
    val wordsLearned: Int
)