package ca.uwaterloo.helloasl.ui.screens.home

data class HomeModel(
    val userName: String = "Yanjin",
    val moduleTitle: String = "Module 1: Greetings",
    val totalLessonsInModule: Int = 10,
    val lessonsCompleted: Int = 3,
    val streakDays: Int = 7,
    val goalsDone: Int = 2,
    val goalsTotal: Int = 3,
) {
    val lessonProgress: String
        get() = "Lesson $lessonsCompleted of $totalLessonsInModule"

    val progress: Float
        get() = if (totalLessonsInModule == 0) 0f
        else lessonsCompleted.toFloat() / totalLessonsInModule
}