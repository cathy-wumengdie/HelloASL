package ca.uwaterloo.helloasl.ui.screens.home

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.trackingModel.DailyProgress
import ca.uwaterloo.helloasl.domain.trackingModel.ProgressSummary
import ca.uwaterloo.helloasl.domain.trackingModel.TimeUtils.today
import ca.uwaterloo.helloasl.domain.trackingModel.WeeklyProgress
import ca.uwaterloo.helloasl.domain.userModel.UserLearningProgress
import ca.uwaterloo.helloasl.domain.userModel.UserSession
import ca.uwaterloo.helloasl.ui.navigations.HomeDestination
import ca.uwaterloo.helloasl.ui.navigations.HomeNavEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Test
    fun init_buildsStateFromModel() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, _) = makeVmLoggedInAsUser1(this, dispatcher)

        advanceUntilIdle()

        assertEquals("Yanjin", vm.state.userName)
        assertEquals("Module 1: Basics", vm.state.moduleTitle)
        assertEquals(2, vm.state.totalLessonsInModule)
        assertEquals(0, vm.state.lessonsCompleted)
        assertEquals(7, vm.state.streakDays)
        assertEquals(20, vm.state.dailyGoalsDone)
        assertEquals(15, vm.state.dailyGoalsTotal)
        assertEquals(3, vm.state.weeklyGoalsDone)
        assertEquals(3, vm.state.weeklyGoalsTotal)
    }

    @Test
    fun refresh_rebuildsStateFromLatestRepoData_afterDbMutation() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, db) = makeVmLoggedInAsUser1(this, dispatcher)

        advanceUntilIdle()

        db.putProgressSummary(
            "1",
            ProgressSummary(
                userId = "1",
                date = today(),
                dailyProgress = DailyProgress(
                    minutesLearned = 5,
                    lastDailyGoalCompletedDate = null,
                    dailyGoalMinutes = 30
                ),
                weeklyProgress = WeeklyProgress(
                    daysCompleted = 1,
                    lastCreditedDate = null,
                    weeklyGoalDays = 6
                ),
                dayStreak = 2
            )
        )

        db.putUserLearningProgress(
            "1",
            UserLearningProgress(
                userId = "1",
                moduleId = 1L,
                lessonId = 2L,
                completedAllLessons = false,
                wordsLearned = 2
            )
        )

        vm.refresh()
        advanceUntilIdle()

        assertEquals("Yanjin", vm.state.userName)
        assertEquals("Module 1: Basics", vm.state.moduleTitle)
        assertEquals(2, vm.state.totalLessonsInModule)
        assertEquals(1, vm.state.lessonsCompleted)
        assertEquals(2, vm.state.streakDays)
        assertEquals(5, vm.state.dailyGoalsDone)
        assertEquals(30, vm.state.dailyGoalsTotal)
        assertEquals(1, vm.state.weeklyGoalsDone)
        assertEquals(6, vm.state.weeklyGoalsTotal)
    }

    @Test
    fun refresh_setsLessonsCompletedToTotal_whenCurrentLessonIsNull() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, db) = makeVmLoggedInAsUser1(this, dispatcher)

        advanceUntilIdle()

        db.putUserLearningProgress(
            "1",
            UserLearningProgress(
                userId = "1",
                moduleId = 1L,
                lessonId = null,
                completedAllLessons = true,
                wordsLearned = 4
            )
        )

        vm.refresh()
        advanceUntilIdle()

        assertEquals("Module 1: Basics", vm.state.moduleTitle)
        assertEquals(2, vm.state.totalLessonsInModule)
        assertEquals(2, vm.state.lessonsCompleted)
    }

    @Test
    fun refresh_usesLearningFallback_whenModuleIdIsNull() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, db) = makeVmLoggedInAsUser1(this, dispatcher)

        advanceUntilIdle()

        db.putUserLearningProgress(
            "1",
            UserLearningProgress(
                userId = "1",
                moduleId = null,
                lessonId = null,
                completedAllLessons = true,
                wordsLearned = 4
            )
        )

        vm.refresh()
        advanceUntilIdle()

        assertEquals("Learning", vm.state.moduleTitle)
        assertEquals(0, vm.state.totalLessonsInModule)
        assertEquals(0, vm.state.lessonsCompleted)
    }

    @Test
    fun refresh_setsLessonsCompletedToTotal_whenLessonIsNotFoundInModule() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, db) = makeVmLoggedInAsUser1(this, dispatcher)

        advanceUntilIdle()

        db.putUserLearningProgress(
            "1",
            UserLearningProgress(
                userId = "1",
                moduleId = 1L,
                lessonId = 999L,
                completedAllLessons = false,
                wordsLearned = 0
            )
        )

        vm.refresh()
        advanceUntilIdle()

        assertEquals("Module 1: Basics", vm.state.moduleTitle)
        assertEquals(2, vm.state.totalLessonsInModule)
        assertEquals(2, vm.state.lessonsCompleted)
    }

    @Test
    fun onLearning_emitsLearningNavigationEvent() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, _) = makeVmLoggedInAsUser1(this, dispatcher)
        var event: HomeNavEvent? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            event = vm.navEvents.first()
        }

        vm.onLearning()
        advanceUntilIdle()

        assertNotNull(event)
        assertEquals(HomeNavEvent(HomeDestination.LEARNING), event)

        job.cancel()
    }

    @Test
    fun onTranslate_emitsTranslateNavigationEvent() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val (vm, _) = makeVmLoggedInAsUser1(this, dispatcher)
        var event: HomeNavEvent? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            event = vm.navEvents.first()
        }

        vm.onTranslate()
        advanceUntilIdle()

        assertNotNull(event)
        assertEquals(HomeNavEvent(HomeDestination.TRANSLATE), event)

        job.cancel()
    }

    private fun makeVmLoggedInAsUser1(
        scope: CoroutineScope,
        ioDispatcher: TestDispatcher
    ): Pair<HomeViewModel, MockDB> {
        val db = MockDB()
        db.setUserSession(
            UserSession(
                userId = "1",
                email = "yanjin@gmail.com",
                userName = "Yanjin"
            )
        )

        val repos = Repositories(
            auth = MockAuthRepository(db),
            star = MockStarRepository(db),
            user = MockUserRepository(db),
            learning = MockLearningRepository(db),
            translate = MockTranslateRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        )

        val model = Model(
            repos = repos,
            ioDispatcher = ioDispatcher
        )

        return HomeViewModel(model, scope) to db
    }
}