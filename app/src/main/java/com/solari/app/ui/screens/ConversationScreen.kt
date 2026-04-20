package com.solari.app.ui.screens

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
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SortDropdownButton
import com.solari.app.ui.components.SortSelection
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.ConversationViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Conversation) -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var sortSelection by remember { mutableStateOf(SortSelection.Default) }
    var isUserRefreshing by remember { mutableStateOf(false) }

    val sortedConversations = when (sortSelection) {
        SortSelection.Default -> viewModel.conversations
        SortSelection.Newest -> viewModel.conversations.sortedByDescending { it.timestamp }
        SortSelection.Oldest -> viewModel.conversations.sortedBy { it.timestamp }
    }
    val isInitialLoading = viewModel.isLoading &&
            viewModel.friendRequests.isEmpty() &&
            viewModel.conversations.isEmpty()

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
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
            if (isInitialLoading) {
                CircularProgressIndicator(
                    color = SolariTheme.colors.primary,
                    trackColor = SolariTheme.colors.surface,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
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

                        if (viewModel.friendRequests.isNotEmpty()) {
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

                            items(viewModel.friendRequests) { request ->
                                FriendRequestItem(
                                    request = request,
                                    onAccept = { viewModel.acceptFriendRequest(request.id) },
                                    onDecline = { viewModel.declineFriendRequest(request.id) }
                                )
                            }
                        }

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

                        items(sortedConversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                onClick = { onNavigateToChat(conversation) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(request: FriendRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
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
                    text = "@${request.user.username}", 
                    color = SolariTheme.colors.tertiary,
                    fontSize = 11.2.sp,
                    fontFamily = PlusJakartaSans
                )
            }

            Surface(
                onClick = onDecline,
                modifier = Modifier.size(32.dp),
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
                onClick = onAccept,
                modifier = Modifier.size(32.dp),
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

@Composable
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    val itemShape = RoundedCornerShape(10.dp)
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
            .clip(itemShape),
        onClick = onClick
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
                    imageUrl = conversation.otherUser.profileImageUrl,
                    username = conversation.otherUser.username,
                    contentDescription = "${conversation.otherUser.displayName} avatar",
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
                            text = conversation.otherUser.displayName, 
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
