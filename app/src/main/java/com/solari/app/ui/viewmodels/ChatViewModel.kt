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
    var conversation by mutableStateOf<Conversation?>(null)
        private set

    var currentUser by mutableStateOf<User?>(null)
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

    fun loadConversation(chatId: String) {
        viewModelScope.launch {
            isLoadingMessages = true
            errorMessage = null

            try {
                when (val userResult = userRepository.getMe()) {
                    is ApiResult.Success -> currentUser = userResult.data
                    is ApiResult.Failure -> errorMessage = userResult.message
                }

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
        val content = messageText.trim()
        if (content.isBlank()) return

        val sender = currentUser
        val localMessage = sender?.let {
            Message(
                id = "local-${UUID.randomUUID()}",
                senderId = it.id,
                text = content,
                timestamp = System.currentTimeMillis(),
                repliedMessageId = repliedMessage?.id,
                repliedMessagePreview = repliedMessage?.text?.takeIf { text -> text.isNotBlank() },
                deliveryState = MessageDeliveryState.Sending
            )
        }
        val previousConversation = conversation

        if (localMessage != null && previousConversation != null) {
            messageText = ""
            conversation = previousConversation.copy(
                messages = previousConversation.messages + localMessage,
                lastMessage = localMessage.text,
                timestamp = localMessage.timestamp
            )
        }

        viewModelScope.launch {
            when (
                val result = conversationRepository.sendMessage(
                    conversationId = chatId,
                    content = content,
                    repliedMessageId = repliedMessage?.id
                )
            ) {
                is ApiResult.Success -> {
                    messageText = ""
                    val sentMessage = result.data.copy(
                        repliedMessagePreview = repliedMessage?.text?.takeIf { it.isNotBlank() }
                    )
                    val currentConversation = conversation
                    if (currentConversation != null) {
                        conversation = currentConversation.copy(
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
                        loadConversation(chatId)
                    }
                }

                is ApiResult.Failure -> {
                    if (localMessage != null && previousConversation != null) {
                        conversation = previousConversation
                    }
                    messageText = content
                    errorMessage = result.message
                }
            }
        }
    }
    
    fun unsendMessage(chatId: String, messageId: String) {
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
        val userId = currentUser?.id ?: return
        val previousConversation = conversation
        val currentUserReaction = conversation
            ?.messages
            ?.firstOrNull { it.id == messageId }
            ?.reactions
            ?.firstOrNull { it.userId == userId }

        if (currentUserReaction?.emoji == emoji) {
            removeMessageReactionLocally(messageId, userId)
            viewModelScope.launch {
                when (val result = conversationRepository.removeMessageReaction(messageId)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Failure -> {
                        conversation = previousConversation
                        errorMessage = result.message
                    }
                }
            }
            return
        }

        replaceMessageReaction(messageId, MessageReaction(userId = userId, emoji = emoji))

        viewModelScope.launch {
            when (val result = conversationRepository.reactToMessage(messageId, emoji)) {
                is ApiResult.Success -> replaceMessageReaction(messageId, result.data)
                is ApiResult.Failure -> {
                    conversation = previousConversation
                    errorMessage = result.message
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
        val referencedPostIds = mapNotNull { it.referencedPostId }.toSet()
        if (referencedPostIds.isEmpty()) return this

        return when (val feedResult = feedRepository.getFeed()) {
            is ApiResult.Failure -> this
            is ApiResult.Success -> {
                val postsById = feedResult.data.associateBy { it.id }
                map { message ->
                    val post = message.referencedPostId?.let(postsById::get)
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
    }
}
