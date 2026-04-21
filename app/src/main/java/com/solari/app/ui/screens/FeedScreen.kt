package com.solari.app.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.solari.app.R
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.FeedViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    initialPostId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBrowse: () -> Unit,
    onActivityPanelVisibilityChanged: (Boolean) -> Unit = {}
) {
    var showMenuForPost by remember { mutableStateOf<Post?>(null) }
    var postPendingDelete by remember { mutableStateOf<Post?>(null) }
    var isActivitySheetVisible by remember { mutableStateOf(false) }
    var activitySheetPostId by remember { mutableStateOf<String?>(null) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    val posts = viewModel.posts
    val currentUser = viewModel.currentUser
    val initialPostPage = remember(initialPostId, posts) {
        posts.indexOfFirst { it.id == initialPostId }.takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(initialPage = initialPostPage) { posts.size }

    LaunchedEffect(initialPostId, posts) {
        if (initialPostId == null || posts.isEmpty()) return@LaunchedEffect

        val requestedPostPage = posts.indexOfFirst { it.id == initialPostId }
        if (requestedPostPage >= 0 && pagerState.currentPage != requestedPostPage) {
            pagerState.scrollToPage(requestedPostPage)
        }
    }

    LaunchedEffect(isActivitySheetVisible) {
        onActivityPanelVisibilityChanged(isActivitySheetVisible)
    }

    LaunchedEffect(viewModel.successMessage) {
        val message = viewModel.successMessage ?: return@LaunchedEffect
        feedbackPillMessage = message
        feedbackPillVisible = true
        delay(2_000)
        feedbackPillVisible = false
        delay(260)
        viewModel.clearMessages()
    }

    LaunchedEffect(pagerState.currentPage, posts, currentUser?.id) {
        val post = posts.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        val currentUserId = currentUser?.id ?: return@LaunchedEffect
        if (post.author.id == currentUserId) {
            viewModel.loadPostActivity(post.id)
        } else {
            viewModel.registerPostView(post)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onActivityPanelVisibilityChanged(false)
        }
    }

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
                when {
                    viewModel.isLoading -> CircularProgressIndicator(
                        color = SolariTheme.colors.primary,
                        trackColor = SolariTheme.colors.surface
                    )

                    else -> Text(
                        text = viewModel.errorMessage ?: "No posts yet",
                        color = SolariTheme.colors.onBackground,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isActivitySheetVisible
            ) { page ->
                FeedPost(
                    post = posts[page],
                    isActive = page == pagerState.currentPage,
                    onLongPress = { showMenuForPost = posts[page] },
                    onMoreClick = { showMenuForPost = posts[page] },
                    activityEntries = viewModel.postActivities[posts[page].id].orEmpty(),
                    onShowActivity = {
                        activitySheetPostId = posts[page].id
                        viewModel.loadPostActivity(posts[page].id, force = true)
                        isActivitySheetVisible = true
                    },
                    onSendPostReaction = { emoji, note, onSent ->
                        viewModel.sendPostReaction(posts[page], emoji, note, onSent)
                    },
                    onSendPostReply = { content, onSent ->
                        viewModel.sendPostReply(posts[page], content, onSent)
                    },
                    onNavigateToBrowse = onNavigateToBrowse,
                    currentUser = currentUser
                )
            }
        }

        val currentActivitySheetPostId = activitySheetPostId
        FeedActivitySheet(
            visible = isActivitySheetVisible,
            activities = currentActivitySheetPostId
                ?.let { viewModel.postActivities[it] }
                .orEmpty(),
            isLoading = currentActivitySheetPostId in viewModel.loadingPostActivityIds,
            onDismiss = { isActivitySheetVisible = false }
        )

        if (showMenuForPost != null) {
            val post = showMenuForPost!!
            Dialog(
                onDismissRequest = { showMenuForPost = null },
            ) {
                Surface(
                    color = SolariTheme.colors.surface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier.width(220.dp)
                ) {
                    Column {
                        val canDeletePost = currentUser != null && post.author.id == currentUser.id
                        FeedPostActionButton(
                            text = "Download",
                            textColor = SolariTheme.colors.onSurface,
                            shape = if (canDeletePost) {
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            } else {
                                RoundedCornerShape(14.dp)
                            },
                            onClick = { showMenuForPost = null }
                        )

                        if (canDeletePost) {
                            FeedPostActionButton(
                                text = "Delete",
                                textColor = Color(0xFFE57373),
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 14.dp,
                                    bottomStart = 14.dp
                                ),
                                onClick = {
                                    postPendingDelete = post
                                    showMenuForPost = null
                                }
                            )
                        }
                    }
                }
            }
        }

        postPendingDelete?.let { post ->
            SolariConfirmationDialog(
                title = "Delete post?",
                message = "This post will be removed from your feed.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deletePost(post.id)
                    postPendingDelete = null
                },
                onDismiss = { postPendingDelete = null }
            )
        }

        AnimatedVisibility(
            visible = feedbackPillVisible,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 260),
                initialOffsetY = { -it * 2 }
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220),
                targetOffsetY = { -it * 2 }
            ) + fadeOut(animationSpec = tween(durationMillis = 160)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 24.dp, end = 24.dp)
        ) {
            FeedFeedbackPill(message = feedbackPillMessage)
        }
    }
}

@Composable
private fun FeedFeedbackPill(message: String) {
    Surface(
        color = Color(0xFF163624),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF77E0A1),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeedPostActionButton(
    text: String,
    textColor: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedPost(
    post: Post,
    isActive: Boolean,
    onLongPress: () -> Unit,
    onMoreClick: () -> Unit,
    activityEntries: List<PostActivityEntry>,
    onShowActivity: () -> Unit,
    onSendPostReaction: (String, String?, () -> Unit) -> Unit,
    onSendPostReply: (String, () -> Unit) -> Unit,
    onNavigateToBrowse: () -> Unit,
    currentUser: User?
) {
    val focusManager = LocalFocusManager.current
    var messageText by remember { mutableStateOf("") }
    var reactionNote by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val currentUserId = currentUser?.id
    val isCurrentUserPost = post.author.id == currentUserId
    val displayAuthor = currentUser?.takeIf { post.author.id == it.id } ?: post.author
    val activityUsers = remember(activityEntries, currentUserId) {
        activityEntries
            .map { it.user }
            .distinctBy { it.id }
            .filter { it.id != currentUserId }
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
                if (post.isVideoMedia()) {
                    FeedVideoPlayer(
                        url = post.imageUrl,
                        mediaType = post.mediaType,
                        isActive = isActive,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FeedImage(
                        url = post.imageUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f))
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.36f))
                        .clickable(onClick = onMoreClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (!post.caption.isEmpty()) {
                    Text(
                        text = post.caption,
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
            }

            Spacer(modifier = Modifier.height(36.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 100.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SolariAvatar(
                    imageUrl = displayAuthor.profileImageUrl,
                    username = displayAuthor.username,
                    contentDescription = "Author Avatar",
                    modifier = Modifier
                        .size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isCurrentUserPost) "You" else displayAuthor.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = post.timestamp.toFeedRelativeTimeLabel(),
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
                    users = activityUsers.take(3),
                    overflowCount = (activityUsers.size - 3).coerceAtLeast(0),
                    onClick = onShowActivity
                )
            } else {
                FeedReactionField(
                    value = reactionNote,
                    onValueChange = { reactionNote = it.take(20) },
                    onReact = { emoji ->
                        onSendPostReaction(emoji, reactionNote.trim().takeIf { it.isNotEmpty() }) {
                            reactionNote = ""
                            focusManager.clearFocus()
                        }
                    },
                    onOpenEmojiPicker = { showEmojiPicker = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                FeedMessageField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        val content = messageText.trim()
                        if (content.isNotEmpty()) {
                            onSendPostReply(content) {
                                messageText = ""
                                focusManager.clearFocus()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FeedBrowseButton(onClick = onNavigateToBrowse)

            Spacer(modifier = Modifier.weight(0.45f))
        }

        if (showEmojiPicker) {
            EmojiPickerPopup(
                onClosed = { showEmojiPicker = false },
                selectedEmoji = null,
                recentEmojis = emptyList(),
                onReact = { emoji ->
                    onSendPostReaction(emoji, reactionNote.trim().takeIf { it.isNotEmpty() }) {
                        reactionNote = ""
                        focusManager.clearFocus()
                    }
                }
            )
        }
    }
}

@Composable
private fun FeedActivityPill(
    users: List<User>,
    overflowCount: Int,
    onClick: () -> Unit
) {
    val visibleUsers = users.take(3)
    val showOverflow = overflowCount > 0
    val avatarItemCount = visibleUsers.size + if (showOverflow) 1 else 0
    val avatarStackWidth = if (avatarItemCount == 0) {
        0.dp
    } else {
        36.dp + ((avatarItemCount - 1) * 23).dp
    }

    Row(
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFF34363B))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Activity",
            color = Color(0xFFE3E2E6),
            fontSize = 18.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )

        if (avatarItemCount > 0) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Box(
            modifier = Modifier
                .width(avatarStackWidth)
                .height(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            visibleUsers.forEachIndexed { index, user ->
                SolariAvatar(
                    imageUrl = user.profileImageUrl,
                    username = user.username,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (index * 23).dp)
                        .size(36.dp),
                    shape = CircleShape,
                    fontSize = 14.sp
                )
            }

            if (showOverflow) {
                Box(
                    modifier = Modifier
                        .offset(x = (visibleUsers.size * 23).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5A5C62)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$overflowCount",
                        color = Color(0xFFD0D0D4),
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedActivitySheet(
    visible: Boolean,
    activities: List<PostActivityEntry>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    var isSheetDragging by remember { mutableStateOf(false) }
    var sheetDragOffsetPx by remember { mutableStateOf(0f) }
    val animatedSheetDragOffsetPx by animateFloatAsState(
        targetValue = sheetDragOffsetPx,
        animationSpec = tween(durationMillis = if (isSheetDragging) 0 else 180),
        label = "feedActivitySheetDragOffset"
    )

    LaunchedEffect(visible) {
        if (visible) {
            isSheetDragging = false
            sheetDragOffsetPx = 0f
        }
    }

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
                    .offset {
                        IntOffset(
                            x = 0,
                            y = animatedSheetDragOffsetPx.roundToInt()
                        )
                    }
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
                        .pointerInput(onDismiss) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    isSheetDragging = true
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    sheetDragOffsetPx = (sheetDragOffsetPx + dragAmount)
                                        .coerceAtLeast(0f)
                                },
                                onDragEnd = {
                                    isSheetDragging = false
                                    if (sheetDragOffsetPx > 80f) {
                                        onDismiss()
                                    } else {
                                        sheetDragOffsetPx = 0f
                                    }
                                },
                                onDragCancel = {
                                    isSheetDragging = false
                                    sheetDragOffsetPx = 0f
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

                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SolariTheme.colors.primary,
                            trackColor = SolariTheme.colors.surfaceVariant
                        )
                    }

                    activities.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activity yet",
                            color = SolariTheme.colors.onSurface.copy(alpha = 0.68f),
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    else -> LazyColumn(
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
}

@Composable
private fun FeedActivityItem(activity: PostActivityEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolariAvatar(
            imageUrl = activity.user.profileImageUrl,
            username = activity.user.username,
            contentDescription = "${activity.user.displayName} avatar",
            modifier = Modifier
                .size(42.dp),
            shape = CircleShape,
            fontSize = 16.sp
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
                text = activity.description,
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
                    fontSize = 12.sp,
                    lineHeight = 13.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            activity.emoji?.let { emoji ->
                Text(
                    text = emoji,
                    fontSize = 22.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
private fun FeedImage(
    url: String,
    modifier: Modifier = Modifier
) {
    var isLoading by remember(url) { mutableStateOf(true) }

    Box(modifier = modifier) {
        AsyncImage(
            model = url,
            contentDescription = "Post Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false }
        )

        if (isLoading) {
            FeedMediaLoadingIndicator()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun FeedVideoPlayer(
    url: String,
    mediaType: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoading by remember(url) { mutableStateOf(true) }
    val player = remember(url, mediaType) {
        ExoPlayer.Builder(context)
            .setVideoChangeFrameRateStrategy(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(
                    MediaItem.Builder()
                        .setUri(Uri.parse(url))
                        .setMimeType(mediaType.toMedia3VideoMimeType())
                        .build()
                )
                prepare()
            }
    }

    LaunchedEffect(player, isActive) {
        player.playWhenReady = isActive
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isLoading = playbackState == Player.STATE_BUFFERING ||
                        playbackState == Player.STATE_IDLE
            }

            override fun onRenderedFirstFrame() {
                isLoading = false
            }

            override fun onPlayerError(error: PlaybackException) {
                isLoading = false
            }
        }

        player.addListener(listener)
        isLoading = player.playbackState == Player.STATE_BUFFERING ||
                player.playbackState == Player.STATE_IDLE

        onDispose {
            player.removeListener(listener)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    controllerAutoShow = false
                    hideController()
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    this.player = player
                }
            },
            update = { playerView ->
                playerView.useController = false
                playerView.controllerAutoShow = false
                playerView.hideController()
                if (playerView.player !== player) {
                    playerView.player = player
                }
            }
        )

        if (isLoading) {
            FeedMediaLoadingIndicator()
        }
    }
}

@Composable
private fun FeedMediaLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.26f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = SolariTheme.colors.primary,
            trackColor = SolariTheme.colors.surface
        )
    }
}

private fun Post.isVideoMedia(): Boolean {
    val normalizedMediaType = mediaType.lowercase()
    val normalizedUrl = imageUrl.substringBefore("?").lowercase()
    return normalizedMediaType.startsWith("video/") ||
            normalizedMediaType == "video" ||
            normalizedUrl.endsWith(".mp4") ||
            normalizedUrl.endsWith(".mov") ||
            normalizedUrl.endsWith(".m4v") ||
            normalizedUrl.endsWith(".webm")
}

private fun String.toMedia3VideoMimeType(): String {
    val normalized = lowercase()
    return when {
        normalized.startsWith("video/") -> this
        normalized.contains("webm") -> MimeTypes.VIDEO_WEBM
        normalized.contains("quicktime") || normalized.contains("mov") -> "video/quicktime"
        else -> MimeTypes.VIDEO_MP4
    }
}

private fun Long.toFeedRelativeTimeLabel(nowMillis: Long = System.currentTimeMillis()): String {
    val elapsedMillis = (nowMillis - this).coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)

    return when {
        minutes < 1 -> "JUST NOW"
        minutes < 60 -> "${minutes}M AGO"
        hours < 24 -> "${hours}H AGO"
        days < 7 -> "${days}D AGO"
        days < 30 -> "${days / 7}W AGO"
        days < 365 -> "${days / 30}MO AGO"
        else -> "${days / 365}Y AGO"
    }
}

@Composable
private fun FeedReactionField(
    value: String,
    onValueChange: (String) -> Unit,
    onReact: (String) -> Unit,
    onOpenEmojiPicker: () -> Unit
) {
    val quickReactionEmojis = listOf("❤️", "😂", "😮")

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
            modifier = Modifier.weight(1f)
                .padding(end = 8.dp),
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
                            text = "Send reaction...",
                            color = Color(0xFF9699A1),
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans
                        )
                    }
                    innerTextField()
                }
            }
        )

        quickReactionEmojis.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onReact(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onOpenEmojiPicker),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "More reactions",
                tint = Color(0xFFD7C0B2),
                modifier = Modifier.size(22.dp)
            )
        }
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
            .clip(RoundedCornerShape(16.dp))
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
