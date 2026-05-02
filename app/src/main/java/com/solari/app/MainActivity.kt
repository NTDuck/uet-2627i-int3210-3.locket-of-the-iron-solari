package com.solari.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.view.Display
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.channels.awaitClose
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.core.content.ContextCompat
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
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.viewmodels.*
import com.solari.app.ui.models.Conversation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FriendManagementTransitionMillis = 360
private const val SelectedConversationKey = "selected_conversation"
private const val ChatSettingsPartnerKey = "chat_settings_partner"
private const val CapturedMediaUriKey = "captured_media_uri"
private const val CapturedMediaTypeKey = "captured_media_type"
private const val CapturedMediaIsVideoKey = "captured_media_is_video"
private const val CapturedMediaDurationKey = "captured_media_duration"
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
        if (intent?.action == "com.solari.app.ACTION_EXIT") {
            finishAffinity()
            return
        }
        pendingFriendInviteDeepLink = intent.extractFriendInviteDeepLink()
        preferHighestRefreshRate()
        enableEdgeToEdge()
        setContent {
            val appContainer = (application as SolariApplication).appContainer
            val settingsViewModel: SettingsViewModel = viewModel(factory = appContainer.viewModelFactory)
            
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
        if (intent.action == "com.solari.app.ACTION_EXIT") {
            finishAffinity()
            return
        }
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
    val feedBrowseRoutePattern = SolariRoute.Screen.FeedBrowse.name + "?authorId={authorId}"
    val route = if (authorId.isNullOrBlank()) {
        SolariRoute.Screen.FeedBrowse.name
    } else {
        "${SolariRoute.Screen.FeedBrowse.name}?authorId=$authorId"
    }
    if (authorId.isNullOrBlank() &&
        (popBackStack(SolariRoute.Screen.FeedBrowse.name, false) ||
                popBackStack(feedBrowseRoutePattern, false))
    ) {
        return
    }
    navigate(route)
}

private fun NavController.navigateToFeedPost(
    postId: String,
    authorIds: Set<String>,
    sort: String
) {
    val queryParameters = buildList {
        if (authorIds.isNotEmpty()) {
            add("authorIds=${Uri.encode(authorIds.sorted().joinToString(","))}")
        }
        if (sort != "default") {
            add("sort=${Uri.encode(sort)}")
        }
    }
    val query = queryParameters.takeIf { it.isNotEmpty() }?.joinToString(
        separator = "&",
        prefix = "?"
    ).orEmpty()
    navigate("${SolariRoute.Screen.Main.name}/1/${Uri.encode(postId)}$query")
}

private fun String?.isFriendManagementRoute(): Boolean {
    return this?.startsWith(SolariRoute.Screen.FriendManagement.name) == true
}

private fun String?.toFeedAuthorFilterIds(): Set<String> {
    return this
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        .orEmpty()
}

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
private fun SolariApp(
    settingsViewModel: SettingsViewModel,
    appContainer: AppContainer,
    pendingFriendInviteDeepLink: FriendInviteDeepLink?,
    onFriendInviteDeepLinkConsumed: (Long) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appAuthViewModel: AppAuthViewModel = viewModel(factory = appContainer.viewModelFactory)
    val friendInvitePreviewViewModel: FriendInvitePreviewViewModel = viewModel(factory = appContainer.viewModelFactory)
    val authState by appAuthViewModel.uiState.collectAsState()
    val friendInvitePreviewState = friendInvitePreviewViewModel.uiState
    val friendInviteFeedbackEvent = friendInvitePreviewViewModel.feedbackEvent
    var profileFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var conversationFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var capturedMediaForPreview by remember { mutableStateOf<CapturedMedia?>(null) }
    var optimisticPostDraft by remember { mutableStateOf<OptimisticPostDraft?>(null) }
    var pendingFriendInviteConfirmation by remember { mutableStateOf<FriendInviteRelationship?>(null) }
    var inviteFeedbackVisible by remember { mutableStateOf(false) }
    var inviteFeedbackMessage by remember { mutableStateOf("") }
    var inviteFeedbackIsSuccess by remember { mutableStateOf(false) }
    var inviteFeedbackEventId by remember { mutableStateOf(0) }
    var internetFeedbackVisible by remember { mutableStateOf(false) }
    var internetFeedbackMessage by remember { mutableStateOf("") }
    var internetFeedbackIsSuccess by remember { mutableStateOf(false) }
    var internetFeedbackIsLoading by remember { mutableStateOf(false) }
    var internetFeedbackEventId by remember { mutableStateOf(0) }
    var notificationPermissionGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
        if (granted) {
            coroutineScope.launch {
                appContainer.pushNotificationCoordinator.preparePushToken()
                if (authState.isAuthenticated) {
                    appContainer.pushNotificationCoordinator.registerStoredDeviceIfAuthenticated()
                }
            }
        }
    }

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

    val startDestination = remember {
        if (authState.isAuthenticated) {
            SolariRoute.Screen.Main.name + "/0"
        } else {
            SolariRoute.Screen.Welcome.name
        }
    }

    fun acknowledgeSessionInvalidation() {
        capturedMediaForPreview = null
        optimisticPostDraft = null
        navController.navigate(SolariRoute.Screen.Welcome.name) {
            launchSingleTop = true
            popUpTo(0)
        }
        appAuthViewModel.acknowledgeSessionInvalidation()
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
            appContainer.webSocketManager.disconnect()
        } else {
            appContainer.webSocketManager.connect()
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

    LaunchedEffect(Unit) {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkChanges = callbackFlow {
            val callback = object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    trySend(Unit)
                }
                override fun onLost(network: android.net.Network) {
                    trySend(Unit)
                }
            }
            val request = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
        
        networkChanges.collectLatest {
            delay(1000)
            
            val request = okhttp3.Request.Builder()
                .url("${com.solari.app.BuildConfig.SOLARI_BACKEND_URL}health")
                .build()
                
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    appContainer.okHttpClient.newCall(request).execute().use { it.isSuccessful }
                }.getOrDefault(false)
            }
            
            if (!result) {
                internetFeedbackMessage = "No internet connection. Reconnecting..."
                internetFeedbackIsSuccess = false
                internetFeedbackIsLoading = true
                internetFeedbackVisible = true
                
                while(true) {
                    delay(1000)
                    val pollResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        runCatching {
                            appContainer.okHttpClient.newCall(request).execute().use { it.isSuccessful }
                        }.getOrDefault(false)
                    }
                    if (pollResult) {
                        internetFeedbackVisible = false
                        delay(400)
                        internetFeedbackMessage = "Reconnected successfully"
                        internetFeedbackIsSuccess = true
                        internetFeedbackIsLoading = false
                        internetFeedbackVisible = true
                        internetFeedbackEventId += 1
                        break
                    }
                }
            } else if (internetFeedbackVisible && !internetFeedbackIsSuccess) {
                internetFeedbackVisible = false
                delay(400)
                internetFeedbackMessage = "Reconnected successfully"
                internetFeedbackIsSuccess = true
                internetFeedbackIsLoading = false
                internetFeedbackVisible = true
                internetFeedbackEventId += 1
            }
        }
    }

    LaunchedEffect(internetFeedbackEventId) {
        if (internetFeedbackEventId > 0) {
            delay(2000)
            internetFeedbackVisible = false
        }
    }

    LaunchedEffect(authState.isCheckingSession, authState.isAuthenticated) {
        if (authState.isCheckingSession || !authState.isAuthenticated) return@LaunchedEffect

        notificationPermissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        if (!notificationPermissionGranted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !appContainer.pushNotificationCoordinator.hasRequestedNotificationPermission()
        ) {
            appContainer.pushNotificationCoordinator.markNotificationPermissionRequested()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(authState.isAuthenticated, notificationPermissionGranted) {
        if (authState.isAuthenticated && notificationPermissionGranted) {
            appContainer.pushNotificationCoordinator.registerStoredDeviceIfAuthenticated()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SharedTransitionLayout {
            NavHost(
                navController = navController,
                modifier = Modifier.background(SolariTheme.colors.background),
                startDestination = startDestination,
                enterTransition = {
                    if (initialState.destination.route?.contains("FeedBrowse") == true &&
                        targetState.destination.route?.contains("Main") == true) {
                        fadeIn(tween(500, delayMillis = 400))
                    } else {
                        slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                    }
                },
                exitTransition = {
                    if (initialState.destination.route?.contains("Main") == true &&
                        targetState.destination.route?.contains("FeedBrowse") == true) {
                        fadeOut(tween(500))
                    } else {
                        slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    }
                },
                popEnterTransition = {
                    if (initialState.destination.route?.contains("FeedBrowse") == true &&
                        targetState.destination.route?.contains("Main") == true) {
                        fadeIn(tween(500, delayMillis = 400))
                    } else if (initialState.destination.route?.contains("Main") == true &&
                        targetState.destination.route?.contains("FeedBrowse") == true) {
                        fadeIn(tween(500, delayMillis = 400))
                    } else {
                        slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                    }
                },
                popExitTransition = {
                    if (initialState.destination.route?.contains("Main") == true &&
                        targetState.destination.route?.contains("FeedBrowse") == true) {
                        fadeOut(tween(500))
                    } else {
                        slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    }
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
                        navController.navigateToWelcomeAfterLogout(appAuthViewModel::signOutLocal)
                    } else {
                        navController.navigate(SolariRoute.Screen.Welcome.name) {
                            popUpTo(SolariRoute.Screen.PasswordRecovery.name) {
                                inclusive = true
                            }
                        }
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
                conversationFeedbackMessage = conversationFeedbackMessage,
                settingsViewModel = settingsViewModel,
                viewModelFactory = appContainer.viewModelFactory,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@composable,
                onNavigateToChat = { conversation -> navController.navigateToChat(conversation) },
                onNavigateToManageFriends = { navController.navigate(SolariRoute.Screen.FriendManagement.name) },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToChangePassword = { navController.navigate(SolariRoute.Screen.PasswordReset.name + "/true") },
                onNavigateToChangeTheme = { navController.navigate(SolariRoute.Screen.ChangeTheme.name) },
                onNavigateToFeedBrowse = { authorId -> navController.navigateToFeedBrowse(authorId) },
                optimisticPostDraft = optimisticPostDraft,
                onOptimisticPostDraftConsumed = { consumedId ->
                    if (optimisticPostDraft?.id == consumedId) {
                        optimisticPostDraft = null
                    }
                },
                onCapture = { media ->
                    capturedMediaForPreview = media
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set(CapturedMediaUriKey, media.uri.toString())
                        set(CapturedMediaTypeKey, media.contentType)
                        set(CapturedMediaIsVideoKey, media.isVideo)
                        set(CapturedMediaDurationKey, media.durationMs)
                    }
                    navController.navigate(SolariRoute.Screen.CameraAfter.name)
                },
                onLogout = {
                    navController.navigateToWelcomeAfterLogout(appAuthViewModel::signOutLocal)
                },
                onProfileFeedbackConsumed = { profileFeedbackMessage = null },
                onConversationFeedbackConsumed = { conversationFeedbackMessage = null }
            )
        }
        composable(
            route = SolariRoute.Screen.Main.name + "/{page}/{postId}?authorIds={authorIds}&sort={sort}",
            enterTransition = { fadeIn(tween(500)) },
            exitTransition = { fadeOut(tween(500)) },
            arguments = listOf(
                navArgument("authorIds") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sort") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: 0
            val postId = backStackEntry.arguments?.getString("postId")
            val feedAuthorFilterIds = backStackEntry.arguments
                ?.getString("authorIds")
                .toFeedAuthorFilterIds()
            val feedSort = backStackEntry.arguments
                ?.getString("sort")
                ?.takeIf { it == "newest" || it == "oldest" }
                ?: "default"
            MainScreen(
                initialPage = page,
                initialFeedPostId = postId,
                initialFeedAuthorFilterIds = feedAuthorFilterIds,
                initialFeedSort = feedSort,
                profileFeedbackMessage = profileFeedbackMessage,
                conversationFeedbackMessage = conversationFeedbackMessage,
                settingsViewModel = settingsViewModel,
                viewModelFactory = appContainer.viewModelFactory,
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@composable,
                onNavigateToChat = { conversation -> navController.navigateToChat(conversation) },
                onNavigateToManageFriends = { navController.navigate(SolariRoute.Screen.FriendManagement.name) },
                onNavigateToBlockedAccounts = { navController.navigate(SolariRoute.Screen.BlockedAccounts.name) },
                onNavigateToChangePassword = { navController.navigate(SolariRoute.Screen.PasswordReset.name + "/true") },
                onNavigateToChangeTheme = { navController.navigate(SolariRoute.Screen.ChangeTheme.name) },
                onNavigateToFeedBrowse = { authorId -> navController.navigateToFeedBrowse(authorId) },
                onNavigateBackFromFeedPost = { navController.popBackStack() },
                optimisticPostDraft = optimisticPostDraft,
                onOptimisticPostDraftConsumed = { consumedId ->
                    if (optimisticPostDraft?.id == consumedId) {
                        optimisticPostDraft = null
                    }
                },
                onCapture = { media ->
                    capturedMediaForPreview = media
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set(CapturedMediaUriKey, media.uri.toString())
                        set(CapturedMediaTypeKey, media.contentType)
                        set(CapturedMediaIsVideoKey, media.isVideo)
                        set(CapturedMediaDurationKey, media.durationMs)
                    }
                    navController.navigate(SolariRoute.Screen.CameraAfter.name)
                },
                onLogout = {
                    navController.navigateToWelcomeAfterLogout(appAuthViewModel::signOutLocal)
                },
                onProfileFeedbackConsumed = { profileFeedbackMessage = null },
                onConversationFeedbackConsumed = { conversationFeedbackMessage = null }
            )
        }
        composable(
            SolariRoute.Screen.ImageEditing.name,
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(500)) }
        ) {
            val viewModel: ImageEditingViewModel = viewModel(factory = appContainer.viewModelFactory)
            val capturedMediaUri = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(CapturedMediaUriKey)
            val capturedMediaType = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(CapturedMediaTypeKey)
                ?: "image/jpeg"
            val capturedMediaIsVideo = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>(CapturedMediaIsVideoKey)
                ?: false
            val capturedMediaDuration = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Long>(CapturedMediaDurationKey)
            
            val initialMedia = capturedMediaForPreview ?: capturedMediaUri?.let { uriString ->
                CapturedMedia(
                    uri = Uri.parse(uriString),
                    contentType = capturedMediaType,
                    isVideo = capturedMediaIsVideo,
                    durationMs = capturedMediaDuration
                )
            }

            ImageEditingScreen(
                viewModel = viewModel,
                initialMedia = initialMedia,
                onNavigateBack = { editedMedia ->
                    if (editedMedia != null) {
                        capturedMediaForPreview = editedMedia
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set(CapturedMediaUriKey, editedMedia.uri.toString())
                            set(CapturedMediaTypeKey, editedMedia.contentType)
                            set(CapturedMediaIsVideoKey, editedMedia.isVideo)
                            set(CapturedMediaDurationKey, editedMedia.durationMs)
                        }
                    }
                    navController.popBackStack()
                }
            )
        }
        composable(SolariRoute.Screen.CameraAfter.name) {
            val viewModel: HomepageAfterCapturingViewModel = viewModel(factory = appContainer.viewModelFactory)
            val capturedMediaUri = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(CapturedMediaUriKey)
            val capturedMediaType = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(CapturedMediaTypeKey)
                ?: "image/jpeg"
            val capturedMediaIsVideo = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Boolean>(CapturedMediaIsVideoKey)
                ?: false
            val capturedMediaDuration = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Long>(CapturedMediaDurationKey)
            val routeCapturedMedia = capturedMediaForPreview ?: capturedMediaUri?.let { uriString ->
                CapturedMedia(
                    uri = Uri.parse(uriString),
                    contentType = capturedMediaType,
                    isVideo = capturedMediaIsVideo,
                    durationMs = capturedMediaDuration
                )
            }
            LaunchedEffect(
                routeCapturedMedia
            ) {
                viewModel.updateCapturedMedia(routeCapturedMedia)
            }
            HomepageAfterCapturingScreen(
                viewModel = viewModel,
                initialCapturedMedia = routeCapturedMedia,
                onNavigateBack = { navController.popBackStack() },
                onSend = { draft ->
                    optimisticPostDraft = draft
                    capturedMediaForPreview = null
                    navController.navigate(SolariRoute.Screen.Main.name + "/0") {
                        launchSingleTop = true
                        popUpTo(SolariRoute.Screen.CameraAfter.name) {
                            inclusive = true
                        }
                    }
                },
                onCancel = {
                    capturedMediaForPreview = null
                    navController.navigate(SolariRoute.Screen.Main.name + "/0") {
                        popUpTo(SolariRoute.Screen.Main.name + "/0") { inclusive = true }
                    }
                },
                onNavigateToEdit = {
                    val currentMedia = routeCapturedMedia
                    if (currentMedia != null) {
                        capturedMediaForPreview = currentMedia
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set(CapturedMediaUriKey, currentMedia.uri.toString())
                            set(CapturedMediaTypeKey, currentMedia.contentType)
                            set(CapturedMediaIsVideoKey, currentMedia.isVideo)
                            set(CapturedMediaDurationKey, currentMedia.durationMs)
                        }
                    }
                    navController.navigate(SolariRoute.Screen.ImageEditing.name)
                },
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
                sharedTransitionScope = this@SharedTransitionLayout,
                animatedVisibilityScope = this@composable,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFeed = { navController.navigate(SolariRoute.Screen.Main.name + "/1") },
                onNavigateToCamera = { navController.navigate(SolariRoute.Screen.Main.name + "/0") },
                onNavigateToChat = { navController.navigate(SolariRoute.Screen.Main.name + "/2") },
                onNavigateToProfile = { navController.navigate(SolariRoute.Screen.Main.name + "/3") },
                onNavigateToPost = { postId, authorIds, sort ->
                    navController.navigateToFeedPost(postId, authorIds, sort)
                }
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
                if (selectedConversation?.isDraft == true) {
                    viewModel.openDraftConversation(selectedConversation)
                } else {
                    viewModel.loadConversation(chatId)
                }
            }
            ChatScreen(
                chatId = chatId,
                initialPartner = selectedConversation?.otherUser,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.navigate(SolariRoute.Screen.Main.name + "/2") {
                        popUpTo(SolariRoute.Screen.Main.name + "/2") { inclusive = true }
                        launchSingleTop = true
                    }
                },
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
                onClearHistoryComplete = { message ->
                    conversationFeedbackMessage = message
                    navController.navigate(SolariRoute.Screen.Main.name + "/2") {
                        popUpTo(SolariRoute.Screen.Chat.name + "/$chatId") { inclusive = true }
                    }
                },
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
                onNavigateToConversation = { conversation -> navController.navigateToChat(conversation) },
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
        
        AnimatedVisibility(
            visible = internetFeedbackVisible,
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
                message = internetFeedbackMessage,
                isSuccess = internetFeedbackIsSuccess,
                isLoading = internetFeedbackIsLoading
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

    authState.sessionInvalidationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {},
            containerColor = SolariTheme.colors.surface,
            title = {
                Text(
                    text = "Sign in again",
                    color = SolariTheme.colors.onSurface
                )
            },
            text = {
                Text(
                    text = message,
                    color = SolariTheme.colors.tertiary
                )
            },
            confirmButton = {
                TextButton(onClick = ::acknowledgeSessionInvalidation) {
                    Text(
                        text = "OK",
                        color = SolariTheme.colors.primary
                    )
                }
            }
        )
    }
}
}
