package ca.uwaterloo.helloasl.ui.screens.home

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.AuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
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
    private class FakeAuthRepository(private val db: MockDB) : AuthRepository {
        override fun signup(name: String, email: String, password: String): Boolean =
            db.signup(name, email, password)

        override fun login(email: String, password: String): Boolean =
            db.login(email, password)

        override fun logout() = db.logout()
    }

    private fun makeVmLoggedInAsUser1(): Triple<HomeViewModel, MockDB, Model> {
        val db = MockDB()

        val ok = db.login(email = "yanjin@gmail.com", password = "1234")
        check(ok) { "Test setup failed: could not login as user 1" }

        val repos = Repositories(
            auth = FakeAuthRepository(db),
            user = MockUserRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )
        val model = Model(repos)
        val vm = HomeViewModel(model)
        return Triple(vm, db, model)
    }

    @Test
    fun init_buildsStateFromModel() {
        val (vm, _, _) = makeVmLoggedInAsUser1()

        assertEquals("Yanjin", vm.state.userName)
        assertEquals("Unit 1: Basics", vm.state.moduleTitle)
        assertEquals(2, vm.state.totalLessonsInModule)
        assertEquals(0, vm.state.lessonsCompleted)

        assertEquals(7, vm.state.streakDays)
        assertEquals(20, vm.state.dailyGoalsDone)
        assertEquals(15, vm.state.dailyGoalsTotal)
    }

    @Test
    fun refresh_rebuildsStateFromLatestRepoData_afterDbMutation() {
        val (vm, db, _) = makeVmLoggedInAsUser1()
        db.updateLearningGoals(minutesPerDay = 30, daysPerWeek = 6)

        db.addLearningMinutes(5)
        db.updateLearningProgress()
        vm.refresh()

        assertEquals("Yanjin", vm.state.userName)
        assertEquals("Unit 1: Basics", vm.state.moduleTitle)
        assertEquals(2, vm.state.totalLessonsInModule)
        assertEquals(1, vm.state.lessonsCompleted)
        assertEquals(30, vm.state.dailyGoalsTotal)
        assertEquals(25, vm.state.dailyGoalsDone)
    }

    @Test
    fun state_computedProperties_matchExpected() {
        val (vm, _, _) = makeVmLoggedInAsUser1()

        assertEquals("Lesson 0 of 2", vm.state.lessonProgress)
        assertEquals(0f / 2f, vm.state.progress)
    }

    private suspend fun awaitOneNavEvent(vm: HomeViewModel): HomeNavEvent =
        withTimeout(500) { vm.navEvents.first() }

    @Test
    fun onDayStreak_emitsDayStreakDestination() = runBlocking {
        val (vm, _, _) = makeVmLoggedInAsUser1()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onDayStreak()
        assertEquals(HomeDestination.DAY_STREAK, wait.await().dest)
    }

    @Test
    fun onDailyGoals_emitsDailyGoalsDestination() = runBlocking {
        val (vm, _, _) = makeVmLoggedInAsUser1()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onDailyGoals()
        assertEquals(HomeDestination.DAILY_GOALS, wait.await().dest)
    }

    @Test
    fun onLearning_emitsLearningDestination() = runBlocking {
        val (vm, _, _) = makeVmLoggedInAsUser1()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onLearning()
        assertEquals(HomeDestination.LEARNING, wait.await().dest)
    }

    @Test
    fun onTakeQuiz_emitsQuizDestination() = runBlocking {
        val (vm, _, _) = makeVmLoggedInAsUser1()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onTakeQuiz()
        assertEquals(HomeDestination.QUIZ, wait.await().dest)
    }

    @Test
    fun onTranslate_emitsTranslateDestination() = runBlocking {
        val (vm, _, _) = makeVmLoggedInAsUser1()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onTranslate()
        assertEquals(HomeDestination.TRANSLATE, wait.await().dest)
    }

    @Test
    fun onNotifications_emitsNotificationsDestination() = runBlocking {
        val (vm, _, _) = makeVmLoggedInAsUser1()
        val wait = async(start = CoroutineStart.UNDISPATCHED) { awaitOneNavEvent(vm) }
        vm.onNotifications()
        assertEquals(HomeDestination.NOTIFICATIONS, wait.await().dest)
    }
}