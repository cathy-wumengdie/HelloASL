package ca.uwaterloo.helloasl.ui.screens.home

import ca.uwaterloo.helloasl.data.repository.AuthRepository
import ca.uwaterloo.helloasl.data.repository.UserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import ca.uwaterloo.helloasl.ui.navigations.HomeDestination
import ca.uwaterloo.helloasl.ui.navigations.HomeNavEvent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
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
    ): Triple<HomeViewModel, FakeUserRepository, Model> {
        val userRepo = FakeUserRepository(user, profile)
        val model = Model(
            Repositories(
                auth = FakeAuthRepository(),
                user = userRepo
            )
        )
        val vm = HomeViewModel(model)
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
                learningProgress = LearningProgress(module = 2, lesson = 1),
                wordsLearned = 99,
                starredSigns = 5
            )
        )

        assertEquals("Alice Bob", vm.state.userName)
        assertEquals("Module 2: Greetings", vm.state.moduleTitle)
        assertEquals(3, vm.state.totalLessonsInModule)
        assertEquals(1, vm.state.lessonsCompleted)
        assertEquals(10, vm.state.streakDays)
        assertEquals(2, vm.state.dailyGoalsDone)
        assertEquals(20, vm.state.dailyGoalsTotal)
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
                learningProgress = LearningProgress(module = 3, lesson = 2),
                wordsLearned = 123,
                starredSigns = 77
            )
        )

        vm.refresh()

        assertEquals("New Name", vm.state.userName)
        assertEquals("Module 3: Greetings", vm.state.moduleTitle)
        assertEquals(3, vm.state.totalLessonsInModule)
        assertEquals(2, vm.state.lessonsCompleted)
        assertEquals(1, vm.state.streakDays)
        assertEquals(2, vm.state.dailyGoalsDone)
        assertEquals(30, vm.state.dailyGoalsTotal)
    }

    @Test
    fun state_computedProperties_matchExpected() {
        val (vm, _, _) = makeVmWith(
            profile = UserProfile(
                userId = 1,
                learningGoalPerDay = 15,
                learningGoalPerWeek = 3,
                streakDays = 7,
                learningProgress = LearningProgress(module = 1, lesson = 2),
                wordsLearned = 40,
                starredSigns = 12
            )
        )

        assertEquals("Lesson 2 of 3", vm.state.lessonProgress)
        assertEquals(2f / 3f, vm.state.progress)
    }

    private suspend fun awaitOneNavEvent(vm: HomeViewModel): HomeNavEvent =
        withTimeout(500) { vm.navEvents.first() }

    @Test
    fun onDayStreak_emitsDayStreakDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onDayStreak()
        val event = wait.await()
        assertEquals(HomeDestination.DAY_STREAK, event.dest)
    }

    @Test
    fun onDailyGoals_emitsDailyGoalsDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onDailyGoals()
        val event = wait.await()
        assertEquals(HomeDestination.DAILY_GOALS, event.dest)
    }

    @Test
    fun onLearning_emitsLearningDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onLearning()
        val event = wait.await()
        assertEquals(HomeDestination.LEARNING, event.dest)
    }

    @Test
    fun onTakeQuiz_emitsQuizDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onTakeQuiz()
        val event = wait.await()
        assertEquals(HomeDestination.QUIZ, event.dest)
    }

    @Test
    fun onTranslate_emitsTranslateDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onTranslate()
        val event = wait.await()
        assertEquals(HomeDestination.TRANSLATE, event.dest)
    }

    @Test
    fun onNotifications_emitsNotificationsDestination() = runBlocking {
        val (vm, _, _) = makeVmWith()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onNotifications()
        val event = wait.await()
        assertEquals(HomeDestination.NOTIFICATIONS, event.dest)
    }
}