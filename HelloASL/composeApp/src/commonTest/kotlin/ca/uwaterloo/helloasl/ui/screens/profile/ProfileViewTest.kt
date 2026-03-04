package ca.uwaterloo.helloasl.ui.screens.profile

import ca.uwaterloo.helloasl.data.repository.AuthRepository
import ca.uwaterloo.helloasl.data.repository.UserRepository
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.domain.Repositories
import ca.uwaterloo.helloasl.domain.userModel.LearningProgress
import ca.uwaterloo.helloasl.domain.userModel.User
import ca.uwaterloo.helloasl.domain.userModel.UserProfile
import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * NOTE:
 * This is a "non-UI View test" in commonTest.
 * We cannot render @Composable ProfileView() here without Compose UI testing dependencies.
 *
 * So we test what ProfileView *displays* by deriving the exact UI strings from ProfileUiState
 * and checking they change when the ViewModel state changes.
 */
class ProfileViewTest {
    private class FakeAuthRepository : AuthRepository {
        override fun signup(name: String, email: String, password: String) = true
        override fun login(email: String, password: String) = true
        override fun logout() {}
    }

    private class FakeUserRepository(
        private var user: User,
        private var profile: UserProfile
    ) : UserRepository {

        override fun getUser(): User = user
        override fun getUserProfile(): UserProfile = profile

        override fun updateLearningGoals(minutesPerDay: Int, daysPerWeek: Int) {
            profile = profile.copy(
                learningGoalPerDay = minutesPerDay,
                learningGoalPerWeek = daysPerWeek
            )
        }
    }

    private fun makeVm(
        minutesPerDay: Int = 15,
        daysPerWeek: Int = 3
    ): ProfileViewModel {
        val userRepo = FakeUserRepository(
            user = User(id = 1, name = "Yanjin Xia", email = "yanjin@gmail.com"),
            profile = UserProfile(
                userId = 1,
                learningGoalPerDay = minutesPerDay,
                learningGoalPerWeek = daysPerWeek,
                streakDays = 7,
                learningProgress = LearningProgress(module = 1, lesson = 2),
                wordsLearned = 40,
                starredSigns = 12
            )
        )
        val model = Model(Repositories(auth = FakeAuthRepository(), user = userRepo))
        return ProfileViewModel(model)
    }

    private fun learningGoalTextPerDay(state: ProfileUiState): String =
        "Learn ${state.learningGoalPerDay} minutes per day"

    private fun learningGoalTextPerWeek(state: ProfileUiState): String =
        "Learn ${state.learningGoalPerWeek} days per week"

    @Test
    fun learningGoalTexts_change_when_vm_state_changes() {
        val vm = makeVm(minutesPerDay = 15, daysPerWeek = 3)
        val beforeDay = learningGoalTextPerDay(vm.state)
        val beforeWeek = learningGoalTextPerWeek(vm.state)

        vm.onSaveLearningGoals(minutesPerDay = 25, daysPerWeek = 5)

        val afterDay = learningGoalTextPerDay(vm.state)
        val afterWeek = learningGoalTextPerWeek(vm.state)

        assertNotEquals(beforeDay, afterDay)
        assertNotEquals(beforeWeek, afterWeek)
    }

    @Test
    fun headerText_changes_when_userName_changes() {
        val vm = makeVm()
        val beforeName = vm.state.userName
        val beforeAvatar = vm.state.avatarText

        val vm2 = makeVm().also {
        }

        val vmDifferent = run {
            val userRepo = FakeUserRepository(
                user = User(id = 1, name = "New Name", email = "n@uw.ca"),
                profile = UserProfile(
                    userId = 1,
                    learningGoalPerDay = 15,
                    learningGoalPerWeek = 3,
                    streakDays = 7,
                    learningProgress = LearningProgress(1, 2),
                    wordsLearned = 40,
                    starredSigns = 12
                )
            )
            val model = Model(Repositories(auth = FakeAuthRepository(), user = userRepo))
            ProfileViewModel(model)
        }

        assertNotEquals(beforeName, vmDifferent.state.userName)
        assertNotEquals(beforeAvatar, vmDifferent.state.avatarText)
    }
}