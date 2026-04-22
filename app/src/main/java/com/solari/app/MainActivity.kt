package com.solari.app

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.solari.app.data.di.AppContainer
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.FriendInvitePreviewDialog
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.screens.*
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.*
import com.solari.app.ui.models.Conversation
import kotlinx.coroutines.delay

private const val FriendManagementTransitionMillis = 360
private const val SelectedConversationKey = "selected_conversation"
private const val ChatSettingsPartnerKey = "chat_settings_partner"
private const val SolariWebHost = "solari.adnope.io.vn"

private data class FriendInviteDeepLink(
    val username: String,
    val sequence: Long
)

class MainActivity : ComponentActivity() {
    private var pendingFriendInviteDeepLink by mutableStateOf<FriendInviteDeepLink?>(null)
    private var friendInviteDeepLinkSequence = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingFriendInviteDeepLink = intent.extractFriendInviteDeepLink()
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
                    appContainer = appContainer,
                    pendingFriendInviteDeepLink = pendingFriendInviteDeepLink,
                    onFriendInviteDeepLinkConsumed = { consumedSequence ->
                        if (pendingFriendInviteDeepLink?.sequence == consumedSequence) {
                            pendingFriendInviteDeepLink = null
                        }
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingFriendInviteDeepLink = intent.extractFriendInviteDeepLink()
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

    private fun Intent.extractFriendInviteDeepLink(): FriendInviteDeepLink? {
        if (action != Intent.ACTION_VIEW) return null

        val username = data?.extractSolariProfileUsername() ?: return null
        friendInviteDeepLinkSequence += 1
        return FriendInviteDeepLink(
            username = username,
            sequence = friendInviteDeepLinkSequence
        )
    }
}

private fun Uri.extractSolariProfileUsername(): String? {
    if (!scheme.equals("https", ignoreCase = true)) return null
    if (!host.equals(SolariWebHost, ignoreCase = true)) return null

    val segments = pathSegments
    if (segments.size != 2 || segments.first() != "u") return null

    return segments[1].trim().takeIf { it.isNotEmpty() }
}

private fun NavController.navigateToChat(conversation: Conversation) {
    currentBackStackEntry
        ?.savedStateHandle
        ?.set(SelectedConversationKey, conversation)
    navigate(SolariRoute.Screen.Chat.name + "/${conversation.id}")
}

private fun NavController.navigateToWelcomeAfterLogout(onNavigated: () -> Unit) {
    val currentDestinationId = currentDestination?.id

    navigate(SolariRoute.Screen.Welcome.name) {
        launchSingleTop = true
        if (currentDestinationId != null) {
            popUpTo(currentDestinationId) {
                inclusive = true
            }
        }
    }
    onNavigated()
}

private fun NavController.navigateToFeedBrowse(authorId: String?) {
    val route = if (authorId.isNullOrBlank()) {
        SolariRoute.Screen.FeedBrowse.name
    } else {
        "${SolariRoute.Screen.FeedBrowse.name}?authorId=$authorId"
    }
    navigate(route)
}

private fun String?.isFriendManagementRoute(): Boolean {
    return this?.startsWith(SolariRoute.Screen.FriendManagement.name) == true
}

@Composable
private fun SolariApp(
    settingsViewModel: SettingsViewModel,
    appContainer: AppContainer,
    pendingFriendInviteDeepLink: FriendInviteDeepLink?,
    onFriendInviteDeepLinkConsumed: (Long) -> Unit
) {
    val navController = rememberNavController()
    val appAuthViewModel: AppAuthViewModel = viewModel(factory = appContainer.viewModelFactory)
    val friendInvitePreviewViewModel: FriendInvitePreviewViewModel = viewModel(factory = appContainer.viewModelFactory)
    val authState by appAuthViewModel.uiState.collectAsState()
    val friendInvitePreviewState = friendInvitePreviewViewModel.uiState
    val friendInviteFeedbackEvent = friendInvitePreviewViewModel.feedbackEvent
    var profileFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var pendingFriendInviteConfirmation by remember { mutableStateOf<FriendInviteRelationship?>(null) }
    var inviteFeedbackVisible by remember { mutableStateOf(false) }
    var inviteFeedbackMessage by remember { mutableStateOf("") }
    var inviteFeedbackIsSuccess by remember { mutableStateOf(false) }
    var inviteFeedbackEventId by remember { mutableStateOf(0) }

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

    LaunchedEffect(authState.isAuthenticated, pendingFriendInviteDeepLink?.sequence) {
        val friendInvite = pendingFriendInviteDeepLink
        if (friendInvite == null) return@LaunchedEffect

        pendingFriendInviteConfirmation = null
        if (authState.isAuthenticated) {
            friendInvitePreviewViewModel.show(friendInvite.username)
        }
        onFriendInviteDeepLinkConsumed(friendInvite.sequence)
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (!authState.isAuthenticated) {
            pendingFriendInviteConfirmation = null
            friendInvitePreviewViewModel.dismiss()
        }
    }

    LaunchedEffect(friendInviteFeedbackEvent?.sequence) {
        val event = friendInviteFeedbackEvent ?: return@LaunchedEffect
        inviteFeedbackMessage = event.message
        inviteFeedbackIsSuccess = event.isSuccess
        inviteFeedbackVisible = true
        inviteFeedbackEventId += 1
        friendInvitePreviewViewModel.clearFeedbackEvent()
    }

    LaunchedEffect(inviteFeedbackEventId) {
        if (inviteFeedbackEventId > 0) {
            delay(1_000)
            inviteFeedbackVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            val viewModel: WelcomeViewModel = viewModel(factory = appContainer.viewModelFactory)
            WelcomeScreen(
                viewModel = viewModel,
                onNavigateToSignUp = { navController.navigate(SolariRoute.Screen.SignUp.name) },
                onNavigateToSignIn = { navController.navigate(SolariRoute.Screen.SignIn.name) },
                onGoogleSignInComplete = {
                    appAuthViewModel.onSignedIn()
                    navController.navigate(SolariRoute.Screen.Main.name + "/0") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(SolariRoute.Screen.SignUp.name) {
            val viewModel: SignUpViewModel = viewModel(factory = appContainer.viewModelFactory)
            SignUpScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigate(SolariRoute.Screen.Welcome.name) {
                        popUpTo(SolariRoute.Screen.Welcome.name) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignIn = { navController.navigate(SolariRoute.Screen.SignIn.name) },
                onSignUpComplete = {
                    appAuthViewModel.onSignedIn()
                    navController.navigate(SolariRoute.Screen.Main.name + "/0") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(SolariRoute.Screen.SignIn.name) {
            val viewModel: SignInViewModel = viewModel(factory = appContainer.viewModelFactory)
            SignInScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigate(SolariRoute.Screen.Welcome.name) {
                        popUpTo(SolariRoute.Screen.Welcome.name) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
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
        composable(SolariRoute.Screen.OTP.name + "/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            val viewModel: OTPConfirmationViewModel = viewModel(factory = appContainer.viewModelFactory)
            OTPConfirmationScreen(
                viewModel = viewModel,
                email = email,
                onNavigateBack = { navController.popBackStack() },
                onConfirmComplete = { verifiedEmail ->
                    navController.navigate(
                        SolariRoute.Screen.CompletePasswordReset.name + "/${Uri.encode(verifiedEmail)}"
                    )
                }
            )
        }
        composable(SolariRoute.Screen.PasswordRecovery.name) {
            val viewModel: PasswordRecoveryViewModel = viewModel(factory = appContainer.viewModelFactory)
            PasswordRecoveryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onGetRecoveryCode = { email ->
                    navController.navigate(SolariRoute.Screen.OTP.name + "/${Uri.encode(email)}")
                }
            )
        }
        composable(SolariRoute.Screen.PasswordReset.name + "/{showTopBar}") { backStackEntry ->
            val showTopBar = backStackEntry.arguments?.getString("showTopBar")?.toBoolean() ?: true
            val viewModel: PasswordResetViewModel = viewModel(factory = appContainer.viewModelFactory)
            PasswordResetScreen(
                viewModel = viewModel,
                showTopBar = showTopBar,
                onNavigateBack = { navController.popBackStack() },
                onResetComplete = { 
                    if (showTopBar) {
                        profileFeedbackMessage = "Password changed successfully"
                        navController.popBackStack()
                    } else {
                        navController.navigate(SolariRoute.Screen.SignIn.name)
                    }
                }
            )
        }
        composable(SolariRoute.Screen.CompletePasswordReset.name + "/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            val viewModel: CompletePasswordResetViewModel = viewModel(factory = appContainer.viewModelFactory)
            CompletePasswordResetScreen(
                viewModel = viewModel,
                email = email,
                onNavigateBack = { navController.popBackStack() },
                onResetComplete = {
                    navController.navigate(SolariRoute.Screen.SignIn.name) {
                        popUpTo(SolariRoute.Screen.PasswordRecovery.name) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(
            route = SolariRoute.Screen.Main.name + "/{page}",
            exitTransition = {
                if (targetState.destination.route.isFriendManagementRoute()) {
                    ExitTransition.None
                } else {
                    null
                }
            },
            popEnterTransition = {
                if (initialState.destination.route.isFriendManagementRoute()) {
                    EnterTransition.None
                } else {
                    null
                }
            }
        ) { backStackEntry ->
            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 0
            MainScreen(
                initialPage = page,
                profileFeedbackMessage = profileFeedbackMessage,
                settingsViewModel = settingsViewModel,
                viewModelFactory = appContainer.viewModelFactory,
                onNavigateToChat = { conversation -> navController.navigateToChat(conversation) },
                onNavigateToManageFriends = { navController.navigate(SolariRoute.Screen.FriendManagement.name) },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToChangePassword = { navController.navigate(SolariRoute.Screen.PasswordReset.name + "/true") },
                onNavigateToChangeTheme = { navController.navigate(SolariRoute.Screen.ChangeTheme.name) },
                onNavigateToFeedBrowse = { authorId -> navController.navigateToFeedBrowse(authorId) },
                onCapture = { navController.navigate(SolariRoute.Screen.CameraAfter.name) },
                onLogout = {
                    navController.navigateToWelcomeAfterLogout(appAuthViewModel::signOutLocal)
                },
                onProfileFeedbackConsumed = { profileFeedbackMessage = null }
            )
        }
        composable(SolariRoute.Screen.Main.name + "/{page}/{postId}") { backStackEntry ->
            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 0
            val postId = backStackEntry.arguments?.getString("postId")
            MainScreen(
                initialPage = page,
                initialFeedPostId = postId,
                profileFeedbackMessage = profileFeedbackMessage,
                settingsViewModel = settingsViewModel,
                viewModelFactory = appContainer.viewModelFactory,
                onNavigateToChat = { conversation -> navController.navigateToChat(conversation) },
                onNavigateToManageFriends = { navController.navigate(SolariRoute.Screen.FriendManagement.name) },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToChangePassword = { navController.navigate(SolariRoute.Screen.PasswordReset.name + "/true") },
                onNavigateToChangeTheme = { navController.navigate(SolariRoute.Screen.ChangeTheme.name) },
                onNavigateToFeedBrowse = { authorId -> navController.navigateToFeedBrowse(authorId) },
                onCapture = { navController.navigate(SolariRoute.Screen.CameraAfter.name) },
                onLogout = {
                    navController.navigateToWelcomeAfterLogout(appAuthViewModel::signOutLocal)
                },
                onProfileFeedbackConsumed = { profileFeedbackMessage = null }
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
        composable(
            route = SolariRoute.Screen.FeedBrowse.name + "?authorId={authorId}",
            arguments = listOf(
                navArgument("authorId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialAuthorId = backStackEntry.arguments?.getString("authorId")
            val viewModel: FeedBrowseViewModel = viewModel(factory = appContainer.viewModelFactory)
            FeedBrowseScreen(
                viewModel = viewModel,
                initialAuthorId = initialAuthorId,
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

        AnimatedVisibility(
            visible = inviteFeedbackVisible,
            enter = slideInVertically(
                initialOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            SolariFeedbackPill(
                message = inviteFeedbackMessage,
                isSuccess = inviteFeedbackIsSuccess
            )
        }
    }

    if (friendInvitePreviewState.requestedUsername != null) {
        FriendInvitePreviewDialog(
            state = friendInvitePreviewState,
            onPrimaryAction = {
                when (friendInvitePreviewState.relationship) {
                    FriendInviteRelationship.Self -> Unit
                    FriendInviteRelationship.None -> friendInvitePreviewViewModel.sendFriendRequest()
                    FriendInviteRelationship.PendingOutgoing -> friendInvitePreviewViewModel.unsendFriendRequest()
                    FriendInviteRelationship.Friend,
                    FriendInviteRelationship.Blocked -> {
                        pendingFriendInviteConfirmation = friendInvitePreviewState.relationship
                    }
                }
            },
            onCancel = friendInvitePreviewViewModel::dismiss
        )
    }

    val invitedUser = friendInvitePreviewState.user
    if (pendingFriendInviteConfirmation == FriendInviteRelationship.Friend && invitedUser != null) {
        SolariConfirmationDialog(
            title = "Unfriend ${invitedUser.displayName}?",
            message = "They will be removed from your friends list.",
            confirmText = "Unfriend",
            onConfirm = {
                pendingFriendInviteConfirmation = null
                friendInvitePreviewViewModel.unfriend()
            },
            onDismiss = { pendingFriendInviteConfirmation = null }
        )
    }

    if (pendingFriendInviteConfirmation == FriendInviteRelationship.Blocked && invitedUser != null) {
        SolariConfirmationDialog(
            title = "Unblock ${invitedUser.displayName}?",
            message = "They will be able to find your profile and interact with visible content again.",
            confirmText = "Unblock",
            onConfirm = {
                pendingFriendInviteConfirmation = null
                friendInvitePreviewViewModel.unblock()
            },
            onDismiss = { pendingFriendInviteConfirmation = null }
        )
    }
}
