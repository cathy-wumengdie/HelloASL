package ca.uwaterloo.helloasl.ui.screens.translate

import ca.uwaterloo.helloasl.MainDispatcherRule
import ca.uwaterloo.helloasl.data.MockDB
import ca.uwaterloo.helloasl.data.authRepository.MockAuthRepository
import ca.uwaterloo.helloasl.data.learningRepository.MockLearningRepository
import ca.uwaterloo.helloasl.data.notificationRepository.NoOpNotificationRepository
import ca.uwaterloo.helloasl.data.progressTrackerRepository.MockProgressTrackerRepository
import ca.uwaterloo.helloasl.data.starRepository.MockStarRepository
import ca.uwaterloo.helloasl.data.translateRepository.MockTranslateRepository
import ca.uwaterloo.helloasl.data.userRepository.MockUserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TranslateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun makeVm(): TranslateViewModel {
        val db = MockDB()
        val model = Model(
            Repositories(
                auth = MockAuthRepository(db),
                user = MockUserRepository(db),
                star = MockStarRepository(db),
                learning = MockLearningRepository(db),
                translate = MockTranslateRepository(db),
                progressTracker = MockProgressTrackerRepository(db),
                notification = NoOpNotificationRepository
            ),
            ioDispatcher = mainDispatcherRule.dispatcher
        )
        return TranslateViewModel(model)
    }

    @Test
    fun onSearch_blank_query_sets_error_message() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("   ")
        vm.onSearch()

        assertEquals("Please enter a word.", vm.state.errorMessage)
        assertNull(vm.state.lastResult)
    }

    @Test
    fun onSearch_valid_query_sets_result_and_history() = runTest {
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
    }

    @Test
    fun onSearch_unknown_word_sets_no_result_error() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("unknown")
        vm.onSearch()
        advanceUntilIdle()

        assertEquals("No result found.", vm.state.errorMessage)
        assertNull(vm.state.lastResult)
    }

    @Test
    fun onSelectHistoryItem_updates_query_and_runs_search() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("hello")
        vm.onSearch()
        advanceUntilIdle()

        val item = vm.state.searchHistory.first()
        vm.onSelectHistoryItem(item.query)
        advanceUntilIdle()

        assertEquals(item.query, vm.state.query)
        assertNotNull(vm.state.lastResult)
    }

    @Test
    fun onSwitchMode_updates_mode_and_clears_error() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("   ")
        vm.onSearch()
        assertNotNull(vm.state.errorMessage)

        vm.onSwitchMode(TranslateMode.ASL_TO_EN)
        assertEquals(TranslateMode.ASL_TO_EN, vm.state.mode)
        assertNull(vm.state.errorMessage)
    }

    @Test
    fun onQueryChange_updates_query_and_clears_error() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("   ")
        vm.onSearch()
        assertNotNull(vm.state.errorMessage)

        vm.onQueryChange("hello")
        assertEquals("hello", vm.state.query)
        assertNull(vm.state.errorMessage)
    }

    @Test
    fun preview_toggle_updates_state() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onStartPreview()
        assertTrue(vm.state.isPreviewActive)

        vm.onStopPreview()
        assertFalse(vm.state.isPreviewActive)
        assertFalse(vm.state.isRecording)
    }

    @Test
    fun start_recording_requires_preview() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onStartRecording()
        assertEquals("Start the camera first.", vm.state.errorMessage)

        vm.onStartPreview()
        vm.onStartRecording()
        assertTrue(vm.state.isRecording)
        assertEquals(0f, vm.state.confidence)
    }

    @Test
    fun stop_recording_and_recording_error_update_state() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onStartPreview()
        vm.onStartRecording()
        vm.onStopRecording()
        assertFalse(vm.state.isRecording)

        vm.onStartRecording()
        vm.onRecordingError("Recorder failed")
        assertFalse(vm.state.isRecording)
        assertEquals("Recorder failed", vm.state.errorMessage)
    }

    @Test
    fun recording_saved_and_cleared_updates_state() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onStartPreview()
        vm.onStartRecording()
        vm.onRecordingSaved("file://demo.mp4")

        assertFalse(vm.state.isRecording)
        assertEquals("file://demo.mp4", vm.state.recordedVideoUri)

        vm.onClearRecording()
        assertNull(vm.state.recordedVideoUri)
        assertEquals("", vm.state.recoText)
        assertEquals(0f, vm.state.confidence)
        assertFalse(vm.state.isRecognizing)
    }

    @Test
    fun interpret_recording_requires_video_and_updates_result() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onInterpretRecording()
        assertEquals("Please record a video first.", vm.state.errorMessage)

        vm.onRecordingSaved("file://demo.mp4")
        vm.onInterpretRecording()
        advanceUntilIdle()

        assertFalse(vm.state.isRecognizing)
        assertTrue(vm.state.confidence in 0f..1f)
        assertTrue(vm.state.recoText.isNotBlank())
    }

    @Test
    fun onClearHistory_empties_search_history() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onQueryChange("hello")
        vm.onSearch()
        advanceUntilIdle()

        assertTrue(vm.state.searchHistory.isNotEmpty())

        vm.onClearHistory()
        advanceUntilIdle()

        assertTrue(vm.state.searchHistory.isEmpty())
        assertNull(vm.state.errorMessage)
    }
}