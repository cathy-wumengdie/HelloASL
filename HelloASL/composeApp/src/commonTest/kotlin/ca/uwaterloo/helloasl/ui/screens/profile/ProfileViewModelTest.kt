package ca.uwaterloo.helloasl.ui.screens.profile

import ca.uwaterloo.helloasl.MainDispatcherRule
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.ProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.UserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserLearningProgress
import ca.uwaterloo.helloasl.ui.navigations.ProfileDestination
import ca.uwaterloo.helloasl.ui.navigations.ProfileNavEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate(2026, 3, 4)

    private fun progressSummary(
        userId: String,
        dailyGoalMinutes: Int,
        weeklyGoalDays: Int,
        minutesLearned: Int = 0,
        weeklyDaysCompleted: Int = 0,
        dayStreak: Int = 0,
        date: LocalDate = today,
        lastDailyGoalCompletedDate: LocalDate? = null,
        lastCreditedDate: LocalDate? = null
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

    private class FakeProgressTrackerRepository(
        private var progressSummary: ProgressSummary
    ) : ProgressTrackerRepository {

        fun setProgressSummary(newProgressSummary: ProgressSummary) {
            progressSummary = newProgressSummary
        }

        override suspend fun getProgressSummary(): ProgressSummary = progressSummary

        override suspend fun addLearningMinutes(minutes: Int): ProgressSummary {
            progressSummary = progressSummary.copy(
                dailyProgress = progressSummary.dailyProgress.copy(
                    minutesLearned = progressSummary.dailyProgress.minutesLearned + minutes
                )
            )
            return progressSummary
        }

        override suspend fun reevaluateProgressAfterGoalChange(
            dailyGoalMinutes: Int,
            weeklyGoalDays: Int
        ): ProgressSummary {
            progressSummary = progressSummary.copy(
                dailyProgress = progressSummary.dailyProgress.copy(
                    dailyGoalMinutes = dailyGoalMinutes
                ),
                weeklyProgress = progressSummary.weeklyProgress.copy(
                    weeklyGoalDays = weeklyGoalDays
                )
            )
            return progressSummary
        }
    }

    private class FakeUserRepository(
        private var user: User,
        private var learningProgress: UserLearningProgress
    ) : UserRepository {

        var lastUpdateGoals: Pair<Int, Int>? = null
            private set

        fun setUser(newUser: User) {
            user = newUser
        }

        fun setLearningProgress(newLearningProgress: UserLearningProgress) {
            learningProgress = newLearningProgress
        }

        override suspend fun getUser(): User = user

        override suspend fun getUserLearningProgress(): UserLearningProgress = learningProgress

        override suspend fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            lastUpdateGoals = minutesPerDay to daysPerWeek
        }

        override suspend fun completeLesson(lessonId: Long): Boolean = true

        override suspend fun updateLearningProgress(): Boolean = true

        override suspend fun getCompletedLessonIds(): Set<Long> = emptySet()
    }

    private data class Fixture(
        val vm: ProfileViewModel,
        val userRepo: FakeUserRepository,
        val progressRepo: FakeProgressTrackerRepository,
        val model: Model
    )

    private suspend fun makeVmWith(
        scope: CoroutineScope,
        user: User = User(id = "1", name = "Yanjin Xia", email = "yanjin@gmail.com"),
        learningProgress: UserLearningProgress = UserLearningProgress(
            userId = "1",
            moduleId = 1L,
            lessonId = 2L,
            completedAllLessons = false,
            wordsLearned = 40
        ),
        progressSummary: ProgressSummary = progressSummary(
            userId = "1",
            dailyGoalMinutes = 15,
            weeklyGoalDays = 3,
            dayStreak = 7
        )
    ): Fixture {
        val db = MockDB()
        val progressRepo = FakeProgressTrackerRepository(progressSummary)
        val userRepo = FakeUserRepository(user, learningProgress)

        val model = Model(
            Repositories(
                auth = MockAuthRepository(db),
                user = userRepo,
                star = MockStarRepository(db),
                learning = MockLearningRepository(db),
                translate = MockTranslateRepository(db),
                progressTracker = progressRepo
            )
        )

        model.login("yanjin@gmail.com", "1234")

        val vm = ProfileViewModel(model, scope)
        return Fixture(vm, userRepo, progressRepo, model)
    }

    private suspend fun awaitOneNavEvent(vm: ProfileViewModel): ProfileNavEvent {
        return withTimeout(500) { vm.navEvents.first() }
    }

    @Test
    fun init_buildsStateFromModel() = runTest {
        val fixture = makeVmWith(
            scope = this,
            user = User(id = "1", name = "Alice Bob", email = "a@b.com"),
            learningProgress = UserLearningProgress(
                userId = "1",
                moduleId = 1L,
                lessonId = 1L,
                completedAllLessons = false,
                wordsLearned = 99
            ),
            progressSummary = progressSummary(
                userId = "1",
                dailyGoalMinutes = 20,
                weeklyGoalDays = 4,
                dayStreak = 10
            )
        )

        advanceUntilIdle()

        assertEquals("Alice Bob", fixture.vm.state.userName)
        assertEquals("AB", fixture.vm.state.avatarText)
        assertEquals(99, fixture.vm.state.wordsLearned)
        assertEquals(0, fixture.vm.state.starredSigns)
        assertEquals(20, fixture.vm.state.learningGoalPerDay)
        assertEquals(4, fixture.vm.state.learningGoalPerWeek)
    }

    @Test
    fun refresh_rebuildsStateFromLatestRepoData() = runTest {
        val fixture = makeVmWith(this)
        advanceUntilIdle()

        fixture.userRepo.setUser(User(id = "1", name = "New Name", email = "n@uw.ca"))
        fixture.userRepo.setLearningProgress(
            UserLearningProgress(
                userId = "1",
                moduleId = 2L,
                lessonId = 3L,
                completedAllLessons = false,
                wordsLearned = 123
            )
        )
        fixture.progressRepo.setProgressSummary(
            progressSummary(
                userId = "1",
                dailyGoalMinutes = 30,
                weeklyGoalDays = 6,
                dayStreak = 1
            )
        )

        fixture.vm.refresh()
        advanceUntilIdle()

        assertEquals("New Name", fixture.vm.state.userName)
        assertEquals("NN", fixture.vm.state.avatarText)
        assertEquals(123, fixture.vm.state.wordsLearned)
        assertEquals(0, fixture.vm.state.starredSigns)
        assertEquals(30, fixture.vm.state.learningGoalPerDay)
        assertEquals(6, fixture.vm.state.learningGoalPerWeek)
    }

    @Test
    fun onSaveLearningGoals_callsModelAndRefreshesState() = runTest {
        val fixture = makeVmWith(this)
        advanceUntilIdle()

        assertNull(fixture.userRepo.lastUpdateGoals)

        fixture.vm.onSaveLearningGoals(minutesPerDay = 25, daysPerWeek = 5)
        advanceUntilIdle()

        assertEquals(25 to 5, fixture.userRepo.lastUpdateGoals)
        assertEquals(25, fixture.vm.state.learningGoalPerDay)
        assertEquals(5, fixture.vm.state.learningGoalPerWeek)
    }

    @Test
    fun onSettings_emitsSettingsDestination() = runTest {
        val fixture = makeVmWith(this)
        val wait = async(start = CoroutineStart.UNDISPATCHED) {
            awaitOneNavEvent(fixture.vm)
        }

        fixture.vm.onSettings()

        assertEquals(ProfileDestination.SETTINGS, wait.await().dest)
    }

    @Test
    fun onWordsLearned_emitsWordsLearnedDestination() = runTest {
        val fixture = makeVmWith(this)
        val wait = async(start = CoroutineStart.UNDISPATCHED) {
            awaitOneNavEvent(fixture.vm)
        }

        fixture.vm.onWordsLearned()

        assertEquals(ProfileDestination.WORDS_LEARNED, wait.await().dest)
    }

    @Test
    fun onStarredSigns_emitsStarredSignsDestination() = runTest {
        val fixture = makeVmWith(this)
        val wait = async(start = CoroutineStart.UNDISPATCHED) {
            awaitOneNavEvent(fixture.vm)
        }

        fixture.vm.onStarredSigns()

        assertEquals(ProfileDestination.STARRED_SIGNS, wait.await().dest)
    }

    @Test
    fun onAccount_emitsAccountDestination() = runTest {
        val fixture = makeVmWith(this)
        val wait = async(start = CoroutineStart.UNDISPATCHED) {
            awaitOneNavEvent(fixture.vm)
        }

        fixture.vm.onAccount()

        assertEquals(ProfileDestination.ACCOUNT, wait.await().dest)
    }

    @Test
    fun onLicense_emitsLicenseDestination() = runTest {
        val fixture = makeVmWith(this)
        val wait = async(start = CoroutineStart.UNDISPATCHED) {
            awaitOneNavEvent(fixture.vm)
        }

        fixture.vm.onLicense()

        assertEquals(ProfileDestination.LICENSE, wait.await().dest)
    }

    @Test
    fun onSignOut_emitsSignInDestination() = runTest {
        val fixture = makeVmWith(this)
        val wait = async(start = CoroutineStart.UNDISPATCHED) {
            awaitOneNavEvent(fixture.vm)
        }

        fixture.vm.onSignOut()
        advanceUntilIdle()

        assertEquals(ProfileDestination.SIGN_IN, wait.await().dest)
    }
}