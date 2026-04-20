package com.solari.app.ui.screens

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
import com.solari.app.ui.viewmodels.*
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    initialPage: Int = 0,
    initialFeedPostId: String? = null,
    settingsViewModel: SettingsViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onNavigateToBlockedAccounts: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeTheme: () -> Unit,
    onNavigateToFeedBrowse: () -> Unit,
    onCapture: () -> Unit,
    onLogout: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage) { 4 }
    val scope = rememberCoroutineScope()

    val routes = listOf(
        SolariRoute.Screen.CameraBefore,
        SolariRoute.Screen.Feed,
        SolariRoute.Screen.Conversations,
        SolariRoute.Screen.Profile
    )

    BackHandler(enabled = true) {
        if (pagerState.currentPage != 0) {
            scope.launch {
                pagerState.animateScrollToPage(0)
            }
        } else {
            // No effect on HomepageBeforePreviewScreen (CameraBefore)
        }
    }

    Scaffold(
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = routes[pagerState.currentPage].name,
                onNavigate = { routeName ->
                    val index = routes.indexOfFirst { it.name == routeName }
                    if (index != -1) {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> {
                    val viewModel: HomepageBeforeCapturingViewModel = viewModel()
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
                    val viewModel: FeedViewModel = viewModel()
                    FeedScreen(
                        viewModel = viewModel,
                        initialPostId = initialFeedPostId,
                        onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToCamera = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToChat = { scope.launch { pagerState.animateScrollToPage(2) } },
                        onNavigateToProfile = { scope.launch { pagerState.animateScrollToPage(3) } },
                        onNavigateToBrowse = onNavigateToFeedBrowse
                    )
                }
                2 -> {
                    val viewModel: ConversationViewModel = viewModel()
                    ConversationScreen(
                        viewModel = viewModel,
                        onNavigateBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToManageFriends = onNavigateToManageFriends,
                        onNavigateToCamera = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNavigateToFeed = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onNavigateToProfile = { scope.launch { pagerState.animateScrollToPage(3) } }
                    )
                }
                3 -> {
                    val viewModel: ProfileViewModel = viewModel()
                    ProfileScreen(
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
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
