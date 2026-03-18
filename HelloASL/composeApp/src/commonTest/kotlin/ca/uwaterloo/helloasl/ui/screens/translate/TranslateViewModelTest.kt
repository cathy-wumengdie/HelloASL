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
import kotlin.test.*

internal class TranslateViewModelTest {
    private fun makeVm(): TranslateViewModel {
        val db = MockDB()
        val model = Model ( Repositories(
            auth = MockAuthRepository(db),
            user = MockUserRepository(db),
            star = MockStarRepository(db),
            translate = MockTranslateRepository(db),
            learning = MockLearningRepository(db),
            progressTracker = MockProgressTrackerRepository(db)
        ))
        return TranslateViewModel(model)
    }

    @Test
    fun onSearch_valid_query_sets_lastResult_and_updates_history() {
        val vm = makeVm()

        vm.onClearHistory()
        vm.onQueryChange("hello")
        vm.onSearch()

        assertNull(vm.state.errorMessage)
        assertNotNull(vm.state.lastResult)
        assertTrue(vm.state.searchHistory.isNotEmpty())
        assertEquals("hello", vm.state.searchHistory.first().query)
    }

    @Test
    fun onSelectHistoryItem_sets_query_and_runs_search() {
        val vm = makeVm()

        vm.onClearHistory()
        vm.onQueryChange("hello")
        vm.onSearch()

        // Select the first history item
        val item = vm.state.searchHistory.first()
        vm.onSelectHistoryItem(item.query)

        assertEquals(item.query, vm.state.query)
        assertNotNull(vm.state.lastResult)
    }

    @Test
    fun start_and_stop_camera_toggles_isCameraRunning() {
        val vm = makeVm()
        // isCameraRunning should be false at start
        assertFalse(vm.state.isCameraRunning)

        vm.onStartCamera()
        assertTrue(vm.state.isCameraRunning)

        vm.onStopCamera()
        assertFalse(vm.state.isCameraRunning)
    }

    @Test
    fun onSearch_blank_query_sets_errorMessage_and_does_not_change_result() {
        val vm = makeVm()

        vm.onQueryChange("   ")
        vm.onSearch()

        assertEquals("Please enter a word.", vm.state.errorMessage)
        assertNull(vm.state.lastResult)
    }

    // Search history should only display the queries in reverse order
    @Test
    fun searchHistory_shows_most_recent_query() {
        val vm = makeVm()

        vm.onClearHistory()
        listOf("a","b","c","d","hello","thanks").forEach {
            vm.onQueryChange(it)
            vm.onSearch()
        }

        val history = vm.state.searchHistory
        assertEquals("thanks", history.first().query)
    }
}