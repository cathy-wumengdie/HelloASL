package ca.uwaterloo.helloasl.domain.userModel

import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary

data class LearningProgress(
    val module: Int,
    val lesson: Int
)

data class UserProfile (
    val userId: Int,
    val progressSummary: ProgressSummary,
    val learningProgress: LearningProgress,
    val wordsLearned: Int,
    val starredSigns: Int,
)