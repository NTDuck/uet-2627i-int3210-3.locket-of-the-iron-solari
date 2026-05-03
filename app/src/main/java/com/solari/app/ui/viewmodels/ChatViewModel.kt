package com.solari.app.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.user.UserRepository
import com.solari.app.data.websocket.WebSocketEvent
import com.solari.app.data.websocket.WebSocketManager
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageDeliveryState
import com.solari.app.ui.models.MessageReaction
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

private const val LocalTypingIdleTimeoutMs = 2_000L
private const val LocalTypingHeartbeatMs = 1_500L
private const val RemoteTypingExpiryMs = 4_000L
private const val MessagePageSize = 50
private const val ChatViewModelLogTag = "ChatViewModel"
private const val MessageUnsentPreviewText = "Message unsent"
private const val UnknownMessagePreviewText = "Unknown message"

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val recentEmojiStore: RecentEmojiStore,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    private val referencedPostsById = mutableMapOf<String, Post>()
    private val replyPreviewByMessageId = mutableMapOf<String, String>()
    private val pendingReplyPreviewMessageIds = mutableSetOf<String>()

    // Monotonically increasing version per message; stale API callbacks are discarded when version has advanced
    private val reactionVersionByMessageId = mutableMapOf<String, Long>()

    var conversation by mutableStateOf<Conversation?>(null)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    var currentUserId by mutableStateOf<String?>(null)
        private set

    var messageText by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoadingMessages by mutableStateOf(false)
        private set

    var isLoadingOlderMessages by mutableStateOf(false)
        private set

    var canLoadOlderMessages by mutableStateOf(false)
        private set

    var isPartnerTyping by mutableStateOf(false)
        private set

    var recentEmojis by mutableStateOf<List<String>>(emptyList())
        private set

    private var localTypingStopJob: Job? = null
    private var localTypingHeartbeatJob: Job? = null
    private var partnerTypingExpiryJob: Job? = null
    private var localTypingConversationId: String? = null
    private var localTypingReceiverId: String? = null
    private var hasSentLocalTyping = false
    private var olderMessagesCursor: String? = null

    init {
        viewModelScope.launch {
            recentEmojiStore.recentEmojis
                .catch { errorMessage = it.message ?: "Failed to load recent emojis" }
                .collect { recentEmojis = it }
        }

        viewModelScope.launch {
            webSocketManager.events.collect { event -> handleWebSocketEvent(event) }
        }
    }

    private fun handleWebSocketEvent(event: WebSocketEvent) {
        val currentConversation = conversation ?: return
        val activeChatId = currentConversation.id

        when (event) {
            is WebSocketEvent.NewMessage -> {
                if (event.conversationId != activeChatId) return
                if (currentConversation.messages.any { it.id == event.message.id }) return

                val isPartnerMessage = event.message.senderId == currentConversation.otherUser.id
                if (isPartnerMessage) {
                    updatePartnerTypingState(false)
                }

                val messages = if (event.message.senderId == currentSenderId()) {
                    currentConversation.messages.replaceMatchingOptimisticMessage(event.message)
                        ?: (currentConversation.messages + event.message)
                } else {
                    currentConversation.messages + event.message
                }

                conversation = currentConversation.copy(
                    messages = messages,
                    lastMessage = event.message.text,
                    lastMessageSenderId = event.message.senderId,
                    isLastMessageDeleted = false,
                    timestamp = event.message.timestamp,
                    isUnread = false
                )

                if (isPartnerMessage) {
                    markActiveConversationRead(activeChatId)
                }
                resolveMissingReplyPreviews(activeChatId)
            }

            is WebSocketEvent.MessageUnsent -> {
                if (event.conversationId != activeChatId) return
                markMessageUnsentLocally(event.messageId)
            }

            is WebSocketEvent.NewReaction -> {
                if (event.conversationId != activeChatId) return
                applyReactionToMessage(event.messageId, event.reaction)
            }

            is WebSocketEvent.ReactionUpdated -> {
                if (event.conversationId != activeChatId) return
                applyReactionToMessage(event.messageId, event.reaction)
            }

            is WebSocketEvent.ReactionRemoved -> {
                if (event.conversationId != activeChatId) return
                removeMessageReactionLocally(event.messageId, event.userId)
            }

            is WebSocketEvent.ConversationRead -> {
                if (event.conversationId != activeChatId) return
                // Only update partnerLastReadAt when the reader is the partner, not the current user
                val myId = currentUser?.id ?: currentUserId
                if (event.userId == myId) return
                conversation = currentConversation.copy(
                    partnerLastReadAt = event.lastReadAtMillis
                )
            }

            is WebSocketEvent.TypingIndicator -> {
                if (event.conversationId != activeChatId) return
                if (event.senderId != currentConversation.otherUser.id) return
                updatePartnerTypingState(event.isTyping)
            }

            is WebSocketEvent.FriendshipStatusChanged -> {
                if (event.partnerId != currentConversation.otherUser.id) return
                if (!event.isFriend) {
                    stopTypingIndicator()
                    updatePartnerTypingState(false)
                    messageText = ""
                }
                conversation = currentConversation.copy(isReadOnly = !event.isFriend)
            }

            is WebSocketEvent.FriendProfileUpdated -> {
                if (event.userId == currentConversation.otherUser.id) {
                    conversation = currentConversation.copy(
                        otherUser = currentConversation.otherUser.withProfileUpdate(event)
                    )
                }
                if (event.userId == currentSenderId()) {
                    currentUser = currentUser?.withProfileUpdate(event)
                }
            }

            is WebSocketEvent.NewFriendRequest,
            is WebSocketEvent.FriendRequestAccepted,
            is WebSocketEvent.FriendRequestRemoved,
            is WebSocketEvent.PostProcessed,
            is WebSocketEvent.PostFailed -> Unit
        }
    }

    private fun applyReactionToMessage(messageId: String, reaction: MessageReaction) {
        val currentConversation = conversation ?: return
        conversation = currentConversation.copy(
            messages = currentConversation.messages.map { message ->
                if (message.id == messageId) {
                    message.withReactionFromUser(reaction)
                } else {
                    message
                }
            }
        )
    }

    private fun resolveMissingReplyPreviews(conversationId: String) {
        val currentConversation = conversation ?: return
        if (currentConversation.id != conversationId) return

        val messagesWithLoadedPreviews = currentConversation.messages.withLoadedReplyPreviews()
        if (messagesWithLoadedPreviews != currentConversation.messages) {
            conversation = currentConversation.copy(messages = messagesWithLoadedPreviews)
        }

        val latestConversation = conversation ?: return
        if (latestConversation.id != conversationId) return

        val missingRepliedMessageIds = latestConversation.messages
            .asSequence()
            .filter { it.repliedMessageId != null && it.repliedMessagePreview == null }
            .mapNotNull { it.repliedMessageId }
            .distinct()
            .toList()

        missingRepliedMessageIds.forEach { repliedMessageId ->
            val cachedPreview = replyPreviewByMessageId[repliedMessageId]
            if (cachedPreview != null) {
                applyReplyPreview(
                    conversationId = conversationId,
                    repliedMessageId = repliedMessageId,
                    preview = cachedPreview
                )
                return@forEach
            }

            if (!pendingReplyPreviewMessageIds.add(repliedMessageId)) return@forEach

            viewModelScope.launch {
                val preview = try {
                    when (val result = conversationRepository.getMessage(repliedMessageId)) {
                        is ApiResult.Success -> result.data.toReplyPreviewText()
                        is ApiResult.Failure -> {
                            if (result.type == "MESSAGE_NOT_FOUND") {
                                UnknownMessagePreviewText
                            } else {
                                Log.w(
                                    ChatViewModelLogTag,
                                    "Failed to resolve replied message $repliedMessageId: ${result.message}"
                                )
                                null
                            }
                        }
                    }
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    Log.w(
                        ChatViewModelLogTag,
                        "Failed to resolve replied message $repliedMessageId",
                        throwable
                    )
                    null
                } finally {
                    pendingReplyPreviewMessageIds.remove(repliedMessageId)
                }

                if (preview != null) {
                    val effectivePreview =
                        if (replyPreviewByMessageId[repliedMessageId] == MessageUnsentPreviewText) {
                            MessageUnsentPreviewText
                        } else {
                            preview
                        }
                    replyPreviewByMessageId[repliedMessageId] = effectivePreview
                    applyReplyPreview(
                        conversationId = conversationId,
                        repliedMessageId = repliedMessageId,
                        preview = effectivePreview
                    )
                }
            }
        }
    }

    private fun applyReplyPreview(
        conversationId: String,
        repliedMessageId: String,
        preview: String
    ) {
        val currentConversation = conversation ?: return
        if (currentConversation.id != conversationId) return

        conversation = currentConversation.copy(
            messages = currentConversation.messages.map { message ->
                if (message.repliedMessageId == repliedMessageId && message.repliedMessagePreview == null) {
                    message.copy(repliedMessagePreview = preview)
                } else {
                    message
                }
            }
        )
    }

    private fun markMessageUnsentLocally(messageId: String) {
        replyPreviewByMessageId[messageId] = MessageUnsentPreviewText
        pendingReplyPreviewMessageIds.remove(messageId)

        val currentConversation = conversation ?: return
        conversation = currentConversation.copy(
            messages = currentConversation.messages.map { message ->
                when {
                    message.id == messageId -> {
                        message.copy(
                            text = MessageUnsentPreviewText,
                            isDeleted = true,
                            reactions = emptyList()
                        )
                    }

                    message.repliedMessageId == messageId -> {
                        message.copy(repliedMessagePreview = MessageUnsentPreviewText)
                    }

                    else -> message
                }
            }
        )
    }

    private fun markActiveConversationRead(conversationId: String) {
        viewModelScope.launch {
            try {
                when (val result = conversationRepository.markConversationRead(conversationId)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Failure -> Log.w(
                        ChatViewModelLogTag,
                        "Failed to mark conversation $conversationId as read: ${result.message}"
                    )
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                Log.w(
                    ChatViewModelLogTag,
                    "Failed to mark conversation $conversationId as read",
                    throwable
                )
            }
        }
    }

    fun setInitialConversation(conversation: Conversation) {
        if (this.conversation == null) {
            this.conversation = conversation
        }
    }

    fun openDraftConversation(conversation: Conversation) {
        stopTypingIndicator()
        updatePartnerTypingState(false)
        setInitialConversation(conversation)
        isLoadingMessages = false
        isLoadingOlderMessages = false
        canLoadOlderMessages = false
        olderMessagesCursor = null
        errorMessage = null
    }

    fun loadConversation(chatId: String) {
        viewModelScope.launch {
            stopTypingIndicator()
            updatePartnerTypingState(false)
            isLoadingMessages = true
            isLoadingOlderMessages = false
            canLoadOlderMessages = false
            olderMessagesCursor = null
            errorMessage = null

            try {
                loadCurrentUser()

                val conversationResult = conversationRepository.getConversation(chatId)
                val messagesResult = conversationRepository.getMessages(
                    conversationId = chatId,
                    limit = MessagePageSize
                )
                conversationRepository.markConversationRead(chatId)

                when {
                    conversationResult is ApiResult.Success && messagesResult is ApiResult.Success -> {
                        val messagesPage = messagesResult.data
                        olderMessagesCursor = messagesPage.nextCursor
                        canLoadOlderMessages = messagesPage.nextCursor != null
                        conversation = conversationResult.data.copy(
                            partnerLastReadAt = messagesPage.partnerLastReadAt
                                ?: conversationResult.data.partnerLastReadAt,
                            messages = messagesPage.messages.withReferencedPostThumbnails()
                        )
                        resolveMissingReplyPreviews(chatId)
                        errorMessage = null
                    }

                    conversationResult is ApiResult.Failure -> errorMessage =
                        conversationResult.message

                    messagesResult is ApiResult.Failure -> errorMessage = messagesResult.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load messages"
            } finally {
                isLoadingMessages = false
            }
        }
    }

    fun loadOlderMessages(): Boolean {
        val currentConversation = conversation ?: return false
        val cursor = olderMessagesCursor ?: return false
        if (currentConversation.isDraft || isLoadingMessages || isLoadingOlderMessages) return false

        isLoadingOlderMessages = true
        viewModelScope.launch {
            try {
                when (
                    val result = conversationRepository.getMessages(
                        conversationId = currentConversation.id,
                        limit = MessagePageSize,
                        cursor = cursor
                    )
                ) {
                    is ApiResult.Success -> {
                        val latestConversation = conversation
                        if (latestConversation == null || latestConversation.id != currentConversation.id) {
                            return@launch
                        }

                        val existingMessageIds = latestConversation.messages
                            .mapTo(mutableSetOf()) { it.id }
                        val olderMessages = result.data.messages
                            .filterNot { message -> message.id in existingMessageIds }
                            .withReferencedPostThumbnails()
                        val mergedMessages = (olderMessages + latestConversation.messages)
                            .distinctBy { it.id }
                            .sortedBy { it.timestamp }

                        olderMessagesCursor = result.data.nextCursor
                        canLoadOlderMessages = result.data.nextCursor != null
                        conversation = latestConversation.copy(
                            partnerLastReadAt = result.data.partnerLastReadAt
                                ?: latestConversation.partnerLastReadAt,
                            messages = mergedMessages
                        )
                        resolveMissingReplyPreviews(currentConversation.id)
                    }

                    is ApiResult.Failure -> {
                        errorMessage = result.message
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load older messages"
            } finally {
                isLoadingOlderMessages = false
            }
        }
        return true
    }

    fun sendMessage(
        chatId: String,
        repliedMessage: Message? = null
    ) {
        if (conversation?.isReadOnly == true) return
        val content = messageText.trim()
        if (content.isBlank()) return
        stopTypingIndicator()

        val senderId = currentUser?.id ?: currentUserId
        val localMessage = senderId?.let {
            Message(
                id = "local-${UUID.randomUUID()}",
                senderId = it,
                text = content,
                timestamp = System.currentTimeMillis(),
                repliedMessageId = repliedMessage?.id,
                repliedMessagePreview = repliedMessage?.text?.takeIf { text -> text.isNotBlank() },
                deliveryState = MessageDeliveryState.Sending
            )
        }
        val previousConversation = conversation
        messageText = ""

        if (localMessage != null && previousConversation != null) {
            conversation = previousConversation.copy(
                messages = previousConversation.messages + localMessage,
                lastMessage = localMessage.text,
                lastMessageSenderId = localMessage.senderId,
                isLastMessageDeleted = false,
                timestamp = localMessage.timestamp
            )
        }

        viewModelScope.launch {
            var targetChatId = chatId
            var createdConversationId: String? = null

            try {
                if (previousConversation?.isDraft == true) {
                    when (val createResult =
                        conversationRepository.createConversation(previousConversation.otherUser.id)) {
                        is ApiResult.Success -> {
                            targetChatId = createResult.data
                            createdConversationId = createResult.data
                            conversation = conversation?.copy(
                                id = createResult.data,
                                isDraft = false
                            )
                        }

                        is ApiResult.Failure -> {
                            if (localMessage != null) {
                                conversation = previousConversation
                            }
                            messageText = content
                            errorMessage = createResult.message
                            return@launch
                        }
                    }
                }

                when (
                    val result = conversationRepository.sendMessage(
                        conversationId = targetChatId,
                        content = content,
                        repliedMessageId = repliedMessage?.id
                    )
                ) {
                    is ApiResult.Success -> {
                        messageText = ""
                        val sentMessage = result.data.copy(
                            repliedMessagePreview = repliedMessage?.text?.takeIf { it.isNotBlank() }
                        )
                        currentUserId = sentMessage.senderId
                        val currentConversation = conversation
                        if (currentConversation != null) {
                            val messages = currentConversation.messages.confirmSentMessage(
                                localMessage = localMessage,
                                sentMessage = sentMessage
                            )
                            conversation = currentConversation.copy(
                                id = targetChatId,
                                isDraft = false,
                                messages = messages,
                                lastMessage = sentMessage.text,
                                lastMessageSenderId = sentMessage.senderId,
                                isLastMessageDeleted = false,
                                timestamp = sentMessage.timestamp
                            )
                        } else {
                            loadConversation(targetChatId)
                        }
                    }

                    is ApiResult.Failure -> {
                        val wasConfirmedByWebSocket = localMessage != null &&
                                !conversation.hasMessage(localMessage.id) &&
                                conversation.hasConfirmedReplacementFor(localMessage)
                        if (!wasConfirmedByWebSocket) {
                            if (localMessage != null && previousConversation != null) {
                                conversation = if (createdConversationId == null) {
                                    previousConversation
                                } else {
                                    previousConversation.copy(
                                        id = createdConversationId,
                                        isDraft = false
                                    )
                                }
                            }
                            messageText = content
                            errorMessage = result.message
                        }
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                val wasConfirmedByWebSocket = localMessage != null &&
                        !conversation.hasMessage(localMessage.id) &&
                        conversation.hasConfirmedReplacementFor(localMessage)
                if (!wasConfirmedByWebSocket) {
                    if (localMessage != null && previousConversation != null) {
                        conversation = createdConversationId?.let { conversationId ->
                            previousConversation.copy(id = conversationId, isDraft = false)
                        } ?: previousConversation
                    }
                    messageText = content
                    errorMessage = throwable.message ?: "Failed to send message"
                }
            }
        }
    }

    fun unsendMessage(chatId: String, messageId: String) {
        if (conversation?.isReadOnly == true) return
        val previousConversation = conversation
        val previousReplyPreview = replyPreviewByMessageId[messageId]
        markMessageUnsentLocally(messageId)

        viewModelScope.launch {
            when (val result = conversationRepository.unsendMessage(chatId, messageId)) {
                is ApiResult.Success -> Unit

                is ApiResult.Failure -> {
                    conversation = previousConversation
                    if (previousReplyPreview == null) {
                        replyPreviewByMessageId.remove(messageId)
                    } else {
                        replyPreviewByMessageId[messageId] = previousReplyPreview
                    }
                    errorMessage = result.message
                }
            }
        }
    }

    fun reactToMessage(messageId: String, emoji: String) {
        if (conversation?.isReadOnly == true) return
        val userId = currentUser?.id ?: return
        val currentUserReaction = conversation
            ?.messages
            ?.firstOrNull { it.id == messageId }
            ?.reactions
            ?.firstOrNull { it.userId == userId }

        // Bump version to invalidate any in-flight callbacks for this message
        val version = reactionVersionByMessageId.merge(messageId, 1L) { old, inc -> old + inc }!!

        if (currentUserReaction?.emoji == emoji) {
            removeMessageReactionLocally(messageId, userId)
            viewModelScope.launch {
                when (val result = conversationRepository.removeMessageReaction(messageId)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Failure -> {
                        // Only revert if no newer reaction operation has started
                        if (reactionVersionByMessageId[messageId] == version) {
                            replaceMessageReaction(messageId, currentUserReaction)
                            errorMessage = result.message
                        }
                    }
                }
            }
            return
        }

        replaceMessageReaction(messageId, MessageReaction(userId = userId, emoji = emoji))

        viewModelScope.launch {
            when (val result = conversationRepository.reactToMessage(messageId, emoji)) {
                is ApiResult.Success -> {
                    // Only apply server response if no newer reaction operation has started
                    if (reactionVersionByMessageId[messageId] == version) {
                        replaceMessageReaction(messageId, result.data)
                    }
                }

                is ApiResult.Failure -> {
                    if (reactionVersionByMessageId[messageId] == version) {
                        if (currentUserReaction != null) {
                            replaceMessageReaction(messageId, currentUserReaction)
                        } else {
                            removeMessageReactionLocally(messageId, userId)
                        }
                        errorMessage = result.message
                    }
                }
            }
        }
    }

    fun recordRecentEmoji(emoji: String) {
        recentEmojis = recentEmojiStore.mergeEmoji(recentEmojis, emoji)
        viewModelScope.launch {
            runCatching {
                recentEmojiStore.recordEmoji(emoji)
            }.onFailure { throwable ->
                errorMessage = throwable.message ?: "Failed to save recent emoji"
            }
        }
    }

    fun onMessageTextChanged(
        chatId: String,
        receiverId: String?,
        value: String,
        canSendTypingState: Boolean
    ) {
        messageText = value

        if (!canSendTypingState || receiverId == null) {
            stopTypingIndicator()
            return
        }

        val isTyping = value.isNotBlank()
        if (!isTyping) {
            stopTypingIndicator()
            return
        }

        if (!hasSentLocalTyping ||
            localTypingConversationId != chatId ||
            localTypingReceiverId != receiverId
        ) {
            sendLocalTypingState(
                conversationId = chatId,
                receiverId = receiverId,
                isTyping = true
            )
            startTypingHeartbeat(
                conversationId = chatId,
                receiverId = receiverId
            )
        }

        localTypingStopJob?.cancel()
        localTypingStopJob = viewModelScope.launch {
            delay(LocalTypingIdleTimeoutMs)
            stopTypingIndicator()
        }
    }

    fun stopTypingIndicator() {
        localTypingStopJob?.cancel()
        localTypingStopJob = null
        localTypingHeartbeatJob?.cancel()
        localTypingHeartbeatJob = null

        val conversationId = localTypingConversationId
        val receiverId = localTypingReceiverId
        if (hasSentLocalTyping && conversationId != null && receiverId != null) {
            webSocketManager.sendTypingState(
                conversationId = conversationId,
                receiverId = receiverId,
                isTyping = false
            )
        }

        localTypingConversationId = null
        localTypingReceiverId = null
        hasSentLocalTyping = false
    }

    private fun startTypingHeartbeat(
        conversationId: String,
        receiverId: String
    ) {
        localTypingHeartbeatJob?.cancel()
        localTypingHeartbeatJob = viewModelScope.launch {
            while (true) {
                delay(LocalTypingHeartbeatMs)
                if (!hasSentLocalTyping ||
                    localTypingConversationId != conversationId ||
                    localTypingReceiverId != receiverId ||
                    messageText.isBlank()
                ) {
                    return@launch
                }
                webSocketManager.sendTypingState(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    isTyping = true
                )
            }
        }
    }

    private fun sendLocalTypingState(
        conversationId: String,
        receiverId: String,
        isTyping: Boolean
    ) {
        val wasQueued = webSocketManager.sendTypingState(
            conversationId = conversationId,
            receiverId = receiverId,
            isTyping = isTyping
        )
        if (wasQueued && isTyping) {
            localTypingConversationId = conversationId
            localTypingReceiverId = receiverId
            hasSentLocalTyping = true
        }
    }

    private fun updatePartnerTypingState(isTyping: Boolean) {
        partnerTypingExpiryJob?.cancel()
        partnerTypingExpiryJob = null
        isPartnerTyping = isTyping

        if (isTyping) {
            partnerTypingExpiryJob = viewModelScope.launch {
                delay(RemoteTypingExpiryMs)
                isPartnerTyping = false
            }
        }
    }

    private suspend fun loadCurrentUser() {
        when (val userResult = userRepository.getMe()) {
            is ApiResult.Success -> {
                currentUser = userResult.data
                currentUserId = userResult.data.id
            }

            is ApiResult.Failure -> errorMessage = userResult.message
        }
    }

    private fun removeMessageReactionLocally(messageId: String, userId: String) {
        val currentConversation = conversation ?: return
        conversation = currentConversation.copy(
            messages = currentConversation.messages.map { message ->
                if (message.id == messageId) {
                    message.copy(reactions = message.reactions.filterNot { it.userId == userId })
                } else {
                    message
                }
            }
        )
    }

    private fun replaceMessageReaction(messageId: String, reaction: MessageReaction) {
        val currentConversation = conversation ?: return
        conversation = currentConversation.copy(
            messages = currentConversation.messages.map { message ->
                if (message.id == messageId) {
                    message.withReactionFromUser(reaction)
                } else {
                    message
                }
            }
        )
    }

    private fun currentSenderId(): String? {
        return currentUser?.id ?: currentUserId
    }

    private fun User.withProfileUpdate(event: WebSocketEvent.FriendProfileUpdated): User {
        val updatedUsername = event.username ?: username
        val profileDisplayName = event.displayName ?: updatedUsername
        return copy(
            username = updatedUsername,
            displayName = nickname?.takeIf { it.isNotBlank() } ?: profileDisplayName,
            profileImageUrl = event.avatarUrl
        )
    }

    private fun Conversation?.hasMessage(messageId: String): Boolean {
        return this?.messages?.any { it.id == messageId } == true
    }

    private fun Conversation?.hasConfirmedReplacementFor(localMessage: Message): Boolean {
        return this?.messages?.any { message ->
            message.id != localMessage.id &&
                    message.deliveryState == MessageDeliveryState.Sent &&
                    message.matchesOptimisticMessage(localMessage)
        } == true
    }

    private fun List<Message>.confirmSentMessage(
        localMessage: Message?,
        sentMessage: Message
    ): List<Message> {
        if (any { it.id == sentMessage.id }) {
            return map { message ->
                if (message.id == sentMessage.id) {
                    sentMessage.withUiOnlyFieldsFrom(message)
                } else {
                    message
                }
            }
        }

        if (localMessage != null) {
            val replacedMessages = replaceMessageById(
                messageId = localMessage.id,
                replacement = sentMessage.withUiOnlyFieldsFrom(localMessage)
            )
            if (replacedMessages != null) return replacedMessages
        }

        return this + sentMessage
    }

    private fun List<Message>.replaceMatchingOptimisticMessage(
        serverMessage: Message
    ): List<Message>? {
        val localMessageIndex = indexOfFirst { message ->
            message.deliveryState == MessageDeliveryState.Sending &&
                    serverMessage.matchesOptimisticMessage(message)
        }
        if (localMessageIndex == -1) return null

        val localMessage = this[localMessageIndex]
        return mapIndexed { index, message ->
            if (index == localMessageIndex) {
                serverMessage.withUiOnlyFieldsFrom(localMessage)
            } else {
                message
            }
        }
    }

    private fun List<Message>.replaceMessageById(
        messageId: String,
        replacement: Message
    ): List<Message>? {
        var didReplace = false
        val messages = map { message ->
            if (message.id == messageId) {
                didReplace = true
                replacement
            } else {
                message
            }
        }
        return if (didReplace) messages else null
    }

    private fun Message.withUiOnlyFieldsFrom(localMessage: Message): Message {
        return copy(
            repliedMessagePreview = repliedMessagePreview ?: localMessage.repliedMessagePreview,
            referencedPostThumbnailUrl = referencedPostThumbnailUrl
                ?: localMessage.referencedPostThumbnailUrl
        )
    }

    private fun Message.matchesOptimisticMessage(localMessage: Message): Boolean {
        return senderId == localMessage.senderId &&
                text == localMessage.text &&
                repliedMessageId == localMessage.repliedMessageId &&
                referencedPostId == localMessage.referencedPostId
    }

    private fun Message.withReactionFromUser(reaction: MessageReaction): Message {
        return copy(
            reactions = reactions
                .filterNot { it.userId == reaction.userId }
                .plus(reaction)
        )
    }

    private fun List<Message>.withLoadedReplyPreviews(): List<Message> {
        val messagesById = associateBy { it.id }
        messagesById.values.forEach { message ->
            replyPreviewByMessageId[message.id] = message.toReplyPreviewText()
        }

        return map { message ->
            val repliedMessageId = message.repliedMessageId ?: return@map message
            if (message.repliedMessagePreview != null) return@map message

            val preview = messagesById[repliedMessageId]?.toReplyPreviewText()
                ?: replyPreviewByMessageId[repliedMessageId]
                ?: return@map message
            message.copy(repliedMessagePreview = preview)
        }
    }

    private fun Message.toReplyPreviewText(): String {
        return if (isDeleted || text.isBlank()) {
            MessageUnsentPreviewText
        } else {
            text
        }
    }

    private suspend fun List<Message>.withReferencedPostThumbnails(): List<Message> {
        val missingPostIds = mapNotNull { it.referencedPostId }
            .filterNot(referencedPostsById::containsKey)
            .toSet()

        missingPostIds.forEach { postId ->
            when (val result = feedRepository.getPost(postId)) {
                is ApiResult.Success -> referencedPostsById[postId] = result.data
                is ApiResult.Failure -> Unit
            }
        }

        return map { message ->
            val post = message.referencedPostId?.let(referencedPostsById::get)
            if (post == null) {
                message
            } else {
                message.copy(
                    referencedPostThumbnailUrl = post.thumbnailUrl.ifBlank { post.imageUrl }
                )
            }
        }
    }

    override fun onCleared() {
        stopTypingIndicator()
        partnerTypingExpiryJob?.cancel()
        partnerTypingExpiryJob = null
        super.onCleared()
    }
}
