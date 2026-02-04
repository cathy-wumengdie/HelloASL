package ca.uwaterloo.helloasl.ui.screens.profile

data class ProfileState(
    val userName: String = "Yanjin",
    val avatarText: String = "YJ",
    val totalLessonsInModule: Int = 10,
    val lessonsCompleted: Int = 3,
    val lessonProgress: String = "Lesson $lessonsCompleted of $totalLessonsInModule",
    val progress: Float = lessonsCompleted.toFloat() / totalLessonsInModule,
    val streakDays: Int = 7,
    val goalsDone: Int = 2,
    val goalsTotal: Int = 3,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)