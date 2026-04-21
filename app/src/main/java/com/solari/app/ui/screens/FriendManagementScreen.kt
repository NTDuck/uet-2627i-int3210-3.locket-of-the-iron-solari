package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SortDropdownButton
import com.solari.app.ui.components.SortSelection
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.viewmodels.FriendManagementViewModel
import kotlinx.coroutines.delay

private val FriendsBackground = Color(0xFF111316)
private val FriendsSurface = Color(0xFF1B1C21)
private val FriendsInput = Color(0xFF080B0E)
private val FriendsPrimary = Color(0xFFFF8426)
private val FriendsPrimaryContent = Color(0xFF5F2900)
private val FriendsText = Color(0xFFE3E2E6)
private val FriendsMuted = Color(0xFFD7C0B2)
private val FriendsSubtle = Color(0xFF9699A1)
private val FriendsButton = Color(0xFF34363B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendManagementScreen(
    viewModel: FriendManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBlockedAccounts: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var requestText by remember { mutableStateOf("") }
    var sortSelection by remember { mutableStateOf(SortSelection.Default) }
    var friendPendingBlock by remember { mutableStateOf<User?>(null) }
    var isUserRefreshing by remember { mutableStateOf(false) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    var feedbackPillIsSuccess by remember { mutableStateOf(false) }
    var feedbackEventId by remember { mutableStateOf(0) }
    val inviteLink = "https://solari-backend.com/usern..."
    val friends = viewModel.friends
    val feedbackMessage = viewModel.successMessage ?: viewModel.errorMessage
    val isSuccessFeedback = viewModel.successMessage != null

    LaunchedEffect(feedbackMessage, isSuccessFeedback) {
        if (feedbackMessage != null) {
            feedbackPillMessage = feedbackMessage
            feedbackPillIsSuccess = isSuccessFeedback
            feedbackPillVisible = true
            feedbackEventId += 1
        }
    }

    LaunchedEffect(feedbackEventId) {
        if (feedbackEventId > 0) {
            delay(2_000)
            feedbackPillVisible = false
            delay(320)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = FriendsBackground,
            bottomBar = {
                SolariBottomNavBar(
                    selectedRoute = SolariRoute.Screen.Conversations.name,
                    onNavigate = { routeName ->
                        when (routeName) {
                            SolariRoute.Screen.CameraBefore.name -> onNavigateToCamera()
                            SolariRoute.Screen.Feed.name -> onNavigateToFeed()
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
                    viewModel.loadFriends(sortSelection.apiValue)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(FriendsBackground)
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 19.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 0.dp, bottom = 22.dp)
                ) {
            item {
                FriendManagementSectionTitle(text = "PERSONAL INVITE")
                Spacer(modifier = Modifier.height(13.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(FriendsSurface)
                        .padding(13.dp)
                ) {
                    Text(
                        text = inviteLink,
                        color = FriendsText,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSans,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FriendsInput, RoundedCornerShape(2.dp))
                            .padding(horizontal = 13.dp, vertical = 11.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.6.dp)
                            .background(FriendsPrimary)
                    )

                    Spacer(modifier = Modifier.height(13.dp))

                    FriendsPrimaryButton(
                        text = "Share invite link",
                        icon = Icons.Outlined.Share,
                        onClick = {
                            clipboardManager.setText(AnnotatedString(inviteLink))
                            feedbackPillMessage = "Invite link copied"
                            feedbackPillIsSuccess = true
                            feedbackPillVisible = true
                            feedbackEventId += 1
                        }
                    )
                }
            }

            item {
                FriendManagementSectionTitle(text = "ADD NEW CONNECTION")
                Spacer(modifier = Modifier.height(13.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                        .background(FriendsSurface)
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .background(FriendsInput, RoundedCornerShape(topStart = 2.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = FriendsMuted,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            BasicTextField(
                                value = requestText,
                                onValueChange = { requestText = it },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = FriendsText,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(FriendsText),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (requestText.isEmpty()) {
                                            Text(
                                                text = "username/email",
                                                color = FriendsSubtle,
                                                fontSize = 12.sp,
                                                fontFamily = PlusJakartaSans,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.6.dp)
                                .background(FriendsMuted)
                        )
                    }

                    Spacer(modifier = Modifier.width(11.dp))

                    Box(
                        modifier = Modifier
                            .weight(0.75f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(FriendsButton)
                            .clickable(enabled = !viewModel.isSendingRequest) {
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                                viewModel.sendFriendRequest(requestText) {
                                    requestText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (viewModel.isSendingRequest) "Sending..." else "Send request",
                            color = FriendsText,
                            fontSize = 12.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(FriendsSurface)
                        .clickable(onClick = onNavigateToBlockedAccounts)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "View blocked accounts",
                        color = FriendsMuted,
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FriendManagementSectionTitle(text = "FRIEND LIST")
                    SortDropdownButton(
                        selected = sortSelection,
                        onSelected = { selection ->
                            sortSelection = selection
                            viewModel.loadFriends(selection.apiValue)
                        },
                        iconTint = FriendsMuted,
                        menuContainerColor = FriendsSurface,
                        menuContentColor = FriendsText,
                        modifier = Modifier.size(28.dp),
                        iconSize = 14
                    )
                }
            }

            if (viewModel.isLoading && friends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = FriendsPrimary,
                            trackColor = FriendsSurface
                        )
                    }
                }
            } else if (friends.isEmpty()) {
                item {
                    Text(
                        text = viewModel.errorMessage ?: "No friends yet",
                        color = FriendsSubtle,
                        fontSize = 13.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        friends.forEach { friend ->
                            FriendListItem(
                                friend = friend,
                                onBlock = { friendPendingBlock = it }
                            )
                        }
                    }
                }
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
            FriendManagementFeedbackPill(
                message = feedbackPillMessage,
                isSuccess = feedbackPillIsSuccess
            )
        }
    }

    friendPendingBlock?.let { friend ->
        SolariConfirmationDialog(
            title = "Block ${friend.displayName}?",
            message = "They will no longer be able to find your profile or interact with your content.",
            confirmText = "Block",
            onConfirm = {
                viewModel.blockFriend(friend)
                friendPendingBlock = null
            },
            onDismiss = { friendPendingBlock = null }
        )
    }
}

@Composable
private fun FriendManagementFeedbackPill(
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
private fun FriendManagementSectionTitle(text: String) {
    Text(
        text = text,
        color = FriendsMuted,
        fontSize = 15.sp,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun FriendsPrimaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(FriendsPrimary)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FriendsPrimaryContent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = FriendsPrimaryContent,
            fontSize = 13.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FriendListItem(
    friend: User,
    onBlock: (User) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val handle = "@${friend.username}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(FriendsSurface)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolariAvatar(
            imageUrl = friend.profileImageUrl,
            username = friend.username,
            contentDescription = "${friend.displayName} avatar",
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = friend.displayName,
                color = FriendsText,
                fontSize = 15.sp,
                lineHeight = 15.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = handle,
                color = FriendsSubtle,
                fontSize = 13.sp,
                lineHeight = 13.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            )
        }

        Box {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isMenuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Friend actions",
                    tint = FriendsMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (isMenuExpanded) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(0, 30),
                    onDismissRequest = { isMenuExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    var menuVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        menuVisible = true
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = menuVisible,
                        enter = fadeIn(animationSpec = tween(120)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(120))
                    ) {
                        Surface(
                            color = FriendsSurface,
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 8.dp
                        ) {
                            Column {
                                FriendActionMenuItem(
                                    text = "Unfriend",
                                    color = FriendsText,
                                    shape = FriendActionOptionTopShape,
                                    onClick = { isMenuExpanded = false }
                                )
                                FriendActionMenuItem(
                                    text = "Block",
                                    color = FriendsMuted,
                                    shape = FriendActionOptionBottomShape,
                                    onClick = {
                                        isMenuExpanded = false
                                        onBlock(friend)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendActionMenuItem(
    text: String,
    color: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(132.dp)
            .height(40.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 15.sp,
            lineHeight = 15.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium
        )
    }
}

private val FriendActionOptionTopShape = RoundedCornerShape(
    topStart = 8.dp,
    topEnd = 8.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

private val FriendActionOptionBottomShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomEnd = 8.dp,
    bottomStart = 8.dp
)
