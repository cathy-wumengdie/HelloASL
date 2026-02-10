package ca.uwaterloo.helloasl

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.uwaterloo.helloasl.ui.theme.HelloASLTheme
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import ca.uwaterloo.helloasl.ui.screens.home.*
import ca.uwaterloo.helloasl.ui.screens.profile.*
import ca.uwaterloo.helloasl.ui.screens.translate.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    HelloASLTheme {
        val homeVm = remember { HomeViewModel() }
        val translateVm = remember {TranslateViewModel()}
        val profileVm = remember { ProfileViewModel() }
        var selectedTab by remember { mutableStateOf(MainTab.HOME) }
        val selectedColor = when (selectedTab) {
            MainTab.HOME -> MaterialTheme.colorScheme.primary
            MainTab.LEARNING -> MaterialTheme.colorScheme.secondary
            MainTab.TRANSLATE -> MaterialTheme.colorScheme.tertiary
            MainTab.PROFILE -> MaterialTheme.colorScheme.surface
        }
        val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
        val navBarIconColors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = MaterialTheme.colorScheme.surfaceContainer
        )
        val navBarColor = when (selectedTab) {
            MainTab.HOME -> MaterialTheme.colorScheme.primaryContainer
            MainTab.LEARNING -> MaterialTheme.colorScheme.secondaryContainer
            MainTab.TRANSLATE -> MaterialTheme.colorScheme.tertiaryContainer
            MainTab.PROFILE -> MaterialTheme.colorScheme.surfaceVariant
        }
        Scaffold(  // Top + Main Content + Bottom
            topBar = {
                when (selectedTab) {
                    MainTab.HOME -> {
                        TopAppBar(
                            title = { Text("Hello, Yanjin!") },
                            actions = {
                                IconButton(onClick = { /* notification page */ }) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = "Notifications"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = selectedColor,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    MainTab.LEARNING -> {}
                    MainTab.TRANSLATE -> {
                        TopAppBar(
                            title = { Text("Translate ASL")},
                            actions = {/*fill in later*/},
                            // think: what do we need for action, a toggle button to change between ASL -> Eng & Eng -> ASL?
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = selectedColor,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    MainTab.PROFILE -> {
                        TopAppBar(
                            title = { Text("Profile") },
                            actions = {
                                IconButton(onClick = { /* settings page */ }) {
                                    Icon(
                                        Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = selectedColor,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

            },
            bottomBar = {
                NavigationBar(containerColor = navBarColor) {
                    NavigationBarItem(
                        selected = (selectedTab == MainTab.HOME),
                        onClick = { selectedTab = MainTab.HOME },
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                        colors = navBarIconColors
                    )
                    NavigationBarItem(
                        selected = (selectedTab == MainTab.LEARNING),
                        onClick = { selectedTab = MainTab.LEARNING },
                        icon = { Icon(imageVector = Icons.Filled.School, contentDescription = "Learning") },
                        colors = navBarIconColors
                    )
                    NavigationBarItem(
                        selected = (selectedTab == MainTab.TRANSLATE),
                        onClick = { selectedTab = MainTab.TRANSLATE },
                        icon = { Icon(imageVector = Icons.Filled.Translate, contentDescription = "Translate") },
                        colors = navBarIconColors
                    )
                    NavigationBarItem(
                        selected = (selectedTab == MainTab.PROFILE),
                        onClick = { selectedTab = MainTab.PROFILE },
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Profile") },
                        colors = navBarIconColors
                    )
                }
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selectedTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            state = homeVm.state,
                            onContinueLearning = {
                                homeVm.onContinueLearning()
                                selectedTab = MainTab.LEARNING
                            },
                            onDayStreak = {
                                homeVm.onDayStreak()
                                // later: navigate to streak details screen
                            },
                            onDailyGoals = {
                                homeVm.onDailyGoals()
                                // later: navigate to goals screen
                            },
                            onLearnAsl = {
                                homeVm.onLearnAsl()
                                selectedTab = MainTab.LEARNING
                            },
                            onTakeQuiz = {
                                homeVm.onTakeQuiz()
                                // later: navigate to quiz screen
                            },
                            onTranslate = {
                                homeVm.onTranslate()
                                selectedTab = MainTab.TRANSLATE
                            }
                        )
                    }

                    MainTab.LEARNING -> {}

                    MainTab.TRANSLATE -> { TranslateScreen(translateVm) }

                    MainTab.PROFILE -> {
                        ProfileScreen(
                            state = profileVm.state,
                            onSettings = {
                                profileVm.onSettings()
                                // later: navigate to settings screen
                            },
                            onWordsLearned = {
                                profileVm.onWordsLearned()
                                // later: navigate to words learned screen
                            },
                            onStarredSigns = {
                                profileVm.onStarredSigns()
                                // later: navigate to starred signs screen
                            },
                            onSetLearningGoals = {
                                profileVm.onSetLearningGoals()
                                // later: navigate to set learning goal screen
                            },
                            onAccount = {
                                profileVm.onAccount()
                                // later: navigate to account screen
                            },
                            onLicense = {
                                profileVm.onLicense()
                                // later: navigate to license screen
                            },
                            onSignOut = {
                                profileVm.onSignOut()
                                // later: navigate to log in screen
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class MainTab {
    HOME, LEARNING, TRANSLATE, PROFILE
}