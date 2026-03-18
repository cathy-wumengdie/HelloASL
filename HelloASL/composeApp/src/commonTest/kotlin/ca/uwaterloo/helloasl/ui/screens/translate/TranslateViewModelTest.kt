package ca.uwaterloo.helloasl.ui.screens.translate

import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
internal class TranslateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeVm(): TranslateViewModel {
        val db = MockDB()
        val model = Model(
            Repositories(
                auth = MockAuthRepository(db),
                user = MockUserRepository(db),
                star = MockStarRepository(db),
                translate = MockTranslateRepository(db),
                learning = MockLearningRepository(db),
                progressTracker = MockProgressTrackerRepository(db)
            )
        )
        return TranslateViewModel(model)
    }

    @Test
    fun onSearch_valid_query_sets_lastResult_and_updates_history() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onClearHistory()
        advanceUntilIdle()

        vm.onQueryChange("hello")
        vm.onSearch()
        advanceUntilIdle()

        assertNull(vm.state.errorMessage)
        assertNotNull(vm.state.lastResult)
        assertEquals("Hello", vm.state.lastResult?.gloss)
        assertTrue(vm.state.searchHistory.isNotEmpty())
        assertEquals("hello", vm.state.searchHistory.first().query)
    }

    @Test
    fun onSelectHistoryItem_sets_query_and_runs_search() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onClearHistory()
        advanceUntilIdle()

        vm.onQueryChange("Hello")
        vm.onSearch()
        advanceUntilIdle()

        val item = vm.state.searchHistory.first()
        vm.onSelectHistoryItem(item.query)
        advanceUntilIdle()

        assertEquals(item.query, vm.state.query)
        assertNotNull(vm.state.lastResult)
        assertEquals("Hello", vm.state.lastResult?.gloss)
    }

    @Test
    fun start_and_stop_camera_toggles_isCameraRunning() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        assertFalse(vm.state.isCameraRunning)

        vm.onStartCamera()
        assertTrue(vm.state.isCameraRunning)

        advanceUntilIdle()
        assertEquals("Hello", vm.state.recoText)
        assertTrue(vm.state.confidence in 0f..1f)

        vm.onStopCamera()
        assertFalse(vm.state.isCameraRunning)
    }

    @Test
    fun onSearch_blank_query_sets_errorMessage_and_does_not_change_result() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("   ")
        vm.onSearch()
        advanceUntilIdle()

        assertEquals("Please enter a word.", vm.state.errorMessage)
        assertNull(vm.state.lastResult)
    }

    @Test
    fun searchHistory_shows_most_recent_query() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onClearHistory()
        advanceUntilIdle()

        listOf("a", "b", "c", "d", "hello", "yes").forEach {
            vm.onQueryChange(it)
            vm.onSearch()
            advanceUntilIdle()
        }

        val history = vm.state.searchHistory
        assertTrue(history.isNotEmpty())
        assertEquals("yes", history.first().query)
    }
}