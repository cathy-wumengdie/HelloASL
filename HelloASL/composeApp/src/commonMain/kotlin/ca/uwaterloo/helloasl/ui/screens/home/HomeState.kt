package ca.uwaterloo.helloasl.ui.screens.home

data class HomeState(
    val userName: String = "Yanjin",
    val moduleTitle: String = "Module 1: Greetings",
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