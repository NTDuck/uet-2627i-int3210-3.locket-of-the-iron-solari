package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.FriendRequest
import com.solari.app.ui.models.FriendRequestDirection
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.components.SortDropdownButton
import com.solari.app.ui.components.SortSelection
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ConversationViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    externalFeedbackMessage: String? = null,
    onExternalFeedbackConsumed: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Conversation) -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var sortSelection by remember { mutableStateOf(SortSelection.Default) }
    var isUserRefreshing by remember { mutableStateOf(false) }
    var requestPendingCancel by remember { mutableStateOf<FriendRequest?>(null) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    var feedbackPillIsSuccess by remember { mutableStateOf(false) }
    var feedbackEventId by remember { mutableStateOf(0) }
    var topFeedbackVisible by remember { mutableStateOf(false) }
    var topFeedbackMessage by remember { mutableStateOf("") }
    var topFeedbackEventId by remember { mutableStateOf(0) }
    val feedbackMessage = viewModel.successMessage ?: viewModel.errorMessage
    val isSuccessFeedback = viewModel.successMessage != null

    val visibleConversations = viewModel.conversations.filter { it.lastMessage.isNotBlank() }
    val sortedConversations = when (sortSelection) {
        SortSelection.Default -> visibleConversations
        SortSelection.Newest -> visibleConversations.sortedByDescending { it.timestamp }
        SortSelection.Oldest -> visibleConversations.sortedBy { it.timestamp }
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
        }
    }

    LaunchedEffect(feedbackMessage, isSuccessFeedback) {
        if (feedbackMessage != null) {
            feedbackPillMessage = feedbackMessage
            feedbackPillIsSuccess = isSuccessFeedback
            feedbackPillVisible = true
            feedbackEventId += 1
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(feedbackEventId) {
        if (feedbackEventId > 0) {
            delay(1000)
            feedbackPillVisible = false
        }
    }

    LaunchedEffect(externalFeedbackMessage) {
        val message = externalFeedbackMessage ?: return@LaunchedEffect
        topFeedbackMessage = message
        topFeedbackVisible = true
        topFeedbackEventId += 1
        onExternalFeedbackConsumed()
    }

    LaunchedEffect(topFeedbackEventId) {
        if (topFeedbackEventId > 0) {
            delay(1000)
            topFeedbackVisible = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isUserRefreshing,
        onRefresh = {
            isUserRefreshing = true
            viewModel.refresh()
        },
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = topFeedbackVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it * 2 },
                    animationSpec = tween(durationMillis = 260)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it * 2 },
                    animationSpec = tween(durationMillis = 220)
                ),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                SolariFeedbackPill(
                    message = topFeedbackMessage,
                    isSuccess = true
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 19.dp),
                        verticalArrangement = Arrangement.spacedBy(13.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Manage Friends Button aligned to the right
                        item {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Surface(
                                    onClick = onNavigateToManageFriends,
                                    color = Color(0xFF2C2D30),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.GroupAdd,
                                            contentDescription = null,
                                            tint = SolariTheme.colors.tertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Manage friends",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Friend requests section: inline spinner or list
                        if (viewModel.isLoadingFriendRequests) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = SolariTheme.colors.primary,
                                        trackColor = SolariTheme.colors.surface
                                    )
                                }
                            }
                        } else if (viewModel.visibleFriendRequests.isNotEmpty()) {
                            item {
                                Text(
                                    text = "FRIEND REQUESTS",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSans,
                                    color = SolariTheme.colors.tertiary,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                                )
                            }

                            items(viewModel.visibleFriendRequests, key = { it.id }) { request ->
                                FriendRequestItem(
                                    request = request,
                                    onAccept = { viewModel.acceptFriendRequest(request.id) },
                                    onDecline = { viewModel.declineFriendRequest(request.id) },
                                    onCancel = { requestPendingCancel = request }
                                )
                            }

                            if (viewModel.canViewMoreFriendRequests || viewModel.canViewLessFriendRequests) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FriendRequestTogglePill(
                                            text = when {
                                                viewModel.canViewMoreFriendRequests -> "View more"
                                                else -> "View less"
                                            },
                                            isLoading = viewModel.isLoadingMoreFriendRequests,
                                            enabled = !viewModel.isLoadingMoreFriendRequests,
                                            onClick = {
                                                if (viewModel.canViewMoreFriendRequests) {
                                                    viewModel.expandFriendRequests()
                                                } else {
                                                    viewModel.collapseFriendRequests()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Conversations section header (always visible)
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 13.dp, bottom = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONVERSATIONS",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSans,
                                    color = SolariTheme.colors.tertiary
                                )
                                SortDropdownButton(
                                    selected = sortSelection,
                                    onSelected = { sortSelection = it },
                                    iconTint = SolariTheme.colors.tertiary,
                                    menuContainerColor = SolariTheme.colors.surface,
                                    menuContentColor = Color.White,
                                    modifier = Modifier.size(28.dp),
                                    iconSize = 17
                                )
                            }
                        }

                        // Conversations section: inline spinner or list
                        if (viewModel.isLoadingConversations) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = SolariTheme.colors.primary,
                                        trackColor = SolariTheme.colors.surface
                                    )
                                }
                            }
                        } else {
                            items(sortedConversations) { conversation ->
                                ConversationItem(
                                    conversation = conversation,
                                    onClick = {
                                        viewModel.markConversationAsRead(conversation.id)
                                        onNavigateToChat(conversation)
                                    }
                                )
                            }
                        }
                    }
                }

            AnimatedVisibility(
                visible = feedbackPillVisible,
                enter = slideInVertically(
                    initialOffsetY = { it * 2 },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it * 2 },
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                ConversationFeedbackPill(
                    message = feedbackPillMessage,
                    isSuccess = feedbackPillIsSuccess
                )
            }
        }
    }

    requestPendingCancel?.let { request ->
        SolariConfirmationDialog(
            title = "Unsend friend request",
            message = "Are you sure you want to cancel your friend request to ${request.user.displayName}?",
            confirmText = "Unsend",
            onConfirm = {
                viewModel.cancelFriendRequest(request.id)
                requestPendingCancel = null
            },
            onDismiss = { requestPendingCancel = null }
        )
    }
}

@Composable
private fun FriendRequestTogglePill(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF2C2D30),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.scaledClickable(
            pressedScale = 1.1f,
            enabled = enabled,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 84.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = SolariTheme.colors.tertiary,
                    trackColor = Color.Transparent
                )
            } else {
                Text(
                    text = text,
                    color = SolariTheme.colors.tertiary,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onCancel: () -> Unit
) {
    val isOutgoing = request.direction == FriendRequestDirection.Outgoing

    Surface(
        color = SolariTheme.colors.surface,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SolariAvatar(
                imageUrl = request.user.profileImageUrl,
                username = request.user.username,
                contentDescription = "${request.user.displayName} avatar",
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(6.dp)),
                shape = RoundedCornerShape(6.dp),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.user.displayName,
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 13.6.sp,
                    fontFamily = PlusJakartaSans
                )
                Text(
                    text = if (isOutgoing) "Outgoing to @${request.user.username}" else "Incoming from @${request.user.username}",
                    color = SolariTheme.colors.tertiary,
                    fontSize = 11.2.sp,
                    fontFamily = PlusJakartaSans
                )
            }

            if (isOutgoing) {
                Surface(
                    onClick = onCancel,
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2C2D30)
                ) {
                    Text(
                        text = "Unsend",
                        color = SolariTheme.colors.secondary,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            } else {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .scaledClickable(pressedScale = 1.1f, onClick = onDecline),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF2C2D30)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = SolariTheme.colors.secondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .scaledClickable(pressedScale = 1.1f, onClick = onAccept),
                    shape = RoundedCornerShape(10.dp),
                    color = SolariTheme.colors.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Accept",
                            tint = Color.Black,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationFeedbackPill(
    message: String,
    isSuccess: Boolean
) {
    val backgroundColor = if (isSuccess) Color(0xFF163624) else Color(0xFF3C1E22)
    val iconTint = if (isSuccess) Color(0xFF77E0A1) else Color(0xFFFF8A80)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

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
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    val itemShape = RoundedCornerShape(10.dp)
    val displayName = if (conversation.isReadOnly) "Someone" else conversation.otherUser.displayName
    val avatarUsername = if (conversation.isReadOnly) "someone" else conversation.otherUser.username
    val avatarUrl = if (conversation.isReadOnly) null else conversation.otherUser.profileImageUrl
    val lastMessagePreview = remember(conversation.lastMessage, conversation.lastMessageSenderId, conversation.otherUser.id) {
        when {
            conversation.lastMessage.isBlank() -> ""
            conversation.lastMessageSenderId != null &&
                    conversation.lastMessageSenderId != conversation.otherUser.id -> {
                "You: ${conversation.lastMessage}"
            }
            else -> conversation.lastMessage
        }
    }

    Surface(
        color = SolariTheme.colors.surfaceVariant,
        shape = itemShape,
        modifier = Modifier
            .fillMaxWidth()
            .scaledClickable(pressedScale = 0.9f, onClick = onClick)
            .clip(itemShape)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Unread indicator
            if (conversation.isUnread) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(45.dp)
                        .background(SolariTheme.colors.secondary, RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                )
            } else {
                Spacer(modifier = Modifier.width(3.dp))
            }

            Row(
                modifier = Modifier.padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SolariAvatar(
                    imageUrl = avatarUrl,
                    username = avatarUsername,
                    contentDescription = "$displayName avatar",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    shape = RoundedCornerShape(6.dp),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans
                        )
                        Text(
                            text = conversation.timestamp.toRelativeTimeLabel(),
                            color = if (conversation.isUnread) SolariTheme.colors.secondary else Color.Gray, 
                            fontSize = 11.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = lastMessagePreview,
                        color = if (conversation.isUnread) Color.White else Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        fontFamily = PlusJakartaSans,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun Long.toRelativeTimeLabel(nowMillis: Long = System.currentTimeMillis()): String {
    val elapsedMillis = (nowMillis - this).coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)

    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        days < 365 -> "${days / 30}mo ago"
        else -> "${days / 365}y ago"
    }
}
