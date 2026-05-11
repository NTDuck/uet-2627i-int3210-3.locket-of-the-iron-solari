package com.solari.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.Post
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.ConversationViewModel
import com.solari.app.ui.viewmodels.FeedViewModel
import com.solari.app.ui.viewmodels.HomepageBeforeCapturingViewModel
import com.solari.app.ui.viewmodels.ProfileViewModel
import com.solari.app.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)
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

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
            .navigationBarsPadding()
            .padding(bottom = 59.dp),
        userScrollEnabled = !isFeedActivityPanelVisible
    ) { page ->
            when (page) {
                0 -> {
                    val viewModel: HomepageBeforeCapturingViewModel =
                        viewModel(factory = viewModelFactory)
                    HomepageBeforeCapturingScreen(
                        viewModel = viewModel,
                        onCapture = onCapture
                    )
                }

                1 -> {
                    val viewModel: FeedViewModel = viewModel(factory = viewModelFactory)
                    LaunchedEffect(optimisticPostDraft?.id) {
                        val draft = optimisticPostDraft ?: return@LaunchedEffect
                        viewModel.addOptimisticPost()
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
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToManageFriends = onNavigateToManageFriends
                    )
                }

                3 -> {
                    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
                    ProfileScreen(
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        externalFeedbackMessage = profileFeedbackMessage,
                        onExternalFeedbackConsumed = onProfileFeedbackConsumed,
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onNavigateToChangeTheme = onNavigateToChangeTheme,
                        onNavigateToManageFriends = onNavigateToManageFriends,
                        onLogout = onLogout
                    )
            }
        }
    }
}
