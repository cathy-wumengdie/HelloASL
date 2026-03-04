package ca.uwaterloo.helloasl.ui.navigations

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import ca.uwaterloo.helloasl.domain.Model
import ca.uwaterloo.helloasl.ui.screens.PermissionsGateScreen
import ca.uwaterloo.helloasl.ui.screens.auth.login.LoginViewModel
import ca.uwaterloo.helloasl.ui.screens.auth.signup.SignupViewModel
import ca.uwaterloo.helloasl.ui.screens.home.HomeViewModel
import ca.uwaterloo.helloasl.ui.screens.learning.LearningRoute as LearningInnerRoute
import ca.uwaterloo.helloasl.ui.screens.learning.LearningViewModel
import ca.uwaterloo.helloasl.ui.screens.learning.LessonViewModel
import ca.uwaterloo.helloasl.ui.screens.profile.ProfileViewModel
import ca.uwaterloo.helloasl.ui.screens.star.StarViewModel
import ca.uwaterloo.helloasl.ui.screens.translate.TranslateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    model: Model,
    hasCameraHardware: Boolean,
    cameraGranted: Boolean,
    notificationGranted: Boolean,
    requestCameraPermission: () -> Unit,
    requestNotificationPermission: () -> Unit,
    hasSeenPermissionGate: Boolean,
    onPermissionGateCompleted: () -> Unit
) {
    var authRoute by rememberSaveable { mutableStateOf(AuthRoute.LOGIN) }
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    if (!isLoggedIn) {
        val loginVm = remember { LoginViewModel(model) }
        val signupVm = remember { SignupViewModel(model) }

        AuthRouteHost(
            model = model,
            route = authRoute,
            loginVm = loginVm,
            signupVm = signupVm,
            onRouteChange = { authRoute = it },
            onAuthSuccess = { isLoggedIn = true }
        )
        return
    }

    if (!hasSeenPermissionGate) {
        PermissionsGateScreen(
            hasCameraHardware = hasCameraHardware,
            cameraGranted = cameraGranted,
            notificationGranted = notificationGranted,
            onRequestCamera = requestCameraPermission,
            onRequestNotifications = requestNotificationPermission,
            onContinue = { onPermissionGateCompleted() }
        )
        return
    }

    val homeVm = remember { HomeViewModel(model) }
    val translateVm = remember { TranslateViewModel(model) }
    val profileVm = remember { ProfileViewModel(model) }
    val starVm = remember { StarViewModel(model) }

    val learningVm = remember { LearningViewModel(model) }
    val lessonVm = remember { LessonViewModel(model) }

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var previousTab by rememberSaveable { mutableStateOf(MainTab.LEARNING) }

    var learningRoute by rememberSaveable { mutableStateOf(LearningInnerRoute.LEARNING_HOME) }
    var lessonTitle by rememberSaveable { mutableStateOf("") }

    val selectedColor = when (selectedTab) {
        MainTab.HOME -> MaterialTheme.colorScheme.primary
        MainTab.LEARNING -> MaterialTheme.colorScheme.secondary
        MainTab.TRANSLATE -> MaterialTheme.colorScheme.tertiary
        MainTab.PROFILE -> MaterialTheme.colorScheme.surface
        MainTab.STAR -> MaterialTheme.colorScheme.secondary
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
        MainTab.STAR -> MaterialTheme.colorScheme.secondaryContainer
    }

    Scaffold(
        topBar = {
            when (selectedTab) {
                MainTab.HOME -> {
                    TopAppBar(
                        title = { Text("Hello, ${homeVm.state.userName}!") },
                        actions = {
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = selectedColor,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                MainTab.LEARNING -> {
                    TopAppBar(
                        title = {
                            Text(
                                when (learningRoute) {
                                    LearningInnerRoute.LEARNING_HOME -> "Learning"
                                    LearningInnerRoute.LESSON -> lessonTitle
                                }
                            )
                        },
                        navigationIcon = {
                            if (learningRoute != LearningInnerRoute.LEARNING_HOME) {
                                IconButton(onClick = { learningRoute = LearningInnerRoute.LEARNING_HOME }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = selectedColor,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                MainTab.TRANSLATE -> {
                    TopAppBar(
                        title = { Text("Translate ASL") },
                        actions = {
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        },
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
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            IconButton(onClick = { /* later */ }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = selectedColor,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                MainTab.STAR -> {
                    TopAppBar(
                        title = { Text("Starred Signs") },
                        navigationIcon = {
                            IconButton(onClick = { selectedTab = previousTab }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    colors = navBarIconColors
                )
                NavigationBarItem(
                    selected = (selectedTab == MainTab.LEARNING),
                    onClick = { selectedTab = MainTab.LEARNING },
                    icon = { Icon(Icons.Filled.School, contentDescription = "Learning") },
                    colors = navBarIconColors
                )
                NavigationBarItem(
                    selected = (selectedTab == MainTab.TRANSLATE),
                    onClick = { selectedTab = MainTab.TRANSLATE },
                    icon = { Icon(Icons.Filled.Translate, contentDescription = "Translate") },
                    colors = navBarIconColors
                )
                NavigationBarItem(
                    selected = (selectedTab == MainTab.PROFILE),
                    onClick = { selectedTab = MainTab.PROFILE },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
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
                MainTab.HOME -> HomeRoute(
                    vm = homeVm,
                    onDayStreak = { /* later */ },
                    onDailyGoals = { /* later */ },
                    onLearning = { selectedTab = MainTab.LEARNING },
                    onTakeQuiz = { /* later */ },
                    onTranslate = { selectedTab = MainTab.TRANSLATE },
                    onNotifications = { /* later */ }
                )

                MainTab.LEARNING -> LearningRoute(
                    vm = learningVm,
                    lessonVm = lessonVm,
                    route = learningRoute,
                    onNavigate = { learningRoute = it },
                    onUpdateLessonTitle = { lessonTitle = it },
                    onOpenStarred = {
                        previousTab = selectedTab
                        selectedTab = MainTab.STAR
                    }
                )

                MainTab.TRANSLATE -> TranslateRoute(
                    vm = translateVm
                )

                MainTab.PROFILE -> ProfileRoute(
                    vm = profileVm,
                    onSettings = { /* later */ },
                    onWordsLearned = { /* later */ },
                    onStarredSigns = {
                        previousTab = selectedTab
                        selectedTab = MainTab.STAR
                    },
                    onAccount = { /* later */ },
                    onLicense = { /* later */ },
                    onSignOut = {
                        model.logout()
                        isLoggedIn = false
                        authRoute = AuthRoute.LOGIN
                    }
                )

                MainTab.STAR -> StarRoute(
                    vm = starVm,
                    onBack = { selectedTab = previousTab },
                    onDetail = { /* later */ }
                )
            }
        }
    }
}