package ca.uwaterloo.helloasl.ui.screens.home

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

class HomeViewTest {

    private val TODAY = LocalDate(2026, 3, 4)

    private class FakeAuthRepository : AuthRepository {
        override fun signup(name: String, email: String, password: String) = true
        override fun login(email: String, password: String) = true
        override fun logout() {}
    }

    private fun ps(
        userId: Int,
        minutesLearned: Int,
        dailyGoalMinutes: Int,
        dayStreak: Int,
        weeklyDaysCompleted: Int = 0,
        weeklyGoalDays: Int = 0,
        lastDailyGoalCompletedDate: LocalDate? = null,
        lastCreditedDate: LocalDate? = null,
        date: LocalDate = TODAY
    ): ProgressSummary {
        return ProgressSummary(
            userId = userId,
            date = date,
            dailyProgress = DailyProgress(
                minutesLearned = minutesLearned,
                lastDailyGoalCompletedDate = lastDailyGoalCompletedDate,
                dailyGoalMinutes = dailyGoalMinutes
            ),
            weeklyProgress = WeeklyProgress(
                daysCompleted = weeklyDaysCompleted,
                lastCreditedDate = lastCreditedDate,
                weeklyGoalDays = weeklyGoalDays
            ),
            dayStreak = dayStreak
        )
    }

    private class FakeUserRepository(
        private var user: User,
        private var profile: UserProfile
    ) : UserRepository {

        fun setUser(newUser: User) { user = newUser }
        fun setProfile(newProfile: UserProfile) { profile = newProfile }

        override fun getUser(): User = user
        override fun getUserProfile(): UserProfile = profile

        override fun updateLearningProgress(): Boolean {
            // not needed for these tests
            return true
        }

        override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            // New model: goals live inside profile.progressSummary
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

        override fun getStarredItems() = emptyList<ca.uwaterloo.helloasl.domain.starModel.StarItem>()
        override fun removeStar(itemId: String) {}
    }

    private fun makeVmWith(
        user: User = User(id = 1, name = "Tracy Hua", email = "t@uw.ca"),
        profile: UserProfile = UserProfile(
            userId = 1,
            progressSummary = ps(
                userId = 1,
                minutesLearned = 20,
                dailyGoalMinutes = 15,
                dayStreak = 7,
                weeklyDaysCompleted = 3,
                weeklyGoalDays = 3
            ),
            learningProgress = LearningProgress(module = 1, lesson = 2),
            wordsLearned = 40,
            starredSigns = 12
        )
    ): Pair<HomeViewModel, FakeUserRepository> {
        val db = MockDB()
        val userRepo = FakeUserRepository(user, profile)
        val model = Model(
            Repositories(
                auth = FakeAuthRepository(),
                user = userRepo,
                learning = MockLearningRepository(db),
                translate = MockTranslateRepository(db),
                progressTracker = MockProgressTrackerRepository(db)
            )
        )
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
                progressSummary = ps(
                    userId = 1,
                    minutesLearned = 5,        // dailyGoalsDone likely changes
                    dailyGoalMinutes = 30,     // dailyGoalsTotal changes
                    dayStreak = 1,             // streakDays changes
                    weeklyDaysCompleted = 1,
                    weeklyGoalDays = 3
                ),
                // moduleTitle / lessonProgress should change
                learningProgress = LearningProgress(module = 1, lesson = 1),
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        vm.refresh()

        val afterLessonProgress = lessonProgressText(vm.state)
        val afterStreak = streakText(vm.state)
        val afterGoals = dailyGoalsText(vm.state)

        assertNotEquals(beforeLessonProgress, afterLessonProgress)
        assertNotEquals(beforeStreak, afterStreak)
        assertNotEquals(beforeGoals, afterGoals)
    }

    @Test
    fun displayed_texts_change_when_goals_total_changes() {
        val (vm, repo) = makeVmWith(
            profile = UserProfile(
                userId = 1,
                progressSummary = ps(
                    userId = 1,
                    minutesLearned = 2,
                    dailyGoalMinutes = 10,
                    dayStreak = 7,
                    weeklyDaysCompleted = 2,
                    weeklyGoalDays = 3
                ),
                learningProgress = LearningProgress(module = 1, lesson = 2),
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        val beforeGoals = dailyGoalsText(vm.state)
        repo.setProfile(
            UserProfile(
                userId = 1,
                progressSummary = ps(
                    userId = 1,
                    minutesLearned = 2,
                    dailyGoalMinutes = 50,
                    dayStreak = 7,
                    weeklyDaysCompleted = 2,
                    weeklyGoalDays = 3
                ),
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