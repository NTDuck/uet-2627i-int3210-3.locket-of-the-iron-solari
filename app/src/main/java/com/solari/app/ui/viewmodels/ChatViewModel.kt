package com.solari.app.ui.viewmodels

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
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageDeliveryState
import com.solari.app.ui.models.MessageReaction
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val recentEmojiStore: RecentEmojiStore
) : ViewModel() {
    private val referencedPostsById = mutableMapOf<String, Post>()
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

    var recentEmojis by mutableStateOf<List<String>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            recentEmojiStore.recentEmojis
                .catch { errorMessage = it.message ?: "Failed to load recent emojis" }
                .collect { recentEmojis = it }
        }
    }

    fun setInitialConversation(conversation: Conversation) {
        if (this.conversation == null) {
            this.conversation = conversation
        }
    }

    fun openDraftConversation(conversation: Conversation) {
        setInitialConversation(conversation)
        isLoadingMessages = false
        errorMessage = null
    }

    fun loadConversation(chatId: String) {
        viewModelScope.launch {
            isLoadingMessages = true
            errorMessage = null

            try {
                loadCurrentUser()

                val conversationResult = conversationRepository.getConversation(chatId)
                val messagesResult = conversationRepository.getMessages(chatId)
                conversationRepository.markConversationRead(chatId)

                when {
                    conversationResult is ApiResult.Success && messagesResult is ApiResult.Success -> {
                        conversation = conversationResult.data.copy(
                            messages = messagesResult.data.withReferencedPostThumbnails()
                        )
                        errorMessage = null
                    }

                    conversationResult is ApiResult.Failure -> errorMessage = conversationResult.message
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

    fun sendMessage(
        chatId: String,
        repliedMessage: Message? = null
    ) {
        if (conversation?.isReadOnly == true) return
        val content = messageText.trim()
        if (content.isBlank()) return

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
                timestamp = localMessage.timestamp
            )
        }

        viewModelScope.launch {
            var targetChatId = chatId
            var createdConversationId: String? = null

            try {
                if (previousConversation?.isDraft == true) {
                    when (val createResult = conversationRepository.createConversation(previousConversation.otherUser.id)) {
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
                            conversation = currentConversation.copy(
                                id = targetChatId,
                                isDraft = false,
                                messages = if (localMessage != null) {
                                    currentConversation.messages.map { message ->
                                        if (message.id == localMessage.id) sentMessage else message
                                    }
                                } else {
                                    currentConversation.messages + sentMessage
                                },
                                lastMessage = sentMessage.text,
                                timestamp = sentMessage.timestamp
                            )
                        } else {
                            loadConversation(targetChatId)
                        }
                    }

                    is ApiResult.Failure -> {
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
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
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
    
    fun unsendMessage(chatId: String, messageId: String) {
        if (conversation?.isReadOnly == true) return
        val previousConversation = conversation
        conversation = previousConversation?.copy(
            messages = previousConversation.messages.map { message ->
                if (message.id == messageId) {
                    message.copy(text = "Message unsent", isDeleted = true, reactions = emptyList())
                } else {
                    message
                }
            }
        )

        viewModelScope.launch {
            when (val result = conversationRepository.unsendMessage(chatId, messageId)) {
                is ApiResult.Success -> Unit

                is ApiResult.Failure -> {
                    conversation = previousConversation
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

    private fun Message.withReactionFromUser(reaction: MessageReaction): Message {
        return copy(
            reactions = reactions
                .filterNot { it.userId == reaction.userId }
                .plus(reaction)
        )
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
}
