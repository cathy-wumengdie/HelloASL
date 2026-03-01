package ca.uwaterloo.helloasl.ui.screens.home

data class HomeUiState(
    val userName: String,
    val moduleTitle: String,
    val totalLessonsInModule: Int,
    val lessonsCompleted: Int,
    val streakDays: Int,
    val dailyGoalsDone: Int,
    val dailyGoalsTotal: Int,
) {
    val lessonProgress: String
        get() = "Lesson $lessonsCompleted of $totalLessonsInModule"

    val progress: Float
        get() = if (totalLessonsInModule == 0) 0f
        else lessonsCompleted.toFloat() / totalLessonsInModule
}