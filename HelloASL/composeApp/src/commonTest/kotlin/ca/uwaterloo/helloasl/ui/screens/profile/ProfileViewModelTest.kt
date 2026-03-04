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
import ca.uwaterloo.helloasl.ui.navigations.ProfileDestination
import ca.uwaterloo.helloasl.ui.navigations.ProfileNavEvent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileViewModelTest {

    private val TODAY = LocalDate(2026, 3, 4)

    private class FakeAuthRepository : AuthRepository {
        override fun signup(name: String, email: String, password: String) = true
        override fun login(email: String, password: String) = true
        override fun logout() {}
    }

    private fun ps(
        userId: Int,
        dailyGoalMinutes: Int,
        weeklyGoalDays: Int,
        minutesLearned: Int = 0,
        weeklyDaysCompleted: Int = 0,
        dayStreak: Int = 0,
        date: LocalDate = TODAY,
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

    private class FakeUserRepository(
        private var user: User,
        private var profile: UserProfile
    ) : UserRepository {

        var lastUpdateGoals: Pair<Int, Int>? = null
            private set

        fun setUser(newUser: User) { user = newUser }
        fun setProfile(newProfile: UserProfile) { profile = newProfile }

        override fun getUser(): User = user
        override fun getUserProfile(): UserProfile = profile

        override fun updateLearningProgress(): Boolean = true

        override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            lastUpdateGoals = minutesPerDay to daysPerWeek

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

    private fun makeVmWith(
        user: User = User(id = 1, name = "Yanjin Xia", email = "yanjin@gmail.com"),
        profile: UserProfile = UserProfile(
            userId = 1,
            progressSummary = ps(
                userId = 1,
                dailyGoalMinutes = 15,
                weeklyGoalDays = 3,
                dayStreak = 7
            ),
            learningProgress = LearningProgress(module = 1, lesson = 2),
            wordsLearned = 40,
            starredSigns = 12
        )
    ): Triple<ProfileViewModel, FakeUserRepository, Model> {
        val db = MockDB()
        db.login("yanjin@gmail.com", "1234")
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
        val vm = ProfileViewModel(model)
        return Triple(vm, userRepo, model)
    }

    @Test
    fun init_buildsStateFromModel() {
        val (vm, _, _) = makeVmWith(
            user = User(id = 1, name = "Alice Bob", email = "a@b.com"),
            profile = UserProfile(
                userId = 1,
                progressSummary = ps(
                    userId = 1,
                    dailyGoalMinutes = 20,
                    weeklyGoalDays = 4,
                    dayStreak = 10
                ),
                learningProgress = LearningProgress(0, 0),
                wordsLearned = 99,
                starredSigns = 5
            )
        )

        assertEquals("Alice Bob", vm.state.userName)
        assertEquals("AB", vm.state.avatarText)
        assertEquals(99, vm.state.wordsLearned)
        assertEquals(5, vm.state.starredSigns)
    }

    @Test
    fun refresh_rebuildsStateFromLatestRepoData() {
        val (vm, userRepo, _) = makeVmWith()

        userRepo.setUser(User(id = 1, name = "New Name", email = "n@uw.ca"))
        userRepo.setProfile(
            UserProfile(
                userId = 1,
                progressSummary = ps(
                    userId = 1,
                    dailyGoalMinutes = 30,
                    weeklyGoalDays = 6,
                    dayStreak = 1
                ),
                learningProgress = LearningProgress(2, 3),
                wordsLearned = 123,
                starredSigns = 77
            )
        )

        vm.refresh()

        assertEquals("New Name", vm.state.userName)
        assertEquals("NN", vm.state.avatarText)
        assertEquals(123, vm.state.wordsLearned)
        assertEquals(77, vm.state.starredSigns)
    }

    @Test
    fun onSaveLearningGoals_callsModelAndRefreshesState() {
        val (vm, userRepo, _) = makeVmWith()
        assertNull(userRepo.lastUpdateGoals)

        vm.onSaveLearningGoals(minutesPerDay = 25, daysPerWeek = 5)

        assertEquals(25 to 5, userRepo.lastUpdateGoals)
    }

    private suspend fun awaitOneNavEvent(vm: ProfileViewModel): ProfileNavEvent =
        withTimeout(500) { vm.navEvents.first() }

    @Test
    fun onSettings_emitsSettingsDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onSettings()
        assertEquals(ProfileDestination.SETTINGS, wait.await().dest)
    }

    @Test
    fun onWordsLearned_emitsWordsLearnedDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onWordsLearned()
        assertEquals(ProfileDestination.WORDS_LEARNED, wait.await().dest)
    }

    @Test
    fun onStarredSigns_emitsStarredSignsDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onStarredSigns()
        assertEquals(ProfileDestination.STARRED_SIGNS, wait.await().dest)
    }

    @Test
    fun onAccount_emitsAccountDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onAccount()
        assertEquals(ProfileDestination.ACCOUNT, wait.await().dest)
    }

    @Test
    fun onLicense_emitsLicenseDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onLicense()
        assertEquals(ProfileDestination.LICENSE, wait.await().dest)
    }

    @Test
    fun onSignOut_emitsSignInDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onSignOut()
        assertEquals(ProfileDestination.SIGN_IN, wait.await().dest)
    }
}