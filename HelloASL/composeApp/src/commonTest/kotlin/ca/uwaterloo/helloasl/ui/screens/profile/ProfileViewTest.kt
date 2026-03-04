package ca.uwaterloo.helloasl.ui.screens.profile

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.AuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.UserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertNotEquals

class ProfileViewTest {

    private val TODAY = LocalDate(2026, 3, 4)

    private class FakeAuthRepository : AuthRepository {
        override fun signup(name: String, email: String, password: String) = true
        override fun login(email: String, password: String) = true
        override fun logout() {}
    }

    private fun ps(
        userId: Int,
        minutesPerDay: Int,
        daysPerWeek: Int
    ): ProgressSummary {
        return ProgressSummary(
            userId = userId,
            date = TODAY,
            dailyProgress = DailyProgress(
                minutesLearned = 0,
                lastDailyGoalCompletedDate = null,
                dailyGoalMinutes = minutesPerDay
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = 0,
                lastCreditedDate = null,
                weeklyGoalDays = daysPerWeek
            ),
            dayStreak = 7
        )
    }

    private class FakeUserRepository(
        private var user: User,
        private var profile: UserProfile
    ) : UserRepository {

        override fun getUser(): User = user
        override fun getUserProfile(): UserProfile = profile

        override fun updateLearningProgress(): Boolean = true

        override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            val old = profile.progressSummary
            val updated = old.copy(
                dailyProgress = old.dailyProgress.copy(dailyGoalMinutes = minutesPerDay),
                weeklyProgress = old.weeklyProgress.copy(weeklyGoalDays = daysPerWeek)
            )
            profile = profile.copy(progressSummary = updated)
        }

        override fun updateWordsLearned(wordsLearned: Int) {
            profile = profile.copy(wordsLearned = wordsLearned)
        }

        override fun getStarredItems() =
            emptyList<ca.uwaterloo.helloasl.domain.starModel.StarItem>()

        override fun removeStar(itemId: String) {}
    }

    private fun makeVm(
        minutesPerDay: Int = 15,
        daysPerWeek: Int = 3,
        userName: String = "Yanjin Xia"
    ): ProfileViewModel {
        val db = MockDB()
        val userRepo = FakeUserRepository(
            user = User(id = 1, name = userName, email = "yanjin@gmail.com"),
            profile = UserProfile(
                userId = 1,
                progressSummary = ps(userId = 1, minutesPerDay = minutesPerDay, daysPerWeek = daysPerWeek),
                learningProgress = LearningProgress(module = 1, lesson = 2),
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        val model = Model(
            Repositories(
                auth = FakeAuthRepository(),
                user = userRepo,
                learning = MockLearningRepository(db),
                translate = MockTranslateRepository(db),
                progressTracker = MockProgressTrackerRepository(db)
            )
        )
        return ProfileViewModel(model)
    }

    private fun learningGoalTextPerDay(state: ProfileUiState): String =
        "Learn ${state.learningGoalPerDay} minutes per day"

    private fun learningGoalTextPerWeek(state: ProfileUiState): String =
        "Learn ${state.learningGoalPerWeek} days per week"

    @Test
    fun learningGoalTexts_change_when_vm_state_changes() {
        val vm = makeVm(minutesPerDay = 15, daysPerWeek = 3)
        val beforeDay = learningGoalTextPerDay(vm.state)
        val beforeWeek = learningGoalTextPerWeek(vm.state)

        vm.onSaveLearningGoals(minutesPerDay = 25, daysPerWeek = 5)

        val afterDay = learningGoalTextPerDay(vm.state)
        val afterWeek = learningGoalTextPerWeek(vm.state)

        assertNotEquals(beforeDay, afterDay)
        assertNotEquals(beforeWeek, afterWeek)
    }

    @Test
    fun headerText_changes_when_userName_changes() {
        val vm = makeVm(userName = "Yanjin Xia")
        val beforeName = vm.state.userName
        val beforeAvatar = vm.state.avatarText

        val vmDifferent = makeVm(userName = "New Name")

        assertNotEquals(beforeName, vmDifferent.state.userName)
        assertNotEquals(beforeAvatar, vmDifferent.state.avatarText)
    }
}