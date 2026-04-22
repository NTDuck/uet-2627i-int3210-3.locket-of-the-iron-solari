package com.solari.app.data.conversation

import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.conversation.ConversationApi
import com.solari.app.data.remote.conversation.ConversationDto
import com.solari.app.data.remote.conversation.CreateConversationRequestDto
import com.solari.app.data.remote.conversation.MessageDto
import com.solari.app.data.remote.conversation.MessageReactionDto
import com.solari.app.data.remote.conversation.MessageReactionRecordDto
import com.solari.app.data.remote.conversation.MessageReactionRequestDto
import com.solari.app.data.remote.conversation.SendMessageRequestDto
import com.solari.app.data.remote.conversation.SentMessageDto
import com.solari.app.data.remote.conversation.ToggleConversationMuteRequestDto
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageReaction

class DefaultConversationRepository(
    private val conversationApi: ConversationApi,
    private val apiExecutor: ApiExecutor
) : ConversationRepository {
    override suspend fun createConversation(targetUserId: String): ApiResult<String> {
        return when (
            val result = apiExecutor.execute {
                conversationApi.createConversation(
                    CreateConversationRequestDto(targetUserId = targetUserId)
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.conversation.id)
        }
    }

    override suspend fun getConversations(): ApiResult<List<Conversation>> {
        return when (val result = apiExecutor.execute { conversationApi.getConversations() }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiConversation() })
        }
    }

    override suspend fun getConversation(conversationId: String): ApiResult<Conversation> {
        return when (val result = apiExecutor.execute { conversationApi.getConversation(conversationId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.conversation.toUiConversation())
        }
    }

    override suspend fun getMessages(conversationId: String): ApiResult<List<Message>> {
        return when (val result = apiExecutor.execute { conversationApi.getMessages(conversationId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                val messages = result.data.items
                    .map { it.toUiMessage() }
                    .sortedBy { it.timestamp }
                    .withReplyPreviews()
                ApiResult.Success(messages)
            }
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        content: String,
        referencedPostId: String?,
        repliedMessageId: String?
    ): ApiResult<Message> {
        return when (
            val result = apiExecutor.execute {
                conversationApi.sendMessage(
                    conversationId = conversationId,
                    request = SendMessageRequestDto(
                        content = content,
                        referencedPostId = referencedPostId,
                        repliedMessageId = repliedMessageId
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.data.toUiMessage())
        }
    }

    override suspend fun unsendMessage(
        conversationId: String,
        messageId: String
    ): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { conversationApi.unsendMessage(conversationId, messageId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun toggleConversationMute(
        conversationId: String
    ): ApiResult<Boolean> {
        return when (
            val result = apiExecutor.execute {
                conversationApi.toggleConversationMute(
                    ToggleConversationMuteRequestDto(conversationId = conversationId)
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.isMuted)
        }
    }

    override suspend fun clearConversationHistory(conversationId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { conversationApi.clearConversationHistory(conversationId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun reactToMessage(
        messageId: String,
        emoji: String
    ): ApiResult<MessageReaction> {
        return when (
            val result = apiExecutor.execute {
                conversationApi.reactToMessage(
                    messageId = messageId,
                    request = MessageReactionRequestDto(emoji = emoji)
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.data.toUiReaction())
        }
    }

    override suspend fun markConversationRead(conversationId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { conversationApi.markConversationRead(conversationId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun removeMessageReaction(messageId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { conversationApi.removeMessageReaction(messageId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    private fun ConversationDto.toUiConversation(): Conversation {
        return Conversation(
            id = id,
            otherUser = partner.toUiUser(),
            lastMessage = lastMessage?.content?.takeUnless { lastMessage.isDeleted }.orEmpty(),
            lastMessageSenderId = lastMessage?.senderId,
            timestamp = (updatedAt ?: lastMessage?.createdAt ?: createdAt).toEpochMillisOrNow(),
            isUnread = lastMessage?.senderId != null &&
                    currentUserLastReadAt != null &&
                    lastMessage.createdAt > currentUserLastReadAt,
            isMuted = isMuted,
            isReadOnly = isReadOnly || legacyIsReadOnly,
            messages = emptyList()
        )
    }

    private fun MessageDto.toUiMessage(): Message {
        return Message(
            id = id,
            senderId = senderId,
            text = if (isDeleted) "Message unsent" else content.orEmpty(),
            timestamp = createdAt.toEpochMillisOrNow(),
            isDeleted = isDeleted,
            referencedPostId = referencedPostId,
            repliedMessageId = repliedMessageId,
            reactions = reactions.map { it.toUiReaction() }
        )
    }

    private fun SentMessageDto.toUiMessage(): Message {
        return Message(
            id = id,
            senderId = senderId,
            text = content,
            timestamp = createdAt.toEpochMillisOrNow(),
            referencedPostId = referencedPostId,
            repliedMessageId = repliedMessageId
        )
    }

    private fun MessageReactionDto.toUiReaction(): MessageReaction {
        return MessageReaction(
            userId = userId,
            emoji = emoji
        )
    }

    private fun MessageReactionRecordDto.toUiReaction(): MessageReaction {
        return MessageReaction(
            userId = userId,
            emoji = emoji
        )
    }

    private fun List<Message>.withReplyPreviews(): List<Message> {
        val messagesById = associateBy { it.id }
        return map { message ->
            val repliedMessage = message.repliedMessageId?.let(messagesById::get)
            if (repliedMessage == null) {
                message
            } else {
                message.copy(
                    repliedMessagePreview = repliedMessage.text.takeIf { it.isNotBlank() }
                        ?: "Message unsent"
                )
            }
        }
    }
}
