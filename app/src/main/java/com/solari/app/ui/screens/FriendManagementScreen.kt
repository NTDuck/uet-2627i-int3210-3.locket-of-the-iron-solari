package com.solari.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SortDropdownButton
import com.solari.app.ui.components.SortSelection
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.util.scaledClickable
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
private const val InviteBaseUrl = "https://solari.adnope.io.vn"

private enum class NicknameAction {
    Set,
    Update
}

private data class NicknameDialogState(
    val friend: User,
    val action: NicknameAction
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendManagementScreen(
    viewModel: FriendManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBlockedAccounts: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToConversation: (Conversation) -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var requestText by remember { mutableStateOf("") }
    var sortSelection by remember { mutableStateOf(SortSelection.Default) }
    var friendPendingUnfriend by remember { mutableStateOf<User?>(null) }
    var friendPendingBlock by remember { mutableStateOf<User?>(null) }
    var nicknameDialogState by remember { mutableStateOf<NicknameDialogState?>(null) }
    var isUserRefreshing by remember { mutableStateOf(false) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    var feedbackPillIsSuccess by remember { mutableStateOf(false) }
    var feedbackEventId by remember { mutableStateOf(0) }
    val inviteLink = viewModel.currentUser?.username?.let(::buildInviteLink).orEmpty()
    val friends = viewModel.friends
    val feedbackMessage = viewModel.successMessage ?: viewModel.errorMessage
    val isSuccessFeedback = viewModel.successMessage != null

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
                        text = inviteLink.ifBlank { "Loading invite link..." },
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
                        text = "Share Invite Link",
                        icon = Icons.Outlined.Share,
                        enabled = inviteLink.isNotBlank(),
                        onClick = {
                            shareInviteLink(context, inviteLink)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.isSendingRequest) {
                                CircularProgressIndicator(
                                    color = FriendsText,
                                    trackColor = FriendsButton,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                            }
                            Text(
                                text = "Send request",
                                color = FriendsText,
                                fontSize = 12.sp,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                                onSetNickname = { nicknameFriend ->
                                    nicknameDialogState = NicknameDialogState(
                                        friend = nicknameFriend,
                                        action = NicknameAction.Set
                                    )
                                },
                                onUpdateNickname = { nicknameFriend ->
                                    nicknameDialogState = NicknameDialogState(
                                        friend = nicknameFriend,
                                        action = NicknameAction.Update
                                    )
                                },
                                onRemoveNickname = viewModel::removeNickname,
                                onMessage = { friend ->
                                    viewModel.openConversation(friend, onNavigateToConversation)
                                },
                                messagingFriendIds = viewModel.messagingFriendIds,
                                onUnfriend = { friendPendingUnfriend = it },
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
                initialOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            FriendManagementFeedbackPill(
                message = feedbackPillMessage,
                isSuccess = feedbackPillIsSuccess
            )
        }
    }

    friendPendingUnfriend?.let { friend ->
        SolariConfirmationDialog(
            title = "Unfriend ${friend.displayName}?",
            message = "They will be removed from your friend list.",
            confirmText = "Unfriend",
            onConfirm = {
                viewModel.unfriend(friend)
                friendPendingUnfriend = null
            },
            onDismiss = { friendPendingUnfriend = null }
        )
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

    nicknameDialogState?.let { state ->
        NicknameDialog(
            state = state,
            onConfirm = { nickname ->
                when (state.action) {
                    NicknameAction.Set -> viewModel.setNickname(state.friend, nickname)
                    NicknameAction.Update -> viewModel.updateNickname(state.friend, nickname)
                }
                nicknameDialogState = null
            },
            onDismiss = { nicknameDialogState = null }
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .scaledClickable(pressedScale = 1.05f, enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(19.dp))
            .background(if (enabled) FriendsPrimary else FriendsButton),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) FriendsPrimaryContent else FriendsSubtle,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (enabled) FriendsPrimaryContent else FriendsSubtle,
            fontSize = 13.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun buildInviteLink(username: String): String {
    return Uri.parse(InviteBaseUrl)
        .buildUpon()
        .appendPath("u")
        .appendPath(username)
        .build()
        .toString()
}

private fun shareInviteLink(context: Context, inviteLink: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, inviteLink)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share invite link"))
}

@Composable
private fun FriendListItem(
    friend: User,
    onSetNickname: (User) -> Unit,
    onUpdateNickname: (User) -> Unit,
    onRemoveNickname: (User) -> Unit,
    onMessage: (User) -> Unit,
    messagingFriendIds: Set<String>,
    onUnfriend: (User) -> Unit,
    onBlock: (User) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var moreButtonPressPosition by remember { mutableStateOf(Offset.Unspecified) }
    var moreButtonSize by remember { mutableStateOf(IntSize.Zero) }
    var menuSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val menuOffsetY = with(density) { 30.dp.roundToPx() }
    val handle = "@${friend.username}"
    val hasNickname = !friend.nickname.isNullOrBlank()

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
                    .onSizeChanged { moreButtonSize = it }
                    .scaledClickable(
                        pressedScale = 1.2f,
                        scaleFromTouch = true,
                        onPressPosition = { moreButtonPressPosition = it }
                    ) {
                        isMenuExpanded = true
                    },
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
                    offset = IntOffset(0, menuOffsetY),
                    onDismissRequest = { isMenuExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    var menuVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        menuVisible = true
                    }
                    val menuScale by animateFloatAsState(
                        targetValue = if (menuVisible) 1f else 0.7f,
                        animationSpec = tween(140),
                        label = "FriendActionMenuScale"
                    )
                    val menuAlpha by animateFloatAsState(
                        targetValue = if (menuVisible) 1f else 0f,
                        animationSpec = tween(90),
                        label = "FriendActionMenuAlpha"
                    )

                    Surface(
                        color = FriendsSurface,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .onSizeChanged { menuSize = it }
                            .graphicsLayer {
                                alpha = menuAlpha
                                scaleX = menuScale
                                scaleY = menuScale
                                transformOrigin = friendActionMenuTransformOrigin(
                                    pressPosition = moreButtonPressPosition,
                                    anchorSize = moreButtonSize,
                                    menuSize = menuSize,
                                    menuOffsetY = menuOffsetY
                                )
                            }
                    ) {
                        val isOpeningMessage = friend.id in messagingFriendIds
                        val actions = buildList {
                            add(
                                FriendActionMenuEntry(
                                    text = if (isOpeningMessage) "Opening..." else "Message",
                                    color = if (isOpeningMessage) FriendsSubtle else FriendsText,
                                    enabled = !isOpeningMessage,
                                    onClick = { onMessage(friend) }
                                )
                            )
                            if (hasNickname) {
                                add(
                                    FriendActionMenuEntry(
                                        text = "Update Nickname",
                                        color = FriendsText,
                                        enabled = true,
                                        onClick = { onUpdateNickname(friend) }
                                    )
                                )
                                add(
                                    FriendActionMenuEntry(
                                        text = "Remove Nickname",
                                        color = FriendsText,
                                        enabled = true,
                                        onClick = { onRemoveNickname(friend) }
                                    )
                                )
                            } else {
                                add(
                                    FriendActionMenuEntry(
                                        text = "Set Nickname",
                                        color = FriendsText,
                                        enabled = true,
                                        onClick = { onSetNickname(friend) }
                                    )
                                )
                            }
                            add(
                                FriendActionMenuEntry(
                                    text = "Unfriend",
                                    color = FriendsText,
                                    enabled = true,
                                    onClick = { onUnfriend(friend) }
                                )
                            )
                            add(
                                FriendActionMenuEntry(
                                    text = "Block",
                                    color = FriendsMuted,
                                    enabled = true,
                                    onClick = { onBlock(friend) }
                                )
                            )
                        }
                        Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                            actions.forEachIndexed { index, action ->
                                FriendActionMenuItem(
                                    text = action.text,
                                    color = action.color,
                                    shape = friendActionOptionShape(
                                        index = index,
                                        lastIndex = actions.lastIndex
                                    ),
                                    onClick = {
                                        if (action.enabled) {
                                            isMenuExpanded = false
                                            action.onClick()
                                        }
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

private data class FriendActionMenuEntry(
    val text: String,
    val color: Color,
    val enabled: Boolean,
    val onClick: () -> Unit
)

private fun friendActionMenuTransformOrigin(
    pressPosition: Offset,
    anchorSize: IntSize,
    menuSize: IntSize,
    menuOffsetY: Int
): TransformOrigin {
    if (!pressPosition.isSpecified || anchorSize.width == 0 || menuSize.width == 0 || menuSize.height == 0) {
        return TransformOrigin(1f, 0f)
    }

    val pressXInMenu = menuSize.width - anchorSize.width + pressPosition.x
    val pressYInMenu = pressPosition.y - menuOffsetY
    return TransformOrigin(
        pivotFractionX = pressXInMenu / menuSize.width,
        pivotFractionY = pressYInMenu / menuSize.height
    )
}

@Composable
private fun NicknameDialog(
    state: NicknameDialogState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember(state.friend.id, state.action) {
        mutableStateOf(if (state.action == NicknameAction.Update) state.friend.nickname.orEmpty() else "")
    }
    val title = when (state.action) {
        NicknameAction.Set -> "Set Nickname"
        NicknameAction.Update -> "Update Nickname"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = FriendsSurface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 12.dp,
            modifier = Modifier.width(284.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,
                        color = FriendsText,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                    BasicTextField(
                        value = nickname,
                        onValueChange = { nickname = it.take(40) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = FriendsText,
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(FriendsText),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FriendsInput)
                            .padding(horizontal = 12.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (nickname.isEmpty()) {
                                    Text(
                                        text = "Nickname",
                                        color = FriendsSubtle,
                                        fontSize = 14.sp,
                                        fontFamily = PlusJakartaSans
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                FriendDialogActionRow(
                    text = title,
                    textColor = FriendsPrimary,
                    shape = RoundedCornerShape(0.dp),
                    onClick = { onConfirm(nickname) }
                )
                FriendDialogActionRow(
                    text = "Cancel",
                    textColor = FriendsText,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun FriendDialogActionRow(
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
            fontWeight = FontWeight.Bold
        )
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
            .fillMaxWidth()
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

private fun friendActionOptionShape(index: Int, lastIndex: Int): RoundedCornerShape {
    return when {
        index == 0 && index == lastIndex -> RoundedCornerShape(8.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        )
        index == lastIndex -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomEnd = 8.dp,
            bottomStart = 8.dp
        )
        else -> RoundedCornerShape(0.dp)
    }
}
