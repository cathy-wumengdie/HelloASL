package ca.uwaterloo.helloasl.ui.profile

import ca.uwaterloo.helloasl.ui.navigations.ProfileDestination
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlin.test.*

class ProfileViewModelTest {

    private lateinit var model: FakeModel
    private lateinit var vm: ProfileViewModel

    @BeforeTest
    fun setup() {
        model = FakeModel()
        vm = ProfileViewModel(model)
    }

    @Test
    fun buildState_initial_values_are_correct() {
        val state = vm.state

        assertEquals("Yanjin", state.userName)
        assertEquals("Y", state.avatarText)
        assertEquals(40, state.wordsLearned)
        assertEquals(12, state.starredSigns)
        assertEquals(15, state.learningGoalPerDay)
        assertEquals(3, state.learningGoalPerWeek)
    }

    @Test
    fun onSaveLearningGoals_updates_state() {
        vm.onSaveLearningGoals(20, 5)

        val state = vm.state

        assertEquals(20, state.learningGoalPerDay)
        assertEquals(5, state.learningGoalPerWeek)
    }

    @Test
    fun onWordsLearned_emits_navigation_event() = runBlocking {
        vm.onWordsLearned()

        val event = vm.navEvents.first()

        assertEquals(ProfileDestination.WORDS_LEARNED, event.dest)
    }

    @Test
    fun onStarredSigns_emits_navigation_event() = runBlocking {
        vm.onStarredSigns()

        val event = vm.navEvents.first()

        assertEquals(ProfileDestination.STARRED_SIGNS, event.dest)
    }

    @Test
    fun onAccount_emits_navigation_event() = runBlocking {
        vm.onAccount()

        val event = vm.navEvents.first()

        assertEquals(ProfileDestination.ACCOUNT, event.dest)
    }

    @Test
    fun onLicense_emits_navigation_event() = runBlocking {
        vm.onLicense()

        val event = vm.navEvents.first()

        assertEquals(ProfileDestination.LICENSE, event.dest)
    }

    @Test
    fun onSignOut_emits_navigation_event() = runBlocking {
        vm.onSignOut()

        val event = vm.navEvents.first()

        assertEquals(ProfileDestination.SIGN_IN, event.dest)
    }
}