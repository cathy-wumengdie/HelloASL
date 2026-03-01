package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ca.uwaterloo.helloasl.ui.screens.home.HomeDestination
import ca.uwaterloo.helloasl.ui.screens.home.HomeView
import ca.uwaterloo.helloasl.ui.screens.home.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeRoute(
    vm: HomeViewModel,
    onDayStreak: () -> Unit,
    onDailyGoals: () -> Unit,
    onLearning: () -> Unit,
    onTakeQuiz: () -> Unit,
    onTranslate: () -> Unit,
    onNotifications: () -> Unit,
) {
    LaunchedEffect(vm) {
        vm.navEvents.collectLatest { event ->
            when (event.dest) {
                HomeDestination.LEARNING -> onLearning()
                HomeDestination.TRANSLATE -> onTranslate()
                HomeDestination.QUIZ -> onTakeQuiz()
                HomeDestination.DAY_STREAK -> onDayStreak()
                HomeDestination.DAILY_GOALS -> onDailyGoals()
                HomeDestination.NOTIFICATIONS -> onNotifications()
            }
        }
    }

    HomeView(vm = vm)
}
