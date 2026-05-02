package com.solari.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.unit.Dp
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
import com.solari.app.ui.theme.SolariTheme
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

private val ChatBackground @Composable get() = SolariTheme.colors.background
private val ChatHeader @Composable get() = SolariTheme.colors.surface
private val ChatIncomingBubble @Composable get() = SolariTheme.colors.surfaceVariant
private val ChatOutgoingBubble @Composable get() = SolariTheme.colors.secondary
private val ChatInput @Composable get() = SolariTheme.colors.surfaceVariant
private val ChatPrimary @Composable get() = SolariTheme.colors.primary
private val ChatText @Composable get() = SolariTheme.colors.onBackground
private val ChatMuted @Composable get() = SolariTheme.colors.onSurfaceVariant
private val ChatChip @Composable get() = SolariTheme.colors.surfaceVariant
private val ChatReadText @Composable get() = SolariTheme.colors.onSurface
private val ChatReactionSurface @Composable get() = SolariTheme.colors.surfaceVariant
private val EmojiPickerPanel @Composable get() = SolariTheme.colors.surface
private val EmojiPickerSearch @Composable get() = SolariTheme.colors.surfaceVariant
private val EmojiPickerMuted @Composable get() = SolariTheme.colors.onSurfaceVariant
private const val MaxScrollToBottomPasses = 12
private const val OlderMessagesTriggerRemainingMessageCount = 30
private const val MinuteMillis = 60_000L
private const val HourMillis = 60L * MinuteMillis
private const val DayMillis = 24L * HourMillis
private const val MessageJumpHighlightDurationMillis = 1_500

private val QuickReactionEmojis = listOf("❤️", "😂", "😮", "😢", "😡", "👍")

private sealed interface ChatListItem {
    val key: String
}

private data class ChatDayHeaderItem(
    val date: LocalDate,
    val label: String
) : ChatListItem {
    override val key: String = chatDayItemKey(date)
}

private data class ChatMessageItem(
    val message: Message,
    val isFromMe: Boolean,
    val showIncomingAvatar: Boolean,
    val showDeliveryFooter: Boolean
) : ChatListItem {
    override val key: String = chatMessageItemKey(message.id)
}

private data class ChatViewportAnchor(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val isScrolledToBottom: Boolean
)

private data class ChatListItemAnchor(
    val itemKey: String,
    val itemScrollOffset: Int
)

private data class ChatScrollSnapshot(
    val totalItemsCount: Int,
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val isScrolledToBottom: Boolean
)

private data class ChatListModel(
    val items: List<ChatListItem>,
    val messageItemIndexes: Map<String, Int>,
    val messageOrdinalsById: Map<String, Int>,
    val listItemIndexes: Map<String, Int>,
    val lastListItemIndex: Int,
    val messageListItemCount: Int
)

private data class ChatContentPaddingState(
    val bottomPadding: Dp,
    val isKeyboardAboveBottomBar: Boolean
)

private class ChatMessageListState(
    val listState: LazyListState,
    initialMessageCount: Int,
    initialLastMessageId: String?
) {
    var previousMessageCount by mutableStateOf(initialMessageCount)
    var previousLastMessageId by mutableStateOf(initialLastMessageId)
    var lastBottomVisibleMessageId by mutableStateOf(initialLastMessageId)
    var shouldKeepChatPinnedToBottom by mutableStateOf(false)
    var isMessageScrollInitialized by mutableStateOf(false)
    var hasSeenInitialMessageLoad by mutableStateOf(false)
    var hasFinishedInitialMessageLoad by mutableStateOf(false)
    var highlightedMessageId by mutableStateOf<String?>(null)
    var olderMessageRestoreAnchor by mutableStateOf<ChatListItemAnchor?>(null)
    var pendingJumpToMessageId by mutableStateOf<String?>(null)
}

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
    val canSendTypingState = !isReadOnly && !isDraftConversation && currentConversation != null
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val keyboardBottomPadding = with(density) { WindowInsets.ime.getBottom(density).toDp() }
    val allMessages = currentConversation?.messages.orEmpty()
    val sortedMessages = remember(allMessages) { allMessages.sortedBy { it.timestamp } }
    val lastMessage = sortedMessages.lastOrNull()
    val isPartnerTyping = viewModel.isPartnerTyping && !isReadOnly
    val recentEmojis = viewModel.recentEmojis
    val chatListModel = rememberChatListModel(
        sortedMessages = sortedMessages,
        currentUserId = currentUserId,
        isPartnerTyping = isPartnerTyping
    )
    val chatMessageListState = rememberChatMessageListState(
        chatId = chatId,
        initialMessageCount = sortedMessages.size,
        initialLastMessageId = lastMessage?.id
    )
    val messageListState = chatMessageListState.listState
    val coroutineScope = rememberCoroutineScope()
    var keyboardAnchorResetToken by remember(chatId) { mutableStateOf(0) }
    var replyingToMessage by remember(chatId) { mutableStateOf<Message?>(null) }
    val jumpToMessage = rememberJumpToMessage(
        messageItemIndexes = chatListModel.messageItemIndexes,
        messageListState = messageListState,
        onHighlightMessage = { chatMessageListState.highlightedMessageId = it }
    )

    BindChatMessageListEffects(
        state = chatMessageListState,
        activeChatId = activeChatId,
        viewModel = viewModel,
        sortedMessages = sortedMessages,
        lastMessage = lastMessage,
        currentUserId = currentUserId,
        isReadOnly = isReadOnly,
        isDraftConversation = isDraftConversation,
        isPartnerTyping = isPartnerTyping,
        lastListItemIndex = chatListModel.lastListItemIndex,
        messageListItemCount = chatListModel.messageListItemCount,
        messageOrdinalsById = chatListModel.messageOrdinalsById,
        listItemIndexes = chatListModel.listItemIndexes
    )

    BackHandler(onBack = onNavigateBack)

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
        val contentPaddingState = rememberChatContentPaddingState(
            chatId = chatId,
            targetContentBottomPadding = targetContentBottomPadding,
            scaffoldBottomPadding = scaffoldBottomPadding,
            isLoadingMessages = viewModel.isLoadingMessages,
            lastListItemIndex = chatListModel.lastListItemIndex,
            lastMessageId = lastMessage?.id,
            keyboardAnchorResetToken = keyboardAnchorResetToken,
            state = chatMessageListState
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatBackground)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPaddingState.bottomPadding
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
                    val isMessageListVisible = chatMessageListState.isMessageScrollInitialized || sortedMessages.isEmpty()

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
                        items(
                            items = chatListModel.items,
                            key = { item -> item.key }
                        ) { item ->
                            when (item) {
                                is ChatDayHeaderItem -> {
                                    ChatDayChip(text = item.label)
                                }

                                is ChatMessageItem -> {
                                    ChatMessageRow(
                                        item = item,
                                        partner = visiblePartner,
                                        currentUserId = currentUserId,
                                        partnerLastReadAt = currentConversation?.partnerLastReadAt,
                                        highlightedMessageId = chatMessageListState.highlightedMessageId,
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
                                        onJumpToMessage = { messageId ->
                                            if (!jumpToMessage(messageId)) {
                                                chatMessageListState.pendingJumpToMessageId = messageId
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        if (isPartnerTyping) {
                            item(key = TypingIndicatorItemKey) {
                                TypingIndicatorRow(
                                    partner = visiblePartner,
                                    partnerName = displayPartnerName,
                                    partnerUsername = displayPartnerUsername,
                                    partnerAvatarUrl = displayPartnerAvatarUrl
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

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = viewModel.isLoadingOlderMessages,
                            enter = fadeIn(animationSpec = tween(durationMillis = 120)),
                            exit = fadeOut(animationSpec = tween(durationMillis = 120))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = ChatPrimary,
                                trackColor = ChatInput
                            )
                        }
                    }
                }
            }

            if (!isReadOnly) {
                ChatInputBar(
                    value = viewModel.messageText,
                    onValueChange = { value ->
                        viewModel.onMessageTextChanged(
                            chatId = activeChatId,
                            receiverId = partner?.id,
                            value = value,
                            canSendTypingState = canSendTypingState
                        )
                    },
                    replyingToMessage = replyingToMessage,
                    isKeyboardAboveBottomBar = contentPaddingState.isKeyboardAboveBottomBar,
                    replyLabel = replyingToMessage?.let { message ->
                        if (message.senderId == currentUserId) {
                            "Replying to yourself"
                        } else {
                            "Replying to $displayPartnerName"
                        }
                    },
                    onCancelReply = { replyingToMessage = null },
                    onInputFocused = {
                        chatMessageListState.shouldKeepChatPinnedToBottom = true
                        if (chatMessageListState.isMessageScrollInitialized && chatListModel.lastListItemIndex >= 0) {
                            coroutineScope.launch {
                                messageListState.scrollToMessageBottom(chatListModel.lastListItemIndex)
                                chatMessageListState.lastBottomVisibleMessageId = lastMessage?.id
                            }
                        }
                    },
                    onSend = {
                        val trimmedMessage = viewModel.messageText.trim()
                        if (trimmedMessage.isNotEmpty()) {
                            chatMessageListState.shouldKeepChatPinnedToBottom = true
                            keyboardAnchorResetToken += 1
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
private fun rememberChatListModel(
    sortedMessages: List<Message>,
    currentUserId: String?,
    isPartnerTyping: Boolean
): ChatListModel {
    return remember(sortedMessages, currentUserId, isPartnerTyping) {
        val items = buildChatListItems(sortedMessages, currentUserId)
        val lastMessageItemIndex = items.lastIndex
        ChatListModel(
            items = items,
            messageItemIndexes = buildMessageItemIndexMap(items),
            messageOrdinalsById = sortedMessages.withIndex().associate { indexedValue ->
                indexedValue.value.id to indexedValue.index
            },
            listItemIndexes = buildListItemIndexMap(items, isPartnerTyping),
            lastListItemIndex = lastMessageItemIndex + if (isPartnerTyping) 1 else 0,
            messageListItemCount = max(0, lastMessageItemIndex + 1)
        )
    }
}

@Composable
private fun rememberChatMessageListState(
    chatId: String,
    initialMessageCount: Int,
    initialLastMessageId: String?
): ChatMessageListState {
    val listState = rememberLazyListState()
    return remember(chatId, listState) {
        ChatMessageListState(
            listState = listState,
            initialMessageCount = initialMessageCount,
            initialLastMessageId = initialLastMessageId
        )
    }
}

@Composable
private fun rememberJumpToMessage(
    messageItemIndexes: Map<String, Int>,
    messageListState: LazyListState,
    onHighlightMessage: (String) -> Unit
): (String) -> Boolean {
    val coroutineScope = rememberCoroutineScope()
    return remember(messageItemIndexes, messageListState, coroutineScope, onHighlightMessage) {
        { messageId ->
            val targetIndex = messageItemIndexes[messageId]
            if (targetIndex != null) {
                onHighlightMessage(messageId)
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
                true
            } else {
                false
            }
        }
    }
}

@Composable
private fun BindChatMessageListEffects(
    state: ChatMessageListState,
    activeChatId: String,
    viewModel: ChatViewModel,
    sortedMessages: List<Message>,
    lastMessage: Message?,
    currentUserId: String?,
    isReadOnly: Boolean,
    isDraftConversation: Boolean,
    isPartnerTyping: Boolean,
    lastListItemIndex: Int,
    messageListItemCount: Int,
    messageOrdinalsById: Map<String, Int>,
    listItemIndexes: Map<String, Int>
) {
    val messageListState = state.listState
    val latestLastMessageId by rememberUpdatedState(lastMessage?.id)

    fun requestOlderMessagesLoad() {
        if (!viewModel.canLoadOlderMessages ||
            viewModel.isLoadingOlderMessages ||
            viewModel.isLoadingMessages ||
            !state.isMessageScrollInitialized
        ) {
            return
        }

        val restoreAnchor = messageListState.olderMessagesRestoreAnchor() ?: return
        state.olderMessageRestoreAnchor = restoreAnchor
        if (!viewModel.loadOlderMessages()) {
            state.olderMessageRestoreAnchor = null
        }
    }

    LaunchedEffect(state.highlightedMessageId) {
        if (state.highlightedMessageId == null) return@LaunchedEffect
        delay(MessageJumpHighlightDurationMillis.toLong())
        state.highlightedMessageId = null
    }

    LaunchedEffect(activeChatId) {
        state.pendingJumpToMessageId = null
    }

    DisposableEffect(activeChatId, isReadOnly, isDraftConversation) {
        onDispose {
            viewModel.stopTypingIndicator()
        }
    }

    LaunchedEffect(isDraftConversation, sortedMessages.isEmpty()) {
        if (isDraftConversation) {
            state.hasSeenInitialMessageLoad = true
            state.hasFinishedInitialMessageLoad = true
            state.isMessageScrollInitialized = sortedMessages.isEmpty()
        }
    }

    LaunchedEffect(viewModel.isLoadingMessages) {
        if (viewModel.isLoadingMessages) {
            state.hasSeenInitialMessageLoad = true
            state.hasFinishedInitialMessageLoad = false
            state.isMessageScrollInitialized = false
        } else if (state.hasSeenInitialMessageLoad) {
            state.hasFinishedInitialMessageLoad = true
        }
    }

    LaunchedEffect(messageListState, state.isMessageScrollInitialized) {
        if (!state.isMessageScrollInitialized) return@LaunchedEffect

        var observedListItemCount = messageListState.layoutInfo.totalItemsCount
        var observedTailMessageId = latestLastMessageId
        snapshotFlow {
            ChatScrollSnapshot(
                totalItemsCount = messageListState.layoutInfo.totalItemsCount,
                firstVisibleItemIndex = messageListState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = messageListState.firstVisibleItemScrollOffset,
                isScrolledToBottom = messageListState.isScrolledToBottom()
            )
        }.collect { snapshot ->
            val didAppendListItem = snapshot.totalItemsCount > observedListItemCount
            val didChangeTailMessage = latestLastMessageId != observedTailMessageId
            observedListItemCount = snapshot.totalItemsCount
            observedTailMessageId = latestLastMessageId

            when {
                snapshot.isScrolledToBottom -> {
                    state.lastBottomVisibleMessageId = latestLastMessageId
                    state.shouldKeepChatPinnedToBottom = true
                }

                !didAppendListItem && !didChangeTailMessage -> {
                    state.lastBottomVisibleMessageId = null
                    state.shouldKeepChatPinnedToBottom = false
                }
            }
        }
    }

    LaunchedEffect(
        messageListState,
        state.isMessageScrollInitialized,
        viewModel.canLoadOlderMessages,
        viewModel.isLoadingOlderMessages,
        viewModel.isLoadingMessages,
        messageListItemCount,
        messageOrdinalsById
    ) {
        if (state.pendingJumpToMessageId != null) return@LaunchedEffect
        if (!state.isMessageScrollInitialized || viewModel.isLoadingMessages || messageListItemCount == 0) {
            return@LaunchedEffect
        }

        snapshotFlow {
            messageListState.layoutInfo.visibleItemsInfo
                .firstOrNull { itemInfo ->
                    (itemInfo.key as? String)?.startsWith("message-") == true
                }
                ?.key as? String
        }.collect { firstVisibleMessageKey ->
            val firstVisibleMessageId = firstVisibleMessageKey
                ?.removePrefix("message-")
                ?.takeIf { it != firstVisibleMessageKey }
            val firstVisibleMessageOrdinal =
                firstVisibleMessageId?.let(messageOrdinalsById::get) ?: return@collect

            if (viewModel.canLoadOlderMessages &&
                !viewModel.isLoadingOlderMessages &&
                firstVisibleMessageOrdinal <= OlderMessagesTriggerRemainingMessageCount
            ) {
                requestOlderMessagesLoad()
            }
        }
    }

    LaunchedEffect(
        viewModel.isLoadingOlderMessages,
        state.isMessageScrollInitialized,
        messageListState
    ) {
        if (state.pendingJumpToMessageId != null) return@LaunchedEffect
        if (!viewModel.isLoadingOlderMessages || !state.isMessageScrollInitialized) return@LaunchedEffect

        snapshotFlow { messageListState.olderMessagesRestoreAnchor() }
            .collect { anchor ->
                if (anchor != null) {
                    state.olderMessageRestoreAnchor = anchor
                }
            }
    }

    LaunchedEffect(
        viewModel.isLoadingOlderMessages,
        state.olderMessageRestoreAnchor,
        listItemIndexes,
        state.pendingJumpToMessageId
    ) {
        if (viewModel.isLoadingOlderMessages) return@LaunchedEffect
        if (state.pendingJumpToMessageId != null) {
            state.olderMessageRestoreAnchor = null
            return@LaunchedEffect
        }

        state.olderMessageRestoreAnchor?.let { anchor ->
            listItemIndexes[anchor.itemKey]?.let { itemIndex ->
                messageListState.requestScrollToItem(
                    index = itemIndex,
                    scrollOffset = anchor.itemScrollOffset
                )
            }
        }

        state.olderMessageRestoreAnchor = null
    }

    LaunchedEffect(
        activeChatId,
        state.pendingJumpToMessageId,
        listItemIndexes,
        viewModel.canLoadOlderMessages,
        viewModel.isLoadingOlderMessages,
        viewModel.isLoadingMessages,
        state.isMessageScrollInitialized
    ) {
        val targetMessageId = state.pendingJumpToMessageId ?: return@LaunchedEffect
        if (!state.isMessageScrollInitialized || viewModel.isLoadingMessages) return@LaunchedEffect

        val targetIndex = listItemIndexes[chatMessageItemKey(targetMessageId)]
        if (targetIndex != null) {
            state.olderMessageRestoreAnchor = null
            state.highlightedMessageId = targetMessageId
            val topHalfOffset = -(messageListState.layoutInfo.viewportSize.height / 4)
            messageListState.animateScrollToItem(targetIndex, scrollOffset = topHalfOffset)
            state.pendingJumpToMessageId = null
            return@LaunchedEffect
        }

        if (!viewModel.canLoadOlderMessages) {
            state.pendingJumpToMessageId = null
            return@LaunchedEffect
        }

        if (viewModel.isLoadingOlderMessages) return@LaunchedEffect

        state.olderMessageRestoreAnchor = null
        if (messageListState.firstVisibleItemIndex > 0) {
            messageListState.animateScrollToItem(0)
        }
        if (!viewModel.loadOlderMessages()) {
            state.pendingJumpToMessageId = null
        }
    }

    LaunchedEffect(
        sortedMessages.size,
        lastMessage?.id,
        currentUserId,
        viewModel.isLoadingMessages,
        state.hasFinishedInitialMessageLoad,
        lastListItemIndex
    ) {
        if (viewModel.isLoadingMessages) return@LaunchedEffect
        if (!state.hasFinishedInitialMessageLoad) return@LaunchedEffect
        if (lastListItemIndex < 0) {
            state.previousMessageCount = sortedMessages.size
            state.previousLastMessageId = null
            state.lastBottomVisibleMessageId = null
            state.shouldKeepChatPinnedToBottom = true
            state.isMessageScrollInitialized = true
            return@LaunchedEffect
        }

        if (!state.isMessageScrollInitialized) {
            state.previousMessageCount = sortedMessages.size
            state.previousLastMessageId = lastMessage?.id
            messageListState.scrollToMessageBottom(lastListItemIndex)
            state.lastBottomVisibleMessageId = lastMessage?.id
            state.shouldKeepChatPinnedToBottom = true
            state.isMessageScrollInitialized = true
            return@LaunchedEffect
        }

        val previousTailMessageId = state.previousLastMessageId
        val hasNewTailMessage = sortedMessages.size > state.previousMessageCount &&
            lastMessage?.id != previousTailMessageId
        val shouldKeepBottomPinned = hasNewTailMessage &&
            (state.shouldKeepChatPinnedToBottom ||
                state.previousMessageCount == 0 ||
                state.lastBottomVisibleMessageId == previousTailMessageId)
        val newMessageFromCurrentUser = hasNewTailMessage && lastMessage?.senderId == currentUserId

        state.previousMessageCount = sortedMessages.size
        state.previousLastMessageId = lastMessage?.id

        if (newMessageFromCurrentUser || shouldKeepBottomPinned) {
            messageListState.scrollToMessageBottom(lastListItemIndex)
            state.lastBottomVisibleMessageId = lastMessage?.id
            state.shouldKeepChatPinnedToBottom = true
        }
    }

    LaunchedEffect(
        isPartnerTyping,
        lastListItemIndex,
        viewModel.isLoadingMessages,
        state.hasFinishedInitialMessageLoad,
        state.isMessageScrollInitialized,
        sortedMessages.isEmpty(),
        state.shouldKeepChatPinnedToBottom,
        state.lastBottomVisibleMessageId,
        lastMessage?.id
    ) {
        if (!isPartnerTyping) return@LaunchedEffect
        if (viewModel.isLoadingMessages ||
            !state.hasFinishedInitialMessageLoad ||
            !state.isMessageScrollInitialized
        ) {
            return@LaunchedEffect
        }

        val shouldKeepBottomPinned = sortedMessages.isEmpty() ||
            state.shouldKeepChatPinnedToBottom ||
            state.lastBottomVisibleMessageId == lastMessage?.id
        if (lastListItemIndex >= 0 && shouldKeepBottomPinned) {
            messageListState.scrollToMessageBottom(lastListItemIndex)
            state.lastBottomVisibleMessageId = lastMessage?.id
            state.shouldKeepChatPinnedToBottom = true
        }
    }
}

@Composable
private fun rememberChatContentPaddingState(
    chatId: String,
    targetContentBottomPadding: Dp,
    scaffoldBottomPadding: Dp,
    isLoadingMessages: Boolean,
    lastListItemIndex: Int,
    lastMessageId: String?,
    keyboardAnchorResetToken: Int,
    state: ChatMessageListState
): ChatContentPaddingState {
    val density = LocalDensity.current
    var displayedContentBottomPadding by remember(chatId) {
        mutableStateOf(targetContentBottomPadding)
    }
    var previousTargetContentBottomPadding by remember(chatId) {
        mutableStateOf(targetContentBottomPadding)
    }
    var keyboardRestoreAnchor by remember(chatId) {
        mutableStateOf<ChatViewportAnchor?>(null)
    }

    LaunchedEffect(keyboardAnchorResetToken) {
        keyboardRestoreAnchor = null
    }

    LaunchedEffect(
        targetContentBottomPadding,
        scaffoldBottomPadding,
        isLoadingMessages,
        lastListItemIndex,
        lastMessageId,
        keyboardAnchorResetToken,
        state.shouldKeepChatPinnedToBottom
    ) {
        if (isLoadingMessages) {
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
                    keyboardRestoreAnchor = state.listState.viewportAnchor()
                }
                val shouldKeepBottomVisible = state.shouldKeepChatPinnedToBottom ||
                    keyboardRestoreAnchor?.isScrolledToBottom == true
                displayedContentBottomPadding = targetContentBottomPadding
                if (shouldKeepBottomVisible && lastListItemIndex >= 0) {
                    state.listState.scrollToMessageBottom(lastListItemIndex)
                    state.lastBottomVisibleMessageId = lastMessageId
                    state.shouldKeepChatPinnedToBottom = true
                } else {
                    state.listState.scrollBy(with(density) { bottomInsetDelta.toPx() })
                }
            }

            bottomInsetDelta < 0.dp -> {
                val restoreAnchor = keyboardRestoreAnchor
                if (state.shouldKeepChatPinnedToBottom && lastListItemIndex >= 0) {
                    state.listState.scrollToMessageBottom(lastListItemIndex)
                } else if (restoreAnchor != null) {
                    state.listState.restoreViewportAnchor(restoreAnchor)
                }
                displayedContentBottomPadding = scaffoldBottomPadding
                keyboardRestoreAnchor = null
            }

            targetContentBottomPadding == scaffoldBottomPadding -> {
                displayedContentBottomPadding = scaffoldBottomPadding
            }
        }
    }

    return ChatContentPaddingState(
        bottomPadding = displayedContentBottomPadding,
        isKeyboardAboveBottomBar = displayedContentBottomPadding > scaffoldBottomPadding
    )
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
private fun TypingIndicatorRow(
    partner: User?,
    partnerName: String,
    partnerUsername: String,
    partnerAvatarUrl: String?
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 120)),
        exit = fadeOut(animationSpec = tween(durationMillis = 160))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SolariAvatar(
                imageUrl = partner?.profileImageUrl ?: partnerAvatarUrl,
                username = partner?.username ?: partnerUsername,
                contentDescription = "${partnerName.ifBlank { "Someone" }} avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = ChatIncomingBubble,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypingDot(delayMillis = 0)
                    TypingDot(delayMillis = 120)
                    TypingDot(delayMillis = 240)
                }
            }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typingDotTransition")
    val offsetY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 900
                0f at delayMillis
                -4f at delayMillis + 150
                0f at delayMillis + 300
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "typingDotOffset"
    )

    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(ChatMuted)
    )
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
private fun ChatMessageRow(
    item: ChatMessageItem,
    partner: User?,
    currentUserId: String?,
    partnerLastReadAt: Long?,
    highlightedMessageId: String?,
    recentEmojis: List<String>,
    areMessageActionsEnabled: Boolean,
    onRecordRecentEmoji: (String) -> Unit,
    onUnsendMessage: (Message) -> Unit,
    onReactToMessage: (Message, String) -> Unit,
    onReplyToMessage: (Message) -> Unit,
    onJumpToMessage: (String) -> Unit
) {
    val message = item.message

    if (item.isFromMe) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
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

            if (item.showDeliveryFooter) {
                Text(
                    text = message.deliveryFooterText(partnerLastReadAt),
                    color = ChatMuted,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp, end = 8.dp, bottom = 8.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (item.showIncomingAvatar) {
                SolariAvatar(
                    imageUrl = partner?.profileImageUrl,
                    username = partner?.username.orEmpty(),
                    contentDescription = partner?.let { "${it.displayName} avatar" },
                    modifier = Modifier
                        .size(36.dp).clip(CircleShape),
                    fontSize = 14.sp
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .width(36.dp)
                        .height(1.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

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
    val replySwipeThresholdPx = with(LocalDensity.current) { 90.dp.toPx() }
    val bubbleShape = RoundedCornerShape(12.dp)
    val highlightScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.1f else 1f,
        animationSpec = tween(durationMillis = 220),
        label = "messageHighlightScale"
    )
    val bubbleBackgroundColor = when {
        isHighlighted -> SolariTheme.colors.primary
        isFromMe -> ChatOutgoingBubble
        else -> ChatIncomingBubble
    }
    val messageTextColor = when {
        isHighlighted -> SolariTheme.colors.onPrimary
        message.isDeleted -> ChatText.copy(alpha = 0.8f)
        else -> ChatText
    }
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
                    .scale(highlightScale)
                    .scaleOnPress(
                        interactionSource = bubbleInteractionSource,
                        pressedScale = 1.1f
                    )
                    .clip(bubbleShape)
                    .then(
                        if (message.isDeleted) {
                            if (isHighlighted) {
                                Modifier.background(bubbleBackgroundColor, bubbleShape)
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = ChatMuted.copy(alpha = 0.45f),
                                    shape = bubbleShape
                                )
                            }
                        } else {
                            Modifier.background(bubbleBackgroundColor, bubbleShape)
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
                        color = messageTextColor,
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
            .background(SolariTheme.colors.onSurface.copy(alpha = 0.12f))
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
                        .background(SolariTheme.colors.background.copy(alpha = 0.48f))
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
                .background(SolariTheme.colors.onSurfaceVariant.copy(alpha = 0.4f))
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
            .background(if (selected) SolariTheme.colors.onSurface.copy(alpha = 0.12f) else Color.Transparent)
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
    onInputFocused: () -> Unit,
    onSend: () -> Unit
) {
    val bottomPadding = if (isKeyboardAboveBottomBar) 16.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = bottomPadding)
            .clip(RoundedCornerShape(12.dp))
            .background(ChatInput)
    ) {
        AnimatedVisibility(
            visible = replyingToMessage != null && replyLabel != null,
            enter = slideInVertically(initialOffsetY = { it }) + expandVertically(),
            exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = replyLabel ?: "",
                        color = ChatPrimary,
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = replyingToMessage?.text ?: "",
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
                .heightIn(min = 56.dp)
                .padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 17.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onInputFocused()
                        }
                    },
                textStyle = TextStyle(
                    color = ChatText,
                    fontSize = 15.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(ChatText),
                singleLine = false,
                maxLines = 5,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
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
                    .padding(bottom = 8.dp)
                    .size(40.dp)
                    .scaledClickable(pressedScale = 1.1f, onClick = onSend)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChatPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = SolariTheme.colors.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun buildChatListItems(
    messages: List<Message>,
    currentUserId: String?
): List<ChatListItem> {
    if (messages.isEmpty()) return emptyList()

    val zoneId = ZoneId.systemDefault()
    val items = mutableListOf<ChatListItem>()
    var previousDate: LocalDate? = null

    messages.forEachIndexed { index, message ->
        val messageDate = message.timestamp.toLocalDate(zoneId)
        if (messageDate != previousDate) {
            items += ChatDayHeaderItem(
                date = messageDate,
                label = messageDate.toChatDayLabel()
            )
            previousDate = messageDate
        }

        val nextMessage = messages.getOrNull(index + 1)
        val nextDate = nextMessage?.timestamp?.toLocalDate(zoneId)
        val isLastInIncomingGroup = nextMessage == null ||
            nextDate != messageDate ||
            nextMessage.senderId != message.senderId

        items += ChatMessageItem(
            message = message,
            isFromMe = message.senderId == currentUserId,
            showIncomingAvatar = message.senderId != currentUserId && isLastInIncomingGroup,
            showDeliveryFooter = message.senderId == currentUserId && index == messages.lastIndex
        )
    }

    return items
}

private fun buildMessageItemIndexMap(chatItems: List<ChatListItem>): Map<String, Int> {
    val indexes = mutableMapOf<String, Int>()

    chatItems.forEachIndexed { index, item ->
        if (item is ChatMessageItem) {
            indexes[item.message.id] = index
        }
    }

    return indexes
}

private fun buildListItemIndexMap(
    chatItems: List<ChatListItem>,
    isPartnerTyping: Boolean
): Map<String, Int> {
    val indexes = mutableMapOf<String, Int>()

    chatItems.forEachIndexed { index, item ->
        indexes[item.key] = index
    }

    if (isPartnerTyping) {
        indexes[TypingIndicatorItemKey] = chatItems.size
    }

    return indexes
}

private fun chatDayItemKey(date: LocalDate): String = "day-$date"

private fun chatMessageItemKey(messageId: String): String = "message-$messageId"

private const val TypingIndicatorItemKey = "typing-indicator"

private fun LazyListState.viewportAnchor(): ChatViewportAnchor {
    return ChatViewportAnchor(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
        isScrolledToBottom = isScrolledToBottom()
    )
}

private fun LazyListState.olderMessagesRestoreAnchor(): ChatListItemAnchor? {
    val layoutInfo = layoutInfo
    val anchorItem = layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
        (itemInfo.key as? String)?.startsWith("message-") == true
    } ?: layoutInfo.visibleItemsInfo.firstOrNull()
    val itemKey = anchorItem?.key as? String ?: return null
    return ChatListItemAnchor(
        itemKey = itemKey,
        itemScrollOffset = layoutInfo.viewportStartOffset - anchorItem.offset
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

private fun Message.deliveryFooterText(partnerLastReadAt: Long?): String {
    return when (deliveryState) {
        MessageDeliveryState.Sending -> "sending"
        MessageDeliveryState.Sent -> {
            if (partnerLastReadAt != null && partnerLastReadAt >= timestamp) {
                seenFooterText(partnerLastReadAt)
            } else {
                sentFooterText(timestamp)
            }
        }
    }
}

private fun seenFooterText(readAtMillis: Long): String {
    val time = Instant.ofEpochMilli(readAtMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    val formatter = DateTimeFormatter.ofPattern("h:mma")
    return "seen at ${time.format(formatter).lowercase()}"
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

    return when (daysAgo) {
        0L -> "Today"
        in 1L..6L -> dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        else -> format(ChatDateFormatter)
    }
}

private val ChatDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
