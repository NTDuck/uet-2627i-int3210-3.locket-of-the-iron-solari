package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.R
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.FeedViewModel

private data class FeedActivityEntry(
    val user: User,
    val emoji: String,
    val caption: String?
)

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    initialPostId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBrowse: () -> Unit
) {
    var showMenuForPost by remember { mutableStateOf<Post?>(null) }
    var isActivitySheetVisible by remember { mutableStateOf(false) }
    var activitySheetEntries by remember { mutableStateOf<List<FeedActivityEntry>>(emptyList()) }
    val posts = viewModel.posts
    val currentUser = viewModel.currentUser
    val users = viewModel.users
    val initialPostPage = remember(initialPostId, posts) {
        posts.indexOfFirst { it.id == initialPostId }.takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(initialPage = initialPostPage) { posts.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
    ) {
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.errorMessage ?: if (viewModel.isLoading) "Loading feed" else "No posts yet",
                    color = SolariTheme.colors.onBackground,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isActivitySheetVisible
            ) { page ->
                FeedPost(
                    post = posts[page],
                    onLongPress = { showMenuForPost = posts[page] },
                    onMoreClick = { showMenuForPost = posts[page] },
                    onShowActivity = { activities ->
                        activitySheetEntries = activities
                        isActivitySheetVisible = true
                    },
                    onNavigateToBrowse = onNavigateToBrowse,
                    currentUser = currentUser,
                    users = users
                )
            }
        }

        FeedActivitySheet(
            visible = isActivitySheetVisible,
            activities = activitySheetEntries,
            onDismiss = { isActivitySheetVisible = false }
        )

        if (showMenuForPost != null) {
            val post = showMenuForPost!!
            AlertDialog(
                onDismissRequest = { showMenuForPost = null },
                confirmButton = {},
                dismissButton = {},
                containerColor = SolariTheme.colors.surface,
                modifier = Modifier.fillMaxWidth(0.9f),
                text = {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        TextButton(
                            onClick = { showMenuForPost = null },
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        ) {
                            Text("Download", color = SolariTheme.colors.onSurface, fontSize = 18.sp)
                        }
                        if (currentUser != null && post.author.id == currentUser.id) {
                            TextButton(
                                onClick = {
                                    viewModel.deletePost(post.id)
                                    showMenuForPost = null
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp)
                            ) {
                                Text("Delete", color = Color.Red, fontSize = 18.sp)
                            }
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedPost(
    post: Post,
    onLongPress: () -> Unit,
    onMoreClick: () -> Unit,
    onShowActivity: (List<FeedActivityEntry>) -> Unit,
    onNavigateToBrowse: () -> Unit,
    currentUser: User?,
    users: List<User>
) {
    val focusManager = LocalFocusManager.current
    var messageText by remember { mutableStateOf("") }
    val currentUserId = currentUser?.id
    val isCurrentUserPost = post.author.id == currentUserId
    val activityUsers = remember(users, currentUserId) {
        users
            .filter { it.id != currentUserId }
            .take(3)
    }
    val activityEntries = remember(post.id, currentUserId, users) {
        buildFeedActivityEntries(
            currentUserId = currentUserId,
            users = users
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.55f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongPress
                    )
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                )

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.36f))
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = post.caption.ifBlank { "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit." },
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontFamily = PlusJakartaSans,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.58f))
                        .padding(horizontal = 28.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 100.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.author.profileImageUrl,
                    contentDescription = "Author Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isCurrentUserPost) "You" else post.author.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "2 HOURS AGO",
                        color = Color(0xFFD7C0B2),
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isCurrentUserPost) {
                FeedActivityPill(
                    avatarUrls = activityUsers.mapNotNull { it.profileImageUrl },
                    overflowCount = activityEntries.size - activityUsers.size,
                    onClick = { onShowActivity(activityEntries) }
                )
            } else {
                FeedReactionField()

                Spacer(modifier = Modifier.height(14.dp))

                FeedMessageField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        messageText = ""
                        focusManager.clearFocus()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FeedBrowseButton(onClick = onNavigateToBrowse)

            Spacer(modifier = Modifier.weight(0.45f))
        }

    }
}

@Composable
private fun FeedActivityPill(
    avatarUrls: List<String>,
    overflowCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(64.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFF34363B))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Activity",
            color = Color(0xFFE3E2E6),
            fontSize = 18.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(105.dp)
                .height(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val visibleAvatarUrls = avatarUrls.take(3)
            visibleAvatarUrls.forEachIndexed { index, avatarUrl ->
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (index * 23).dp)
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = (visibleAvatarUrls.size * 23).dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5A5C62)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${overflowCount.coerceAtLeast(0)}",
                    color = Color(0xFFD0D0D4),
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FeedActivitySheet(
    visible: Boolean,
    activities: List<FeedActivityEntry>,
    onDismiss: () -> Unit
) {
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    val handleInteractionSource = remember { MutableInteractionSource() }
    var handleDragDistance by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = dismissInteractionSource,
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 260),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220),
                targetOffsetY = { it }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.68f)
                    .clip(RoundedCornerShape(topStart = 29.dp, topEnd = 29.dp))
                    .background(SolariTheme.colors.surface)
                    .clickable(
                        interactionSource = sheetInteractionSource,
                        indication = null,
                        onClick = {}
                    )
                    .padding(top = 22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(58.dp)
                        .height(18.dp)
                        .clickable(
                            interactionSource = handleInteractionSource,
                            indication = null,
                            onClick = onDismiss
                        )
                        .pointerInput(onDismiss) {
                            detectVerticalDragGestures(
                                onDragStart = { handleDragDistance = 0f },
                                onVerticalDrag = { _, dragAmount ->
                                    handleDragDistance += dragAmount
                                    if (handleDragDistance > 20f) {
                                        onDismiss()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SolariTheme.colors.onSurface.copy(alpha = 0.28f))
                    )
                }

                Text(
                    text = "Activity",
                    color = SolariTheme.colors.onSurface,
                    fontSize = 19.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 22.dp, bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(start = 19.dp, end = 19.dp, bottom = 26.dp)
                ) {
                    items(activities) { activity ->
                        FeedActivityItem(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedActivityItem(activity: FeedActivityEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = activity.user.profileImageUrl,
            contentDescription = "${activity.user.displayName} avatar",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.user.displayName,
                color = SolariTheme.colors.onSurface,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Viewed! ✨",
                color = SolariTheme.colors.onSurface.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.widthIn(max = 102.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (activity.caption != null) {
                Text(
                    text = activity.caption,
                    color = SolariTheme.colors.secondary,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = activity.emoji,
                fontSize = 22.sp,
                lineHeight = 24.sp
            )
        }
    }
}

private fun buildFeedActivityEntries(
    currentUserId: String?,
    users: List<User>
): List<FeedActivityEntry> {
    val reactions = listOf(
        "🫶" to "Loved this",
        "🍊" to null,
        "🔥" to "So good",
        "❤️" to "Miss this",
        "☀️" to null,
        "☺️" to "Clean shot",
        "✨" to "Nice",
        "😮" to null,
        "👏" to "Perfect timing",
        "💫" to "Great mood"
    )

    return users
        .filter { it.id != currentUserId }
        .take(10)
        .mapIndexed { index, user ->
            val reaction = reactions[index % reactions.size]
            FeedActivityEntry(
                user = user,
                emoji = reaction.first,
                caption = reaction.second
            )
        }
}

@Composable
private fun FeedReactionField() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF1B1C21))
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Send reaction...",
            color = Color(0xFF9699A1),
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            modifier = Modifier.weight(1f)
        )
        Text("🔥", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 14.sp)
        Text("❤", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 14.sp)
        Text("☀", modifier = Modifier.padding(horizontal = 10.dp), fontSize = 14.sp)
        Text("☺", modifier = Modifier.padding(start = 10.dp), fontSize = 16.sp)
    }
}

@Composable
private fun FeedMessageField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF1B1C21))
            .padding(start = 26.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans
            ),
            cursorBrush = SolidColor(Color.White),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Send message...",
                            color = Color(0xFF9699A1),
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans
                        )
                    }
                    innerTextField()
                }
            }
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send message",
            tint = Color(0xFFD7C0B2),
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onSend)
        )
    }
}

@Composable
private fun FeedBrowseButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(48.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF080B0E))
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "View all photos",
            color = Color(0xFFD7C0B2),
            fontSize = 16.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}
