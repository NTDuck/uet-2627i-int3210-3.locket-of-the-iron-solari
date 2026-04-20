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
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.ConversationViewModel

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToManageFriends: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var isSortDescending by remember { mutableStateOf(true) }

    val sortedConversations = if (isSortDescending) {
        viewModel.conversations.sortedByDescending { it.timestamp }
    } else {
        viewModel.conversations.sortedBy { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
    ) {
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
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manage friends",
                                color = Color.White,
                                fontSize = 12.sp,
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
                        fontSize = 14.4.sp,
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
                        fontSize = 14.4.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PlusJakartaSans,
                        color = SolariTheme.colors.tertiary
                    )
                    IconButton(
                        onClick = { isSortDescending = !isSortDescending },
                        modifier = Modifier.size(19.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterList, 
                            contentDescription = "Sort", 
                            tint = SolariTheme.colors.tertiary, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            items(sortedConversations) { conversation ->
                ConversationItem(
                    conversation = conversation,
                    onClick = { onNavigateToChat(conversation.id) }
                )
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
            // Square avatar
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF5E2D15)),
                contentAlignment = Alignment.Center
            ) {
                val initials = request.user.displayName.split(" ").map { it.first() }.joinToString("")
                Text(
                    text = initials, 
                    color = SolariTheme.colors.secondary, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.4.sp,
                    fontFamily = PlusJakartaSans
                )
            }

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
    Surface(
        color = SolariTheme.colors.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                // Square avatar
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF5E2D15)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = conversation.otherUser.displayName.split(" ").map { it.first() }.joinToString("")
                    Text(
                        text = initials, 
                        color = SolariTheme.colors.secondary, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.4.sp,
                        fontFamily = PlusJakartaSans
                    )
                }

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
                            fontSize = 13.6.sp,
                            fontFamily = PlusJakartaSans
                        )
                        Text(
                            text = "2m ago", 
                            color = if (conversation.isUnread) SolariTheme.colors.secondary else Color.Gray, 
                            fontSize = 9.6.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = conversation.lastMessage,
                        color = if (conversation.isUnread) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        fontFamily = PlusJakartaSans,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
