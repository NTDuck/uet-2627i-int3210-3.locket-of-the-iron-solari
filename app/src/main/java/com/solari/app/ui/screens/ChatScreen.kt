package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageDeliveryState
import com.solari.app.ui.models.MessageReaction
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.util.EmojiCatalog
import com.solari.app.ui.util.EmojiCatalogCategory
import com.solari.app.ui.util.scaleOnPress
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

private val ChatBackground = Color(0xFF111316)
private val ChatHeader = Color(0xFF1B1C21)
private val ChatIncomingBubble = Color(0xFF1D1E23)
private val ChatOutgoingBubble = Color(0xFF34363B)
private val ChatInput = Color(0xFF080B0E)
private val ChatPrimary = Color(0xFFFF8426)
private val ChatText = Color(0xFFE3E2E6)
private val ChatMuted = Color(0xFF9699A1)
private val ChatChip = Color(0xFF080B0E)
private val ChatReadText = Color(0xFFE8D6CB)
private val ChatReactionSurface = Color(0xFF26282E)
private val EmojiPickerPanel = Color(0xFF303234)
private val EmojiPickerSearch = Color(0xFF46484B)
private val EmojiPickerMuted = Color(0xFFB5B5B7)
private const val MaxScrollToBottomPasses = 12
private const val MessageSendScrollDeltaPx = 1_000_000f
private const val MinuteMillis = 60_000L
private const val HourMillis = 60L * MinuteMillis
private const val DayMillis = 24L * HourMillis

private val QuickReactionEmojis = listOf("❤️", "😂", "😮", "😢", "😡", "👍")

private data class ChatMessageBlock(
    val senderId: String,
    val messages: List<Message>
)

private data class ChatDaySection(
    val date: LocalDate,
    val label: String,
    val blocks: List<ChatMessageBlock>
)

private data class ChatViewportAnchor(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val isScrolledToBottom: Boolean
)

@Composable
fun ChatScreen(
    chatId: String,
    initialPartner: User?,
    viewModel: ChatViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (chatId: String, partner: User?) -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val currentUser = viewModel.currentUser
    val currentUserId = currentUser?.id ?: viewModel.currentUserId
    val currentConversation = viewModel.conversation
    val activeChatId = currentConversation?.id ?: chatId
    val isReadOnly = currentConversation?.isReadOnly == true
    val isDraftConversation = currentConversation?.isDraft == true
    val partner = currentConversation?.otherUser ?: initialPartner
    val displayPartnerName = if (isReadOnly) "Someone" else partner?.displayName.orEmpty()
    val displayPartnerUsername = if (isReadOnly) "someone" else partner?.username.orEmpty()
    val displayPartnerAvatarUrl = if (isReadOnly) null else partner?.profileImageUrl
    val visiblePartner = if (isReadOnly) {
        User(
            id = "read-only-partner",
            displayName = "Someone",
            username = "someone",
            email = "",
            profileImageUrl = null
        )
    } else {
        partner
    }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val keyboardBottomPadding = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val allMessages = currentConversation?.messages.orEmpty()
    val sortedMessages = remember(allMessages) { allMessages.sortedBy { it.timestamp } }
    val daySections = remember(sortedMessages) { buildChatDaySections(sortedMessages) }
    val lastMessage = sortedMessages.lastOrNull()
    val recentEmojis = viewModel.recentEmojis
    val messageListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var previousMessageCount by remember(chatId) { mutableStateOf(sortedMessages.size) }
    var isMessageScrollInitialized by remember(chatId) { mutableStateOf(false) }
    var hasSeenInitialMessageLoad by remember(chatId) { mutableStateOf(false) }
    var hasFinishedInitialMessageLoad by remember(chatId) { mutableStateOf(false) }
    var replyingToMessage by remember(chatId) { mutableStateOf<Message?>(null) }
    var highlightedMessageId by remember(chatId) { mutableStateOf<String?>(null) }
    val messageItemIndexes = remember(daySections) {
        buildMessageItemIndexMap(daySections)
    }
    val lastMessageItemIndex = remember(daySections) {
        daySections.sumOf { section -> 1 + section.blocks.size } - 1
    }

    fun jumpToMessage(messageId: String) {
        val targetIndex = messageItemIndexes[messageId] ?: return
        highlightedMessageId = messageId
        coroutineScope.launch {
            val layoutInfo = messageListState.layoutInfo
            val targetItemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == targetIndex }
            if (targetItemInfo != null &&
                targetItemInfo.offset < layoutInfo.viewportEndOffset &&
                targetItemInfo.offset + targetItemInfo.size > layoutInfo.viewportStartOffset
            ) {
                return@launch
            }

            val topHalfOffset = -(messageListState.layoutInfo.viewportSize.height / 4)
            messageListState.animateScrollToItem(targetIndex, scrollOffset = topHalfOffset)
        }
    }

    LaunchedEffect(highlightedMessageId) {
        if (highlightedMessageId == null) return@LaunchedEffect
        delay(1_250)
        highlightedMessageId = null
    }

    LaunchedEffect(isDraftConversation) {
        if (isDraftConversation) {
            hasSeenInitialMessageLoad = true
            hasFinishedInitialMessageLoad = true
            isMessageScrollInitialized = sortedMessages.isEmpty()
        }
    }

    LaunchedEffect(viewModel.isLoadingMessages) {
        if (viewModel.isLoadingMessages) {
            hasSeenInitialMessageLoad = true
            hasFinishedInitialMessageLoad = false
            isMessageScrollInitialized = false
        } else if (hasSeenInitialMessageLoad) {
            hasFinishedInitialMessageLoad = true
        }
    }

    LaunchedEffect(
        sortedMessages.size,
        lastMessage?.id,
        currentUserId,
        viewModel.isLoadingMessages,
        hasFinishedInitialMessageLoad
    ) {
        if (viewModel.isLoadingMessages) return@LaunchedEffect
        if (!hasFinishedInitialMessageLoad) return@LaunchedEffect
        if (lastMessageItemIndex < 0) {
            previousMessageCount = sortedMessages.size
            isMessageScrollInitialized = true
            return@LaunchedEffect
        }

        if (!isMessageScrollInitialized) {
            previousMessageCount = sortedMessages.size
            messageListState.scrollToMessageBottom(lastMessageItemIndex)
            isMessageScrollInitialized = true
            return@LaunchedEffect
        }

        val newMessageFromCurrentUser = sortedMessages.size > previousMessageCount &&
                lastMessage?.senderId == currentUserId

        previousMessageCount = sortedMessages.size

        if (newMessageFromCurrentUser) {
            messageListState.scrollToBottomFromCurrentPosition()
        }
    }

    Scaffold(
        containerColor = ChatBackground,
        bottomBar = {
            SolariBottomNavBar(
                selectedRoute = SolariRoute.Screen.Chat.name,
                onNavigate = { route ->
                    when (route) {
                        SolariRoute.Screen.CameraBefore.name -> onNavigateToCamera()
                        SolariRoute.Screen.Feed.name -> onNavigateToFeed()
                        SolariRoute.Screen.Profile.name -> onNavigateToProfile()
                        SolariRoute.Screen.Conversations.name -> onNavigateBack()
                    }
                }
            )
        }
    ) { innerPadding ->
        val scaffoldBottomPadding = innerPadding.calculateBottomPadding()
        val targetContentBottomPadding = max(
            keyboardBottomPadding.value,
            scaffoldBottomPadding.value
        ).dp
        var displayedContentBottomPadding by remember(chatId) {
            mutableStateOf(targetContentBottomPadding)
        }
        val isKeyboardAboveBottomBar = displayedContentBottomPadding > scaffoldBottomPadding
        var previousTargetContentBottomPadding by remember(chatId) {
            mutableStateOf(targetContentBottomPadding)
        }
        var keyboardRestoreAnchor by remember(chatId) {
            mutableStateOf<ChatViewportAnchor?>(null)
        }

        LaunchedEffect(targetContentBottomPadding, scaffoldBottomPadding, viewModel.isLoadingMessages) {
            if (viewModel.isLoadingMessages) {
                previousTargetContentBottomPadding = targetContentBottomPadding
                displayedContentBottomPadding = targetContentBottomPadding
                keyboardRestoreAnchor = null
                return@LaunchedEffect
            }

            val bottomInsetDelta = targetContentBottomPadding - previousTargetContentBottomPadding
            previousTargetContentBottomPadding = targetContentBottomPadding

            when {
                bottomInsetDelta > 0.dp -> {
                    if (keyboardRestoreAnchor == null) {
                        keyboardRestoreAnchor = messageListState.viewportAnchor()
                    }
                    val shouldKeepBottomVisible = keyboardRestoreAnchor?.isScrolledToBottom == true
                    displayedContentBottomPadding = targetContentBottomPadding
                    if (shouldKeepBottomVisible) {
                        messageListState.scrollToBottomFromCurrentPosition()
                    } else {
                        messageListState.scrollBy(
                            with(density) { bottomInsetDelta.toPx() }
                        )
                    }
                }

                bottomInsetDelta < 0.dp -> {
                    val restoreAnchor = keyboardRestoreAnchor
                    if (restoreAnchor != null) {
                        messageListState.restoreViewportAnchor(restoreAnchor)
                    }
                    displayedContentBottomPadding = scaffoldBottomPadding
                    keyboardRestoreAnchor = null
                }

                targetContentBottomPadding == scaffoldBottomPadding -> {
                    displayedContentBottomPadding = scaffoldBottomPadding
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatBackground)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = displayedContentBottomPadding
                )
        ) {
            ChatHeaderBar(
                chatId = activeChatId,
                partnerName = displayPartnerName,
                partnerUsername = displayPartnerUsername,
                partnerAvatarUrl = displayPartnerAvatarUrl,
                partner = if (isReadOnly) null else partner,
                isSettingsEnabled = !isDraftConversation,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.isLoadingMessages) {
                    CircularProgressIndicator(
                        color = ChatPrimary,
                        trackColor = ChatInput
                    )
                } else {
                    val isMessageListVisible = isMessageScrollInitialized || sortedMessages.isEmpty()

                    LazyColumn(
                        state = messageListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (isMessageListVisible) 1f else 0f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            top = 24.dp,
                            end = 24.dp,
                        )
                    ) {
                        daySections.forEach { section ->
                            item(key = "day-${section.date}") {
                                ChatDayChip(text = section.label)
                            }

                            items(
                                items = section.blocks,
                                key = { block -> "${section.date}-${block.messages.first().id}" }
                            ) { block ->
                                val isFromMe = block.senderId == currentUserId
                                val lastBlockMessage = block.messages.last()

                                ChatMessageBlockRow(
                                    block = block,
                                    partner = visiblePartner,
                                    isFromMe = isFromMe,
                                    isLastBlock = lastMessage?.id == lastBlockMessage.id,
                                    currentUserId = currentUserId,
                                    highlightedMessageId = highlightedMessageId,
                                    recentEmojis = recentEmojis,
                                    areMessageActionsEnabled = !isReadOnly,
                                    onRecordRecentEmoji = viewModel::recordRecentEmoji,
                                    onUnsendMessage = { message ->
                                        viewModel.unsendMessage(activeChatId, message.id)
                                    },
                                    onReactToMessage = { message, emoji ->
                                        viewModel.reactToMessage(message.id, emoji)
                                    },
                                    onReplyToMessage = { message ->
                                        replyingToMessage = message
                                    },
                                    onJumpToMessage = ::jumpToMessage
                                )
                            }
                        }
                    }

                    if (!isMessageListVisible) {
                        CircularProgressIndicator(
                            color = ChatPrimary,
                            trackColor = ChatInput
                        )
                    }
                }
            }

            if (!isReadOnly) {
                ChatInputBar(
                    value = viewModel.messageText,
                    onValueChange = { viewModel.messageText = it },
                    replyingToMessage = replyingToMessage,
                    isKeyboardAboveBottomBar = isKeyboardAboveBottomBar,
                    replyLabel = replyingToMessage?.let { message ->
                        if (message.senderId == currentUserId) {
                            "Replying to yourself"
                        } else {
                            "Replying to ${displayPartnerName}"
                        }
                    },
                    onCancelReply = { replyingToMessage = null },
                    onSend = {
                        val trimmedMessage = viewModel.messageText.trim()
                        if (trimmedMessage.isNotEmpty()) {
                            keyboardRestoreAnchor = null
                            viewModel.sendMessage(activeChatId, repliedMessage = replyingToMessage)
                            replyingToMessage = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatHeaderBar(
    chatId: String,
    partnerName: String,
    partnerUsername: String,
    partnerAvatarUrl: String?,
    partner: User?,
    isSettingsEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (chatId: String, partner: User?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(start = 16.dp, end = 16.dp, bottom = 4.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .scaledClickable(pressedScale = 1.2f, onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ChatPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            SolariAvatar(
                imageUrl = partnerAvatarUrl,
                username = partnerUsername,
                contentDescription = "$partnerName avatar",
                modifier = Modifier
                    .size(40.dp).clip(CircleShape),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = partnerName,
                color = ChatText,
                fontSize = 17.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .scaledClickable(
                        pressedScale = 1.2f,
                        enabled = isSettingsEnabled
                    ) {
                        onNavigateToSettings(chatId, partner)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Chat settings",
                    tint = if (isSettingsEnabled) ChatMuted else ChatMuted.copy(alpha = 0.38f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatDayChip(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(ChatChip)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = ChatReadText,
                fontSize = 12.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChatMessageBlockRow(
    block: ChatMessageBlock,
    partner: User?,
    isFromMe: Boolean,
    isLastBlock: Boolean,
    currentUserId: String?,
    highlightedMessageId: String?,
    recentEmojis: List<String>,
    areMessageActionsEnabled: Boolean,
    onRecordRecentEmoji: (String) -> Unit,
    onUnsendMessage: (Message) -> Unit,
    onReactToMessage: (Message, String) -> Unit,
    onReplyToMessage: (Message) -> Unit,
    onJumpToMessage: (String) -> Unit
) {
    val lastMessage = block.messages.last()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        if (isFromMe) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                block.messages.forEach { message ->
                    ChatBubble(
                        message = message,
                        isFromMe = true,
                        isHighlighted = message.id == highlightedMessageId,
                        partnerName = partner?.displayName.orEmpty(),
                        currentUserId = currentUserId,
                        recentEmojis = recentEmojis,
                        areActionsEnabled = areMessageActionsEnabled,
                        onRecordRecentEmoji = onRecordRecentEmoji,
                        onUnsendMessage = onUnsendMessage,
                        onReactToMessage = onReactToMessage,
                        onReplyToMessage = onReplyToMessage,
                        onJumpToMessage = onJumpToMessage
                    )
                }
            }

            if (isLastBlock) {
                Text(
                    text = lastMessage.deliveryFooterText(),
                    color = ChatReadText,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp, bottom = 8.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SolariAvatar(
                    imageUrl = partner?.profileImageUrl,
                    username = partner?.username.orEmpty(),
                    contentDescription = partner?.let { "${it.displayName} avatar" },
                    modifier = Modifier
                        .size(36.dp).clip(CircleShape),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    block.messages.forEach { message ->
                        ChatBubble(
                            message = message,
                            isFromMe = false,
                            isHighlighted = message.id == highlightedMessageId,
                            partnerName = partner?.displayName.orEmpty(),
                            currentUserId = currentUserId,
                            recentEmojis = recentEmojis,
                            areActionsEnabled = areMessageActionsEnabled,
                            onRecordRecentEmoji = onRecordRecentEmoji,
                            onUnsendMessage = onUnsendMessage,
                            onReactToMessage = onReactToMessage,
                            onReplyToMessage = onReplyToMessage,
                            onJumpToMessage = onJumpToMessage
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    message: Message,
    isFromMe: Boolean,
    isHighlighted: Boolean,
    partnerName: String,
    currentUserId: String?,
    recentEmojis: List<String>,
    areActionsEnabled: Boolean,
    onRecordRecentEmoji: (String) -> Unit,
    onUnsendMessage: (Message) -> Unit,
    onReactToMessage: (Message, String) -> Unit,
    onReplyToMessage: (Message) -> Unit,
    onJumpToMessage: (String) -> Unit
) {
    var isActionMenuExpanded by remember { mutableStateOf(false) }
    var isEmojiPickerExpanded by remember { mutableStateOf(false) }
    var isConfirmingUnsend by remember { mutableStateOf(false) }
    var dragOffsetPx by remember(message.id) { mutableStateOf(0f) }
    var isReplySwipeArmed by remember(message.id) { mutableStateOf(false) }
    var hasReplySwipeHapticFired by remember(message.id) { mutableStateOf(false) }
    val bubbleInteractionSource = remember(message.id) { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    val replySwipeThresholdPx = with(LocalDensity.current) { 180.dp.toPx() }
    val bubbleShape = RoundedCornerShape(12.dp)
    val highlightAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 0.12f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "messageHighlightAlpha"
    )
    val currentUserReactionEmoji = message.reactions
        .firstOrNull { it.userId == currentUserId }
        ?.emoji
    fun reactWithRecent(emoji: String) {
        onRecordRecentEmoji(emoji)
        onReactToMessage(message, emoji)
    }

    val hasReactions = !message.isDeleted && message.reactions.isNotEmpty()

    Box(
        modifier = Modifier
            .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
            .widthIn(max = if (isFromMe) 292.dp else 248.dp)
            .padding(bottom = if (hasReactions) 8.dp else 0.dp)
            .then(
                if (message.isDeleted || !areActionsEnabled) {
                    Modifier
                } else {
                    Modifier.pointerInput(message.id, isFromMe) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                dragOffsetPx = 0f
                                isReplySwipeArmed = false
                                hasReplySwipeHapticFired = false
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                dragOffsetPx += dragAmount
                                val isPastReplyThreshold = if (isFromMe) {
                                    dragOffsetPx <= -replySwipeThresholdPx
                                } else {
                                    dragOffsetPx >= replySwipeThresholdPx
                                }
                                isReplySwipeArmed = isPastReplyThreshold
                                if (isPastReplyThreshold && !hasReplySwipeHapticFired) {
                                    hasReplySwipeHapticFired = true
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            },
                            onDragEnd = {
                                if (isReplySwipeArmed) {
                                    onReplyToMessage(message)
                                }
                                dragOffsetPx = 0f
                                isReplySwipeArmed = false
                                hasReplySwipeHapticFired = false
                            },
                            onDragCancel = {
                                dragOffsetPx = 0f
                                isReplySwipeArmed = false
                                hasReplySwipeHapticFired = false
                            }
                        )
                    }
                }
            )
    ) {
        Box {
            Box(
                modifier = Modifier
                    .scaleOnPress(
                        interactionSource = bubbleInteractionSource,
                        pressedScale = 1.1f
                    )
                    .clip(bubbleShape)
                    .then(
                        if (message.isDeleted) {
                            Modifier.border(
                                width = 1.dp,
                                color = ChatMuted.copy(alpha = 0.45f),
                                shape = bubbleShape
                            )
                        } else {
                            Modifier.background(if (isFromMe) ChatOutgoingBubble else ChatIncomingBubble)
                        }
                    )
                    .then(
                        if (highlightAlpha > 0f) {
                            Modifier.background(Color.White.copy(alpha = highlightAlpha), bubbleShape)
                        } else {
                            Modifier
                        }
                    )
                    .then(
                        if (message.isDeleted || !areActionsEnabled) {
                            Modifier
                        } else {
                            Modifier.combinedClickable(
                                interactionSource = bubbleInteractionSource,
                                indication = null,
                                onClick = {},
                                onDoubleClick = { reactWithRecent("❤️") },
                                onLongClick = { isActionMenuExpanded = true }
                            )
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!message.isDeleted) {
                        MessageContextPreview(
                            message = message,
                            isFromMe = isFromMe,
                            partnerName = partnerName,
                            isClickEnabled = areActionsEnabled,
                            onReplyPreviewClick = onJumpToMessage
                        )
                    }

                    Text(
                        text = message.text,
                        color = if (message.isDeleted) ChatText.copy(alpha = 0.8f) else ChatText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Normal,
                        fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal
                    )
                }
            }

            if (hasReactions) {
                MessageReactionPill(
                    reactions = message.reactions,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(
                            y = 8.dp
                        )
                        .zIndex(1f)
                )
            }
        }

        if (isActionMenuExpanded && !message.isDeleted && areActionsEnabled) {
            MessageActionPopup(
                isFromMe = isFromMe,
                onDismiss = { isActionMenuExpanded = false },
                selectedEmoji = currentUserReactionEmoji,
                onReact = { emoji ->
                    isActionMenuExpanded = false
                    reactWithRecent(emoji)
                },
                onOpenEmojiPicker = {
                    isActionMenuExpanded = false
                    isEmojiPickerExpanded = true
                },
                onUnsend = {
                    isActionMenuExpanded = false
                    isConfirmingUnsend = true
                }
            )
        }

        if (isEmojiPickerExpanded && !message.isDeleted && areActionsEnabled) {
            EmojiPickerPopup(
                onClosed = { isEmojiPickerExpanded = false },
                selectedEmoji = currentUserReactionEmoji,
                recentEmojis = recentEmojis,
                onReact = { emoji ->
                    reactWithRecent(emoji)
                }
            )
        }
    }

    if (isConfirmingUnsend) {
        SolariConfirmationDialog(
            title = "Unsend message?",
            message = "This message will be removed from the conversation.",
            confirmText = "Unsend",
            onConfirm = {
                isConfirmingUnsend = false
                onUnsendMessage(message)
            },
            onDismiss = { isConfirmingUnsend = false }
        )
    }
}

@Composable
private fun MessageContextPreview(
    message: Message,
    isFromMe: Boolean,
    partnerName: String,
    isClickEnabled: Boolean,
    onReplyPreviewClick: (String) -> Unit
) {
    val hasReferencedPost = message.referencedPostId != null
    val replyPreview = message.repliedMessagePreview
    val replyLabel = if (isFromMe) {
        "You replied to ${partnerName.ifBlank { "them" }}"
    } else {
        "${partnerName.ifBlank { "They" }} replied to you"
    }

    if (!hasReferencedPost && replyPreview == null) {
        return
    }

    Column(
        modifier = Modifier
            .widthIn(min = 160.dp, max = 220.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (hasReferencedPost) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChatHeader),
                    contentAlignment = Alignment.Center
                ) {
                    if (message.referencedPostThumbnailUrl.isNullOrBlank()) {
                        Text(
                            text = "Post",
                            color = ChatMuted,
                            fontSize = 10.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        AsyncImage(
                            model = message.referencedPostThumbnailUrl,
                            contentDescription = "Referenced post thumbnail",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Replied to a post",
                    color = ChatText.copy(alpha = 0.86f),
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (replyPreview != null) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isClickEnabled) {
                            Modifier.clickable {
                                message.repliedMessageId?.let(onReplyPreviewClick)
                            }
                        } else {
                            Modifier
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = ChatMuted.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = null,
                        tint = ChatPrimary,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = replyLabel,
                        color = ChatPrimary,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = replyPreview,
                    color = ChatText.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MessageReactionPill(
    reactions: List<MessageReaction>,
    modifier: Modifier = Modifier
) {
    val groupedReactions = reactions.groupingBy { it.emoji }.eachCount()
    val text = groupedReactions.entries.joinToString(" ") { (emoji, count) ->
        if (count > 1) "$emoji $count" else emoji
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ChatReactionSurface)
            .padding(horizontal = 3.dp)
    ) {
        Text(
            text = text,
            color = ChatText,
            fontSize = 12.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )
    }
}

@Composable
private fun MessageActionPopup(
    isFromMe: Boolean,
    onDismiss: () -> Unit,
    selectedEmoji: String?,
    onReact: (String) -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onUnsend: () -> Unit
) {
    Popup(
        alignment = Alignment.TopCenter,
        offset = IntOffset(0, if (isFromMe) -240 else -120),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            color = ChatHeader,
            shape = RoundedCornerShape(22.dp),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickReactionEmojis.forEachIndexed { index, emoji ->
                        AnimatedReactionOptionButton(
                            text = emoji,
                            selected = emoji == selectedEmoji,
                            delayMillis = index * 20,
                            onClick = { onReact(emoji) }
                        )
                    }

                    AnimatedReactionOptionButton(
                        text = "+",
                        selected = false,
                        delayMillis = QuickReactionEmojis.size * 20,
                        onClick = onOpenEmojiPicker
                    )
                }

                if (isFromMe) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .scaledClickable(pressedScale = 1.1f, onClick = onUnsend)
                            .padding(horizontal = 18.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unsend",
                            color = ChatText,
                            fontSize = 13.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmojiPickerPopup(
    onClosed: () -> Unit,
    selectedEmoji: String?,
    recentEmojis: List<String>,
    onReact: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var panelVisible by remember { mutableStateOf(false) }
    var isSheetDragging by remember { mutableStateOf(false) }
    var sheetDragOffsetPx by remember { mutableStateOf(0f) }
    val visibleCategories = remember(searchQuery) {
        filterEmojiCategories(searchQuery)
    }
    val showRecentEmojis = searchQuery.isBlank() && recentEmojis.isNotEmpty()
    val animatedSheetDragOffsetPx by animateFloatAsState(
        targetValue = sheetDragOffsetPx,
        animationSpec = tween(durationMillis = if (isSheetDragging) 0 else 180),
        label = "emojiSheetDragOffset"
    )

    fun dismissWithAnimation() {
        panelVisible = false
    }

    LaunchedEffect(Unit) {
        panelVisible = true
    }

    LaunchedEffect(panelVisible) {
        if (!panelVisible) {
            delay(220)
            onClosed()
        }
    }

    Popup(
        alignment = Alignment.BottomCenter,
        onDismissRequest = { dismissWithAnimation() },
        properties = PopupProperties(focusable = true)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = panelVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(animationSpec = tween(durationMillis = 180))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.48f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { dismissWithAnimation() }
                        )
                )
            }

            AnimatedVisibility(
                visible = panelVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 220),
                    targetOffsetY = { it }
                )
            ) {
                Surface(
                    color = EmojiPickerPanel,
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.72f)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = animatedSheetDragOffsetPx.roundToInt()
                            )
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        EmojiPickerHandle(
                            onDragStart = {
                                isSheetDragging = true
                            },
                            onDrag = { dragAmount ->
                                sheetDragOffsetPx = (sheetDragOffsetPx + dragAmount).coerceAtLeast(0f)
                            },
                            onDragEnd = {
                                isSheetDragging = false
                                if (sheetDragOffsetPx > 80f) {
                                    dismissWithAnimation()
                                } else {
                                    sheetDragOffsetPx = 0f
                                }
                            },
                            onDragCancel = {
                                isSheetDragging = false
                                sheetDragOffsetPx = 0f
                            }
                        )

                        EmojiSearchField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        )

                        Text(
                            text = "Your reactions",
                            color = EmojiPickerMuted,
                            fontSize = 17.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 22.dp, top = 8.dp, bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuickReactionEmojis.forEach { emoji ->
                                EmojiPickerEmojiButton(
                                    emoji = emoji,
                                    selected = emoji == selectedEmoji,
                                    onClick = {
                                        onReact(emoji)
                                        dismissWithAnimation()
                                    }
                                )
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 16.dp),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (showRecentEmojis) {
                                item(
                                    key = "recent-emojis-title",
                                    span = { GridItemSpan(maxLineSpan) }
                                ) {
                                    Text(
                                        text = "Recent emojis",
                                        color = EmojiPickerMuted,
                                        fontSize = 17.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
                                    )
                                }

                                items(
                                    items = recentEmojis,
                                    key = { emoji -> "recent-$emoji" }
                                ) { emoji ->
                                    EmojiPickerEmojiButton(
                                        emoji = emoji,
                                        selected = emoji == selectedEmoji,
                                        onClick = {
                                            onReact(emoji)
                                            dismissWithAnimation()
                                        }
                                    )
                                }
                            }

                            visibleCategories.forEach { category ->
                                item(
                                    key = "group-${category.name}",
                                    span = { GridItemSpan(maxLineSpan) }
                                ) {
                                    Text(
                                        text = category.name,
                                        color = EmojiPickerMuted,
                                        fontSize = 17.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 8.dp)
                                    )
                                }

                                items(
                                    items = category.emojis,
                                    key = { item -> "${category.name}-${item.emoji}-${item.name}" }
                                ) { item ->
                                    EmojiPickerEmojiButton(
                                        emoji = item.emoji,
                                        selected = item.emoji == selectedEmoji,
                                        onClick = {
                                            onReact(item.emoji)
                                            dismissWithAnimation()
                                        }
                                    )
                                }
                            }

                            if (visibleCategories.isEmpty()) {
                                item(
                                    key = "emoji-empty-state",
                                    span = { GridItemSpan(maxLineSpan) }
                                ) {
                                    Text(
                                        text = "No emojis found",
                                        color = EmojiPickerMuted,
                                        fontSize = 16.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun EmojiPickerHandle(
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onVerticalDrag = { _, dragAmount ->
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF8C8C8E))
        )
    }
}

@Composable
private fun EmojiSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(EmojiPickerSearch)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = EmojiPickerMuted,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(ChatText),
            textStyle = TextStyle(
                color = ChatText,
                fontSize = 18.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isBlank()) {
                        Text(
                            text = "Search",
                            color = EmojiPickerMuted,
                            fontSize = 18.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun EmojiPickerEmojiButton(
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White.copy(alpha = 0.12f) else Color.Transparent)
            .scaledClickable(pressedScale = 1.1f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 30.sp,
            fontFamily = PlusJakartaSans
        )
    }
}

private fun filterEmojiCategories(query: String): List<EmojiCatalogCategory> {
    val trimmedQuery = query.trim()
    if (trimmedQuery.isBlank()) return EmojiCatalog.categories

    val normalizedQuery = trimmedQuery.lowercase()
    return EmojiCatalog.categories.mapNotNull { category ->
        val filteredEmojis = category.emojis.filter { item ->
            item.emoji.contains(trimmedQuery) ||
                    item.name.contains(normalizedQuery, ignoreCase = true) ||
                    category.name.contains(normalizedQuery, ignoreCase = true)
        }

        if (filteredEmojis.isEmpty()) {
            null
        } else {
            EmojiCatalogCategory(
                name = category.name,
                emojis = filteredEmojis
            )
        }
    }
}

@Composable
private fun AnimatedReactionOptionButton(
    text: String,
    selected: Boolean,
    delayMillis: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "reactionOptionScale"
    )

    ReactionOptionButton(
        text = text,
        selected = selected,
        modifier = Modifier.scale(scale),
        onClick = {
            if (visible) {
                onClick()
            }
        }
    )
}

@Composable
private fun ReactionOptionButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (selected) ChatPrimary.copy(alpha = 0.20f) else Color.Transparent)
            .scaledClickable(pressedScale = 1.1f, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = ChatText,
            fontSize = if (text == "+") 20.sp else 18.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    replyingToMessage: Message?,
    isKeyboardAboveBottomBar: Boolean,
    replyLabel: String?,
    onCancelReply: () -> Unit,
    onSend: () -> Unit
) {
    val bottomPadding = if (isKeyboardAboveBottomBar) 16.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = bottomPadding)
            .clip(RoundedCornerShape(12.dp))
            .background(ChatInput)
            .padding(start = 20.dp, end = 8.dp, top = if (replyingToMessage == null) 0.dp else 10.dp, bottom = 0.dp)
    ) {
        if (replyingToMessage != null && replyLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = replyLabel,
                        color = ChatPrimary,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = replyingToMessage.text,
                        color = ChatMuted,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSans,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onCancelReply),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel reply",
                        tint = ChatMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = ChatText,
                    fontSize = 15.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(ChatText),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Type a message",
                                color = ChatMuted,
                                fontSize = 14.sp,
                                fontFamily = PlusJakartaSans,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scaledClickable(pressedScale = 1.1f, onClick = onSend)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChatPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = Color(0xFF5F2900),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun groupConsecutiveMessages(messages: List<Message>): List<ChatMessageBlock> {
    if (messages.isEmpty()) return emptyList()

    val blocks = mutableListOf<ChatMessageBlock>()
    var currentSenderId = messages.first().senderId
    val currentMessages = mutableListOf<Message>()

    messages.forEach { message ->
        if (message.senderId != currentSenderId && currentMessages.isNotEmpty()) {
            blocks.add(ChatMessageBlock(currentSenderId, currentMessages.toList()))
            currentMessages.clear()
            currentSenderId = message.senderId
        }
        currentMessages.add(message)
    }

    if (currentMessages.isNotEmpty()) {
        blocks.add(ChatMessageBlock(currentSenderId, currentMessages.toList()))
    }

    return blocks
}

private fun buildChatDaySections(messages: List<Message>): List<ChatDaySection> {
    if (messages.isEmpty()) return emptyList()

    val zoneId = ZoneId.systemDefault()
    return messages
        .groupBy { message -> message.timestamp.toLocalDate(zoneId) }
        .toSortedMap()
        .map { (date, dayMessages) ->
            ChatDaySection(
                date = date,
                label = date.toChatDayLabel(),
                blocks = groupConsecutiveMessages(dayMessages.sortedBy { it.timestamp })
            )
        }
}

private fun buildMessageItemIndexMap(daySections: List<ChatDaySection>): Map<String, Int> {
    val indexes = mutableMapOf<String, Int>()
    var itemIndex = 0

    daySections.forEach { section ->
        itemIndex += 1 // Day chip.
        section.blocks.forEach { block ->
            block.messages.forEach { message ->
                indexes[message.id] = itemIndex
            }
            itemIndex += 1
        }
    }

    return indexes
}

private fun LazyListState.viewportAnchor(): ChatViewportAnchor {
    return ChatViewportAnchor(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
        isScrolledToBottom = isScrolledToBottom()
    )
}

private fun LazyListState.isScrolledToBottom(): Boolean {
    val layoutInfo = layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    if (totalItemsCount == 0) return true

    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    return lastVisibleItem.index == totalItemsCount - 1 &&
            lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
}

private suspend fun LazyListState.restoreViewportAnchor(anchor: ChatViewportAnchor) {
    scrollToItem(
        index = anchor.firstVisibleItemIndex,
        scrollOffset = anchor.firstVisibleItemScrollOffset
    )
}

private fun Message.deliveryFooterText(): String {
    return when (deliveryState) {
        MessageDeliveryState.Sending -> "sending"
        MessageDeliveryState.Sent -> sentFooterText(timestamp)
    }
}

private fun sentFooterText(timestamp: Long): String {
    val ageMillis = max(0L, System.currentTimeMillis() - timestamp)
    if (ageMillis <= MinuteMillis) return "sent"

    val (amount, suffix) = when {
        ageMillis < HourMillis -> ageMillis / MinuteMillis to "m"
        ageMillis < DayMillis -> ageMillis / HourMillis to "h"
        else -> ageMillis / DayMillis to "d"
    }
    return "sent $amount$suffix ago"
}

private suspend fun LazyListState.scrollToBottomFromCurrentPosition() {
    repeat(MaxScrollToBottomPasses) {
        val consumed = scrollBy(MessageSendScrollDeltaPx)
        if (consumed == 0f) return
    }
}

private suspend fun LazyListState.scrollToMessageBottom(itemIndex: Int) {
    scrollToItem(itemIndex)

    repeat(MaxScrollToBottomPasses) {
        withFrameNanos { }

        val viewportHeight = layoutInfo.viewportSize.height
        val scrollDelta = if (viewportHeight > 0) {
            viewportHeight.toFloat()
        } else {
            10_000f
        }
        val consumed = scrollBy(scrollDelta)
        if (consumed == 0f) return
    }
}

private fun Long.toLocalDate(zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(this)
        .atZone(zoneId)
        .toLocalDate()
}

private fun LocalDate.toChatDayLabel(): String {
    val today = LocalDate.now()
    val daysAgo = ChronoUnit.DAYS.between(this, today)

    return when {
        daysAgo == 0L -> "Today"
        daysAgo in 1L..6L -> dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        else -> format(ChatDateFormatter)
    }
}

private val ChatDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
