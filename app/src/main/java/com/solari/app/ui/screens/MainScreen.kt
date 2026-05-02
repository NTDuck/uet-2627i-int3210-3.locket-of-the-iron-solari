package com.solari.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.ViewModelProvider
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.Post
import com.solari.app.ui.viewmodels.*
import kotlinx.coroutines.launch
@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    initialPage: Int = 0,
    initialFeedPostId: String? = null,
    initialFeedPost: Post? = null,
    initialFeedPosts: List<Post>? = null,
    isInitialFeedSharedTransitionEnabled: Boolean = false,
    initialFeedAuthorFilterIds: Set<String> = emptySet(),
    initialFeedSort: String = "default",
    pageHistory: List<Int> = listOf(initialPage),
    onPageHistoryChange: (List<Int>) -> Unit = {},
    onCurrentPageChange: (Int) -> Unit = {},
    profileFeedbackMessage: String? = null,
    conversationFeedbackMessage: String? = null,
    settingsViewModel: SettingsViewModel,
    viewModelFactory: ViewModelProvider.Factory,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope? = null,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope? = null,
    onNavigateToChat: (Conversation) -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onNavigateToBlockedAccounts: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeTheme: () -> Unit,
    onNavigateToFeedBrowse: (String?) -> Unit,
    onNavigateBackFromFeedPost: () -> Unit = {},
    optimisticPostDraft: OptimisticPostDraft? = null,
    onOptimisticPostDraftConsumed: (String) -> Unit = {},
    onCapture: (CapturedMedia) -> Unit,
    onLogout: () -> Unit,
    onProfileFeedbackConsumed: () -> Unit = {},
    onConversationFeedbackConsumed: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }

    LaunchedEffect(initialPage) {
        if (pagerState.currentPage != initialPage) {
            pagerState.scrollToPage(initialPage)
            if (pageHistory.lastOrNull() != initialPage) {
                onPageHistoryChange(pageHistory + initialPage)
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onCurrentPageChange(pagerState.currentPage)
        if (pageHistory.lastOrNull() != pagerState.currentPage) {
            onPageHistoryChange(pageHistory + pagerState.currentPage)
        }
    }

    val scope = rememberCoroutineScope()
    var isFeedActivityPanelVisible by remember { mutableStateOf(false) }

    val routes = listOf(
        SolariRoute.Screen.CameraBefore,
        SolariRoute.Screen.Feed,
        SolariRoute.Screen.Conversations,
        SolariRoute.Screen.Profile
    )

    BackHandler(enabled = true) {
        if (initialFeedPostId != null &&
            pagerState.currentPage == 1
        ) {
            onNavigateBackFromFeedPost()
        } else if (pageHistory.size > 1) {
            val updatedHistory = pageHistory.dropLast(1)
            val previousPage = updatedHistory.last()
            onPageHistoryChange(updatedHistory)
            scope.launch { pagerState.animateScrollToPage(previousPage) }
        } else if (pagerState.currentPage != 0) {
            onPageHistoryChange(listOf(0))
            scope.launch { pagerState.animateScrollToPage(0) }
        } else {
            // No effect on HomepageBeforePreviewScreen (CameraBefore)
        }
    }

    val viewModelStoreOwner = androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner.current

    Scaffold(
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = routes[pagerState.currentPage].name,
                onNavigate = { routeName ->
                    val index = routes.indexOfFirst { it.name == routeName }
                    if (index != -1) {
                        if (index == 1 && viewModelStoreOwner != null) {
                            val feedViewModel: FeedViewModel = ViewModelProvider(viewModelStoreOwner, viewModelFactory)[FeedViewModel::class.java]
                            feedViewModel.resetFilters()
                        }
                        
                        if (
                            routeName == SolariRoute.Screen.Feed.name &&
                            pagerState.currentPage == 1
                        ) {
                            onNavigateToFeedBrowse(null)
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            userScrollEnabled = !isFeedActivityPanelVisible
        ) { page ->
            when (page) {
                0 -> {
                    val viewModel: HomepageBeforeCapturingViewModel = viewModel(factory = viewModelFactory)
                    HomepageBeforeCapturingScreen(
                        viewModel = viewModel,
                        onNavigateBack = {},
                        onCapture = onCapture,
                        onNavigateToFeed = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onNavigateToChat = { scope.launch { pagerState.animateScrollToPage(2) } },
                        onNavigateToProfile = { scope.launch { pagerState.animateScrollToPage(3) } }
                    )
                }
                1 -> {
                    val viewModel: FeedViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(optimisticPostDraft?.id) {
                        val draft = optimisticPostDraft ?: return@LaunchedEffect
                        viewModel.addOptimisticPost(draft)
                        onOptimisticPostDraftConsumed(draft.id)
                    }
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        FeedScreen(
                            viewModel = viewModel,
                            initialPostId = initialFeedPostId,
                            initialPost = initialFeedPost,
                            initialPosts = initialFeedPosts,
                            enableInitialSharedTransition = isInitialFeedSharedTransitionEnabled,
                            authorFilterIds = initialFeedAuthorFilterIds,
                            sortMode = initialFeedSort,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                            onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                            onNavigateToCamera = { scope.launch { pagerState.animateScrollToPage(0) } },
                            onNavigateToConversations = { scope.launch { pagerState.animateScrollToPage(2) } },
                            onNavigateToProfile = { scope.launch { pagerState.animateScrollToPage(3) } },
                            onNavigateToBrowse = onNavigateToFeedBrowse,
                            isFeedVisible = pagerState.currentPage == 1,
                            onActivityPanelVisibilityChanged = { isFeedActivityPanelVisible = it }
                        )
                    }
                }
                2 -> {
                    val viewModel: ConversationViewModel = viewModel(factory = viewModelFactory)
                    ConversationScreen(
                        viewModel = viewModel,
                        externalFeedbackMessage = conversationFeedbackMessage,
                        onExternalFeedbackConsumed = onConversationFeedbackConsumed,
                        onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToManageFriends = onNavigateToManageFriends,
                        onNavigateToCamera = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToFeed = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onNavigateToProfile = { scope.launch { pagerState.animateScrollToPage(3) } }
                    )
                }
                3 -> {
                    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
                    ProfileScreen(
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        externalFeedbackMessage = profileFeedbackMessage,
                        onExternalFeedbackConsumed = onProfileFeedbackConsumed,
                        onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onNavigateToChangeTheme = onNavigateToChangeTheme,
                        onNavigateToManageFriends = onNavigateToManageFriends,
                        onNavigateToCamera = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToFeed = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onNavigateToChat = { scope.launch { pagerState.animateScrollToPage(2) } },
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
