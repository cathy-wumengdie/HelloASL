package ca.uwaterloo.helloasl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.uwaterloo.helloasl.ui.theme.HelloASLTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import ca.uwaterloo.helloasl.ui.screens.home.HomeScreen
import ca.uwaterloo.helloasl.ui.screens.home.HomeViewModel
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileScreen
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    HelloASLTheme {
        val homeVm = remember { HomeViewModel() }
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
        Scaffold(
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
                    MainTab.TRANSLATE -> {}
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
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home"
                            )
                        },
                        colors = navBarIconColors
                    );
                    NavigationBarItem(
                        selected = (selectedTab == MainTab.LEARNING),
                        onClick = { selectedTab = MainTab.LEARNING },
                        icon = { Icon(imageVector = Icons.Filled.School, contentDescription = "Learning") },
                        colors = navBarIconColors
                    );
                    NavigationBarItem(
                        selected = (selectedTab == MainTab.TRANSLATE),
                        onClick = { selectedTab = MainTab.TRANSLATE },
                        icon = { Icon(imageVector = Icons.Filled.Translate, contentDescription = "Translate") },
                        colors = navBarIconColors
                    );
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
                    MainTab.TRANSLATE -> {}
                    MainTab.PROFILE -> {
                        ProfileScreen(
                            state = profileVm.state,
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