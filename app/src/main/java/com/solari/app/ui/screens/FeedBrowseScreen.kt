package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.FeedBrowseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedBrowseScreen(
    viewModel: FeedBrowseViewModel,
    initialAuthorId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPost: (postId: String, authorIds: Set<String>, sort: String) -> Unit
) {
    val posts = viewModel.posts
    val friends = viewModel.friends
    val currentUser = viewModel.currentUser
    var selectedSort by remember { mutableStateOf("default") }
    var selectedFriendIds by remember(initialAuthorId) {
        mutableStateOf(initialAuthorId?.let { setOf(it) } ?: emptySet())
    }
    var isUserRefreshing by remember { mutableStateOf(false) }
    val visibleFriends = remember(friends, currentUser?.id) {
        friends.filterNot { it.id == currentUser?.id }
    }
    
    val filteredSortedPosts = remember(posts, selectedSort, selectedFriendIds) {
        val filteredPosts = if (selectedFriendIds.isEmpty()) {
            posts
        } else {
            posts.filter { it.author.id in selectedFriendIds }
        }

        when (selectedSort) {
            "newest" -> filteredPosts.sortedByDescending { it.timestamp }
            "oldest" -> filteredPosts.sortedBy { it.timestamp }
            else -> filteredPosts
        }
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
        }
    }

    Scaffold(
        containerColor = SolariTheme.colors.background,
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = SolariRoute.Screen.Feed.name,
                onNavigate = { routeName ->
                    when (routeName) {
                        SolariRoute.Screen.CameraBefore.name -> onNavigateToCamera()
                        SolariRoute.Screen.Feed.name -> onNavigateBack()
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
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "SORT",
                    fontSize = 12.sp * 1.4f,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortChip("default", isSelected = selectedSort == "default") { selectedSort = "default" }
                    SortChip("newest", isSelected = selectedSort == "newest") { selectedSort = "newest" }
                    SortChip("oldest", isSelected = selectedSort == "oldest") { selectedSort = "oldest" }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "FILTER BY FRIENDS",
                    fontSize = 12.sp * 1.4f,
                    fontWeight = FontWeight.Bold,
                    color = SolariTheme.colors.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val isAllSelected = selectedFriendIds.isEmpty()
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .scaledClickable(pressedScale = 1.1f) { selectedFriendIds = emptySet() }
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
                                    tint = if (isAllSelected) SolariTheme.colors.primary else Color.Gray
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
                                            selectedFriendIds = if (isSelected) {
                                                selectedFriendIds - user.id
                                            } else {
                                                selectedFriendIds + user.id
                                            }
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
                                        selectedFriendIds = if (isSelected) {
                                            selectedFriendIds - friend.id
                                        } else {
                                            selectedFriendIds + friend.id
                                        }
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

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredSortedPosts) { post ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .scaledClickable(pressedScale = 0.9f) {
                                    viewModel.registerPostView(post.id)
                                    onNavigateToPost(post.id, selectedFriendIds, selectedSort)
                                }
                                .clip(RoundedCornerShape(8.dp))
                                .background(SolariTheme.colors.surface)
                        ) {
                            AsyncImage(
                                model = post.thumbnailUrl.ifBlank { post.imageUrl },
                                contentDescription = "Browse Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SortChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.surface,
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) SolariTheme.colors.onPrimary else SolariTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
