package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.FilterToggleButton
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.components.SortSelection
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.FeedBrowseViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val FeedBrowseScrollTopButtonThresholdIndex = 24
private const val FeedBrowseGridColumnCount = 3
private val FeedBrowseThumbnailCornerRadius = 8.dp


@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)
@Composable
fun FeedBrowseScreen(
    viewModel: FeedBrowseViewModel,
    initialAuthorId: String? = null,
    onNavigateToFeed: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPost: (post: Post, posts: List<Post>, authorIds: Set<String>, sort: String, enableSharedTransition: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val posts = viewModel.posts
    val friends = viewModel.friends
    val currentUser = viewModel.currentUser
    val selectedSort = viewModel.selectedSort
    val selectedFriendIds = viewModel.selectedFriendIds
    var isUserRefreshing by remember { mutableStateOf(false) }
    val feedListState = rememberLazyGridState(
        initialFirstVisibleItemIndex = viewModel.feedListFirstVisibleItemIndex,
        initialFirstVisibleItemScrollOffset = viewModel.feedListFirstVisibleItemScrollOffset
    )
    val showScrollTopButton by remember {
        derivedStateOf {
            feedListState.firstVisibleItemIndex >= FeedBrowseScrollTopButtonThresholdIndex
        }
    }
    val visibleFriends = remember(friends, currentUser?.id) {
        friends.filterNot { it.id == currentUser?.id }
    }

    val context = LocalContext.current

    LaunchedEffect(initialAuthorId) {
        viewModel.applyInitialAuthorFilter(initialAuthorId)
    }

    androidx.activity.compose.BackHandler {
        onNavigateToFeed()
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = feedListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 && lastVisibleIndex >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore, viewModel.isLoading) {
        if (shouldLoadMore && !viewModel.isLoading) {
            viewModel.loadNextPage()
        }
    }

    fun scrollFeedListToTop() {
        viewModel.resetFeedListScroll()
        coroutineScope.launch {
            feedListState.scrollToItem(0)
        }
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
        }
    }

    LaunchedEffect(feedListState) {
        snapshotFlow { feedListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 6) {
                    viewModel.loadNextPage()
                }
            }
    }

    LaunchedEffect(feedListState) {
        snapshotFlow {
            feedListState.firstVisibleItemIndex to feedListState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (firstVisibleItemIndex, firstVisibleItemScrollOffset) ->
                viewModel.updateFeedListScroll(
                    firstVisibleItemIndex = firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = firstVisibleItemScrollOffset
                )
            }
    }

    val currentSortSelection = when (selectedSort) {
        "oldest" -> SortSelection.Oldest
        else -> SortSelection.Newest
    }

    fun navigateToFirstGridPost() {
        val firstPost = posts.firstOrNull()
        if (firstPost == null) {
            onNavigateToFeed()
            return
        }

        if (firstPost.uploadStatus == PostUploadStatus.None) {
            viewModel.registerPostView(firstPost.id)
        }
        viewModel.updateFeedListScroll(
            firstVisibleItemIndex = feedListState.firstVisibleItemIndex,
            firstVisibleItemScrollOffset = feedListState.firstVisibleItemScrollOffset
        )
        onNavigateToPost(firstPost, posts, selectedFriendIds, selectedSort, false)
    }

    Scaffold(
        containerColor = SolariTheme.colors.background,
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = SolariRoute.Screen.Feed.name,
                onNavigate = { routeName ->
                    when (routeName) {
                        SolariRoute.Screen.CameraBefore.name -> onNavigateToCamera()
                        SolariRoute.Screen.Feed.name -> navigateToFirstGridPost()
                        SolariRoute.Screen.Conversations.name -> onNavigateToChat()
                        SolariRoute.Screen.Profile.name -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isUserRefreshing,
            onRefresh = {
                isUserRefreshing = true
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "POSTS",
                        fontSize = 12.sp * 1.4f,
                        fontWeight = FontWeight.Bold,
                        color = SolariTheme.colors.secondary
                    )
                    FilterToggleButton(
                        selected = currentSortSelection,
                        onToggle = { selection ->
                            viewModel.updateSelectedSort(selection.apiValue ?: "newest")
                            scrollFeedListToTop()
                        },
                        iconTint = SolariTheme.colors.secondary,
                        modifier = Modifier.size(28.dp),
                        iconSize = 18
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().graphicsLayer { clip = false }
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 12.dp)) {
                            val isAllSelected = selectedFriendIds.isEmpty()
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .scaledClickable(pressedScale = 1.1f) {
                                        viewModel.clearFriendFilters()
                                        scrollFeedListToTop()
                                    }
                                    .clip(CircleShape)
                                    .border(
                                        width = 2.dp,
                                        color = if (isAllSelected) SolariTheme.colors.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .background(SolariTheme.colors.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = "All",
                                    tint = if (isAllSelected) SolariTheme.colors.primary else SolariTheme.colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "All (${visibleFriends.size + if (currentUser != null) 1 else 0})",
                                color = if (isAllSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    currentUser?.let { user ->
                        item {
                            val isSelected = user.id in selectedFriendIds
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                SolariAvatar(
                                    imageUrl = user.profileImageUrl,
                                    username = user.username,
                                    contentDescription = "You",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .scaledClickable(pressedScale = 1.1f) {
                                            viewModel.toggleFriendFilter(user.id)
                                            scrollFeedListToTop()
                                        }
                                        .border(
                                            width = 2.dp,
                                            color = if (isSelected) SolariTheme.colors.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .padding(4.dp)
                                        .clip(CircleShape),
                                    shape = CircleShape,
                                    fontSize = 22.sp
                                )
                                Text(
                                    "You",
                                    color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    items(visibleFriends) { friend ->
                        val isSelected = friend.id in selectedFriendIds
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SolariAvatar(
                                imageUrl = friend.profileImageUrl,
                                username = friend.username,
                                contentDescription = friend.displayName,
                                modifier = Modifier
                                    .size(64.dp)
                                    .scaledClickable(pressedScale = 1.1f) {
                                        viewModel.toggleFriendFilter(friend.id)
                                        scrollFeedListToTop()
                                    }
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) SolariTheme.colors.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                shape = CircleShape,
                                fontSize = 22.sp
                            )
                            Text(
                                friend.displayName,
                                color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
                                fontSize = 12.sp,
                                maxLines = 2,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        state = feedListState,
                        columns = GridCells.Fixed(FeedBrowseGridColumnCount),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp)
                    ) {
                        items(posts, key = { post -> post.id }) { post ->
                            val thumbnailUrl = post.thumbnailUrl.ifBlank { post.imageUrl }

                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .aspectRatio(1f)
                                    .scaledClickable(
                                        pressedScale = 0.9f
                                    ) {
                                        if (post.uploadStatus == PostUploadStatus.None) {
                                            viewModel.registerPostView(post.id)
                                        }
                                        viewModel.updateFeedListScroll(
                                            firstVisibleItemIndex = feedListState.firstVisibleItemIndex,
                                            firstVisibleItemScrollOffset = feedListState.firstVisibleItemScrollOffset
                                        )
                                        onNavigateToPost(
                                            post,
                                            posts,
                                            selectedFriendIds,
                                            selectedSort,
                                            false
                                        )
                                    }
                                    .clip(RoundedCornerShape(FeedBrowseThumbnailCornerRadius))
                                    .background(SolariTheme.colors.surface)
                            ) {
                                if (thumbnailUrl.isNotBlank()) {
                                    val thumbnailRequest =
                                        remember(context, post.id, thumbnailUrl) {
                                            ImageRequest.Builder(context)
                                                .data(thumbnailUrl)
                                                .memoryCacheKey("thumb_${post.id}")
                                                .crossfade(false)
                                                .build()
                                        }

                                    AsyncImage(
                                        model = thumbnailRequest,
                                        contentDescription = "Browse Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                when (post.uploadStatus) {
                                    PostUploadStatus.Uploading,
                                    PostUploadStatus.Processing -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(SolariTheme.colors.onSurface.copy(alpha = 0.18f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = SolariTheme.colors.onBackground,
                                                trackColor = SolariTheme.colors.onBackground.copy(
                                                    alpha = 0.18f
                                                ),
                                                modifier = Modifier.size(24.dp),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }

                                    PostUploadStatus.Failed -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(SolariTheme.colors.onSurface.copy(alpha = 0.36f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Failed",
                                                color = SolariTheme.colors.onBackground,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    PostUploadStatus.None -> Unit
                                }
                            }
                        }

                        if (viewModel.isFetchingNextPage) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = SolariTheme.colors.primary,
                                        trackColor = SolariTheme.colors.surface,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                    }

                    if (showScrollTopButton) {
                        FeedBrowseScrollTopButton(
                            onClick = ::scrollFeedListToTop,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedBrowseScrollTopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = SolariTheme.colors.surface.copy(alpha = 0.94f),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                tint = SolariTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Top",
                color = SolariTheme.colors.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
