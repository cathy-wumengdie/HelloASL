package ca.uwaterloo.helloasl.ui.screens.home

import ca.uwaterloo.helloasl.data.repository.AuthRepository
import ca.uwaterloo.helloasl.data.repository.UserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import kotlin.test.Test
import kotlin.test.assertNotEquals

class HomeViewTest {

    private class FakeAuthRepository : AuthRepository {
        override fun signup(name: String, email: String, password: String) = true
        override fun login(email: String, password: String) = true
        override fun logout() {}
    }

    private class FakeUserRepository(
        private var user: User,
        private var profile: UserProfile
    ) : UserRepository {

        fun setUser(newUser: User) { user = newUser }
        fun setProfile(newProfile: UserProfile) { profile = newProfile }

        override fun getUser(): User = user
        override fun getUserProfile(): UserProfile = profile

        override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            // HomeViewModel buildState 会读 learningGoalPerDay，所以这里模拟更新
            profile = profile.copy(
                learningGoalPerDay = minutesPerDay,
                learningGoalPerWeek = daysPerWeek
            )
        }
    }

    private fun makeVmWith(
        user: User = User(id = 1, name = "Tracy Hua", email = "t@uw.ca"),
        profile: UserProfile = UserProfile(
            userId = 1,
            learningGoalPerDay = 15,
            learningGoalPerWeek = 3,
            streakDays = 7,
            learningProgress = LearningProgress(module = 1, lesson = 2),
            wordsLearned = 40,
            starredSigns = 12
        )
    ): Pair<HomeViewModel, FakeUserRepository> {
        val userRepo = FakeUserRepository(user, profile)
        val model = Model(Repositories(auth = FakeAuthRepository(), user = userRepo))
        return HomeViewModel(model) to userRepo
    }

    private fun streakText(state: HomeUiState): String = "${state.streakDays} Day Streak"
    private fun dailyGoalsText(state: HomeUiState): String =
        "${state.dailyGoalsDone} / ${state.dailyGoalsTotal} Daily Goals"

    private fun moduleTitleText(state: HomeUiState): String = state.moduleTitle
    private fun lessonProgressText(state: HomeUiState): String = state.lessonProgress

    @Test
    fun displayed_texts_change_after_refresh_when_repo_data_changes() {
        val (vm, repo) = makeVmWith()
        val beforeModuleTitle = moduleTitleText(vm.state)
        val beforeLessonProgress = lessonProgressText(vm.state)
        val beforeStreak = streakText(vm.state)
        val beforeGoals = dailyGoalsText(vm.state)

        repo.setProfile(
            UserProfile(
                userId = 1,
                learningGoalPerDay = 30,         // dailyGoalsTotal 会变
                learningGoalPerWeek = 3,
                streakDays = 1,                  // streakDays 会变
                learningProgress = LearningProgress(module = 3, lesson = 1), // moduleTitle/lessonProgress 会变
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        vm.refresh()

        val afterModuleTitle = moduleTitleText(vm.state)
        val afterLessonProgress = lessonProgressText(vm.state)
        val afterStreak = streakText(vm.state)
        val afterGoals = dailyGoalsText(vm.state)

        assertNotEquals(beforeModuleTitle, afterModuleTitle)
        assertNotEquals(beforeLessonProgress, afterLessonProgress)
        assertNotEquals(beforeStreak, afterStreak)
        assertNotEquals(beforeGoals, afterGoals)
    }

    @Test
    fun displayed_texts_change_when_goals_total_changes() {
        val (vm, repo) = makeVmWith(
            profile = UserProfile(
                userId = 1,
                learningGoalPerDay = 10,
                learningGoalPerWeek = 3,
                streakDays = 7,
                learningProgress = LearningProgress(module = 1, lesson = 2),
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        val beforeGoals = dailyGoalsText(vm.state)

        repo.setProfile(
            UserProfile(
                userId = 1,
                learningGoalPerDay = 50,
                learningGoalPerWeek = 3,
                streakDays = 7,
                learningProgress = LearningProgress(module = 1, lesson = 2),
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        vm.refresh()

        val afterGoals = dailyGoalsText(vm.state)
        assertNotEquals(beforeGoals, afterGoals)
    }
}