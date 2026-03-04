package ca.uwaterloo.helloasl.ui.screens.profile

import ca.uwaterloo.helloasl.data.repository.AuthRepository
import ca.uwaterloo.helloasl.data.repository.UserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import ca.uwaterloo.helloasl.ui.navigations.ProfileDestination
import ca.uwaterloo.helloasl.ui.navigations.ProfileNavEvent
import kotlinx.coroutines.CoroutineStart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first

class ProfileViewModelTest {
    private class FakeAuthRepository : AuthRepository {
        override fun signup(name: String, email: String, password: String) = true
        override fun login(email: String, password: String) = true
        override fun logout() {}
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

        override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            lastUpdateGoals = minutesPerDay to daysPerWeek
            profile = profile.copy(
                learningGoalPerDay = minutesPerDay,
                learningGoalPerWeek = daysPerWeek
            )
        }
    }

    private fun makeVmWith(
        user: User = User(id = 1, name = "Yanjin Xia", email = "yanjin@gmail.com"),
        profile: UserProfile = UserProfile(
            userId = 1,
            learningGoalPerDay = 15,
            learningGoalPerWeek = 3,
            streakDays = 7,
            learningProgress = LearningProgress(module = 1, lesson = 2),
            wordsLearned = 40,
            starredSigns = 12
        )
    ): Triple<ProfileViewModel, FakeUserRepository, Model> {
        val userRepo = FakeUserRepository(user, profile)
        val model = Model(
            Repositories(
                auth = FakeAuthRepository(),
                user = userRepo
            )
        )
        val vm = ProfileViewModel(model)
        return Triple(vm, userRepo, model)
    }

    @Test
    fun init_buildsStateFromModel() {
        // Arrange
        val (vm, _, _) = makeVmWith(
            user = User(id = 1, name = "Alice Bob", email = "a@b.com"),
            profile = UserProfile(
                userId = 1,
                learningGoalPerDay = 20,
                learningGoalPerWeek = 4,
                streakDays = 10,
                learningProgress = LearningProgress(0, 0),
                wordsLearned = 99,
                starredSigns = 5
            )
        )

        assertEquals("Alice Bob", vm.state.userName)
        assertEquals("AB", vm.state.avatarText) // from name.toAvatarText()
        assertEquals(99, vm.state.wordsLearned)
        assertEquals(5, vm.state.starredSigns)
        assertEquals(20, vm.state.learningGoalPerDay)
        assertEquals(4, vm.state.learningGoalPerWeek)
    }

    @Test
    fun refresh_rebuildsStateFromLatestRepoData() {
        val (vm, userRepo, _) = makeVmWith()

        userRepo.setUser(User(id = 1, name = "New Name", email = "n@uw.ca"))
        userRepo.setProfile(
            UserProfile(
                userId = 1,
                learningGoalPerDay = 30,
                learningGoalPerWeek = 6,
                streakDays = 1,
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
        assertEquals(30, vm.state.learningGoalPerDay)
        assertEquals(6, vm.state.learningGoalPerWeek)
    }

    @Test
    fun onSaveLearningGoals_callsModelAndRefreshesState() {
        val (vm, userRepo, _) = makeVmWith()
        assertNull(userRepo.lastUpdateGoals)

        vm.onSaveLearningGoals(minutesPerDay = 25, daysPerWeek = 5)

        assertEquals(25 to 5, userRepo.lastUpdateGoals)
        assertEquals(25, vm.state.learningGoalPerDay)
        assertEquals(5, vm.state.learningGoalPerWeek)
    }

    private suspend fun awaitOneNavEvent(vm: ProfileViewModel): ProfileNavEvent =
        withTimeout(500) { vm.navEvents.first() }

    @Test
    fun onSettings_emitsSettingsDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onSettings()
        val event = wait.await()
        assertEquals(ProfileDestination.SETTINGS, event.dest)
    }

    @Test
    fun onWordsLearned_emitsWordsLearnedDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onWordsLearned()
        val event = wait.await()
        assertEquals(ProfileDestination.WORDS_LEARNED, event.dest)
    }

    @Test
    fun onStarredSigns_emitsStarredSignsDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onStarredSigns()
        val event = wait.await()
        assertEquals(ProfileDestination.STARRED_SIGNS, event.dest)
    }

    @Test
    fun onAccount_emitsAccountDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onAccount()
        val event = wait.await()
        assertEquals(ProfileDestination.ACCOUNT, event.dest)
    }

    @Test
    fun onLicense_emitsLicenseDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onLicense()
        val event = wait.await()
        assertEquals(ProfileDestination.LICENSE, event.dest)
    }

    @Test
    fun onSignOut_emitsSignInDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onSignOut()
        val event = wait.await()
        assertEquals(ProfileDestination.SIGN_IN, event.dest)
    }
}