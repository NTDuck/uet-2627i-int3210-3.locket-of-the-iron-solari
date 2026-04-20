package com.solari.app

import android.os.Build
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.solari.app.data.di.AppContainer
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.screens.*
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.*
import com.solari.app.ui.models.Conversation

private const val FriendManagementTransitionMillis = 360
private const val SelectedConversationKey = "selected_conversation"
private const val ChatSettingsPartnerKey = "chat_settings_partner"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferHighestRefreshRate()
        enableEdgeToEdge()
        setContent {
            val appContainer = (application as SolariApplication).appContainer
            val settingsViewModel: SettingsViewModel = viewModel()
            val isSystemDark = isSystemInDarkTheme()
            
            LaunchedEffect(Unit) {
                settingsViewModel.isDarkMode = isSystemDark
            }
            
            SolariTheme(
                variant = settingsViewModel.activeThemeVariant
            ) {
                SolariApp(
                    settingsViewModel = settingsViewModel,
                    appContainer = appContainer
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun preferHighestRefreshRate() {
        val display: Display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display ?: return
        } else {
            windowManager.defaultDisplay
        }

        val highestRefreshRate = display.supportedModes
            .maxOfOrNull { it.refreshRate }
            ?: return

        window.attributes = window.attributes.apply {
            preferredRefreshRate = highestRefreshRate
        }
    }
}

private fun NavController.navigateToChat(conversation: Conversation) {
    currentBackStackEntry
        ?.savedStateHandle
        ?.set(SelectedConversationKey, conversation)
    navigate(SolariRoute.Screen.Chat.name + "/${conversation.id}")
}

@Composable
fun SolariApp(
    settingsViewModel: SettingsViewModel,
    appContainer: AppContainer
) {
    val navController = rememberNavController()
    val appAuthViewModel: AppAuthViewModel = viewModel(factory = appContainer.viewModelFactory)
    val authState by appAuthViewModel.uiState.collectAsState()

    if (authState.isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = SolariTheme.colors.primary,
                trackColor = SolariTheme.colors.surface
            )
        }
        return
    }

    val startDestination = if (authState.isAuthenticated) {
        SolariRoute.Screen.Main.name + "/0"
    } else {
        SolariRoute.Screen.Welcome.name
    }

    NavHost(
        navController = navController, 
        modifier = Modifier.background(SolariTheme.colors.background),
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(SolariRoute.Screen.Welcome.name) {
            val viewModel: WelcomeViewModel = viewModel()
            WelcomeScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(SolariRoute.Screen.SignUp.name) },
                onNavigateToSignIn = { navController.navigate(SolariRoute.Screen.SignIn.name) }
            )
        }
        composable(SolariRoute.Screen.SignUp.name) {
            val viewModel: SignUpViewModel = viewModel()
            SignUpScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignIn = { navController.navigate(SolariRoute.Screen.SignIn.name) },
                onSignUpComplete = { navController.navigate(SolariRoute.Screen.Main.name + "/0") }
            )
        }
        composable(SolariRoute.Screen.SignIn.name) {
            val viewModel: SignInViewModel = viewModel(factory = appContainer.viewModelFactory)
            SignInScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSignUp = { navController.navigate(SolariRoute.Screen.SignUp.name) },
                onNavigateToForgotPassword = { navController.navigate(SolariRoute.Screen.PasswordRecovery.name) },
                onSignInComplete = {
                    appAuthViewModel.onSignedIn()
                    navController.navigate(SolariRoute.Screen.Main.name + "/0") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(SolariRoute.Screen.OTP.name + "/{purpose}") { backStackEntry ->
            val purpose = backStackEntry.arguments?.getString("purpose") ?: "signup"
            val viewModel: OTPConfirmationViewModel = viewModel()
            OTPConfirmationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onConfirmComplete = {
                    if (purpose == "reset") {
                        navController.navigate(SolariRoute.Screen.PasswordReset.name + "/false")
                    } else {
                        navController.navigate(SolariRoute.Screen.Main.name + "/0")
                    }
                }
            )
        }
        composable(SolariRoute.Screen.PasswordRecovery.name) {
            val viewModel: PasswordRecoveryViewModel = viewModel()
            PasswordRecoveryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onGetRecoveryCode = { navController.navigate(SolariRoute.Screen.OTP.name + "/reset") }
            )
        }
        composable(SolariRoute.Screen.PasswordReset.name + "/{showTopBar}") { backStackEntry ->
            val showTopBar = backStackEntry.arguments?.getString("showTopBar")?.toBoolean() ?: true
            val viewModel: PasswordResetViewModel = viewModel()
            PasswordResetScreen(
                viewModel = viewModel,
                showTopBar = showTopBar,
                onNavigateBack = { navController.popBackStack() },
                onResetComplete = { 
                    if (showTopBar) navController.popBackStack() else navController.navigate(SolariRoute.Screen.SignIn.name)
                }
            )
        }
        composable(
            route = SolariRoute.Screen.Main.name + "/{page}",
            exitTransition = {
                if (targetState.destination.route == SolariRoute.Screen.FriendManagement.name) {
                    ExitTransition.None
                } else {
                    null
                }
            },
            popEnterTransition = {
                if (initialState.destination.route == SolariRoute.Screen.FriendManagement.name) {
                    EnterTransition.None
                } else {
                    null
                }
            }
        ) { backStackEntry ->
            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 0
            MainScreen(
                initialPage = page,
                settingsViewModel = settingsViewModel,
                viewModelFactory = appContainer.viewModelFactory,
                onNavigateToChat = { conversation -> navController.navigateToChat(conversation) },
                onNavigateToManageFriends = { navController.navigate(SolariRoute.Screen.FriendManagement.name) },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToChangePassword = { navController.navigate(SolariRoute.Screen.PasswordReset.name + "/true") },
                onNavigateToChangeTheme = { navController.navigate(SolariRoute.Screen.ChangeTheme.name) },
                onNavigateToFeedBrowse = { navController.navigate(SolariRoute.Screen.FeedBrowse.name) },
                onCapture = { navController.navigate(SolariRoute.Screen.CameraAfter.name) },
                onLogout = {
                    appAuthViewModel.signOutLocal()
                    navController.navigate(SolariRoute.Screen.Welcome.name) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(SolariRoute.Screen.Main.name + "/{page}/{postId}") { backStackEntry ->
            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 0
            val postId = backStackEntry.arguments?.getString("postId")
            MainScreen(
                initialPage = page,
                initialFeedPostId = postId,
                settingsViewModel = settingsViewModel,
                viewModelFactory = appContainer.viewModelFactory,
                onNavigateToChat = { conversation -> navController.navigateToChat(conversation) },
                onNavigateToManageFriends = { navController.navigate(SolariRoute.Screen.FriendManagement.name) },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToChangePassword = { navController.navigate(SolariRoute.Screen.PasswordReset.name + "/true") },
                onNavigateToChangeTheme = { navController.navigate(SolariRoute.Screen.ChangeTheme.name) },
                onNavigateToFeedBrowse = { navController.navigate(SolariRoute.Screen.FeedBrowse.name) },
                onCapture = { navController.navigate(SolariRoute.Screen.CameraAfter.name) },
                onLogout = {
                    appAuthViewModel.signOutLocal()
                    navController.navigate(SolariRoute.Screen.Welcome.name) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(SolariRoute.Screen.CameraAfter.name) {
            val viewModel: HomepageAfterCapturingViewModel = viewModel(factory = appContainer.viewModelFactory)
            HomepageAfterCapturingScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSend = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onCancel = { navController.popBackStack() },
                onNavigateToFeed = { navController.navigate(SolariRoute.Screen.Main.name + "/1") },
                onNavigateToChat = { navController.navigate(SolariRoute.Screen.Main.name + "/2") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") }
            )
        }
        composable(SolariRoute.Screen.FeedBrowse.name) {
            val viewModel: FeedBrowseViewModel = viewModel(factory = appContainer.viewModelFactory)
            FeedBrowseScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onNavigateToChat = { navController.navigate(SolariRoute.Screen.Main.name + "/2") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") },
                onNavigateToPost = { postId -> navController.navigate(SolariRoute.Screen.Main.name + "/1/$postId") }
            )
        }
        composable(SolariRoute.Screen.Chat.name + "/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val viewModel: ChatViewModel = viewModel(factory = appContainer.viewModelFactory)
            val selectedConversation = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Conversation>(SelectedConversationKey)

            LaunchedEffect(chatId) {
                selectedConversation?.let(viewModel::setInitialConversation)
                viewModel.loadConversation(chatId)
            }
            ChatScreen(
                chatId = chatId,
                initialPartner = selectedConversation?.otherUser,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { settingsChatId, partner ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(ChatSettingsPartnerKey, partner)
                    navController.navigate(SolariRoute.Screen.ChatSettings.name + "/$settingsChatId")
                },
                onNavigateToCamera = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onNavigateToFeed = { navController.navigate(SolariRoute.Screen.Main.name + "/1") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") }
            )
        }
        composable(SolariRoute.Screen.ChatSettings.name + "/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId").orEmpty()
            val viewModel: ChatSettingsViewModel = viewModel(factory = appContainer.viewModelFactory)
            val initialPartner = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<com.solari.app.ui.models.User>(ChatSettingsPartnerKey)
            ChatSettingsScreen(
                chatId = chatId,
                initialPartner = initialPartner,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onNavigateToFeed = { navController.navigate(SolariRoute.Screen.Main.name + "/1") },
                onNavigateToChat = { navController.navigate(SolariRoute.Screen.Main.name + "/2") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") }
            )
        }
        composable(SolariRoute.Screen.ChangeTheme.name) {
            val viewModel: ChangeThemeViewModel = viewModel()
            ChangeThemeScreen(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = SolariRoute.Screen.FriendManagement.name,
            enterTransition = {
                if (initialState.destination.route?.startsWith(SolariRoute.Screen.Main.name) == true) {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = FriendManagementTransitionMillis,
                            easing = FastOutSlowInEasing
                        )
                    )
                } else {
                    null
                }
            },
            popExitTransition = {
                if (targetState.destination.route?.startsWith(SolariRoute.Screen.Main.name) == true) {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(
                            durationMillis = FriendManagementTransitionMillis,
                            easing = FastOutSlowInEasing
                        )
                    )
                } else {
                    null
                }
            }
        ) {
            val viewModel: FriendManagementViewModel = viewModel(factory = appContainer.viewModelFactory)
            FriendManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToCamera = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onNavigateToFeed = { navController.navigate(SolariRoute.Screen.Main.name + "/1") },
                onNavigateToChat = { navController.navigate(SolariRoute.Screen.Main.name + "/2") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") }
            )
        }
        composable(SolariRoute.Screen.BlockedAccounts.name) {
            val viewModel: BlockedAccountsViewModel = viewModel(factory = appContainer.viewModelFactory)
            BlockedAccountsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onNavigateToFeed = { navController.navigate(SolariRoute.Screen.Main.name + "/1") },
                onNavigateToChat = { navController.navigate(SolariRoute.Screen.Main.name + "/2") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") }
            )
        }
    }
}
