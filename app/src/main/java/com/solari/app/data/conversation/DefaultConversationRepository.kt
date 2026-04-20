package com.solari.app.data.conversation

import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.conversation.ConversationApi
import com.solari.app.data.remote.conversation.ConversationDto
import com.solari.app.data.remote.conversation.MessageDto
import com.solari.app.data.remote.conversation.SendMessageRequestDto
import com.solari.app.data.remote.conversation.SentMessageDto
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message

class DefaultConversationRepository(
    private val conversationApi: ConversationApi,
    private val apiExecutor: ApiExecutor
) : ConversationRepository {
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
            is ApiResult.Success -> ApiResult.Success(
                result.data.items
                    .map { it.toUiMessage() }
                    .sortedBy { it.timestamp }
            )
        }
    }

    override suspend fun sendMessage(
        conversationId: String,
        content: String
    ): ApiResult<Message> {
        return when (
            val result = apiExecutor.execute {
                conversationApi.sendMessage(
                    conversationId = conversationId,
                    request = SendMessageRequestDto(content = content)
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

    private fun ConversationDto.toUiConversation(): Conversation {
        return Conversation(
            id = id,
            otherUser = partner.toUiUser(),
            lastMessage = lastMessage?.content?.takeUnless { lastMessage.isDeleted }.orEmpty(),
            timestamp = (updatedAt ?: lastMessage?.createdAt ?: createdAt).toEpochMillisOrNow(),
            isUnread = lastMessage?.senderId != null &&
                    currentUserLastReadAt != null &&
                    lastMessage.createdAt > currentUserLastReadAt,
            messages = emptyList()
        )
    }

    private fun MessageDto.toUiMessage(): Message {
        return Message(
            id = id,
            senderId = senderId,
            text = if (isDeleted) "Message unsent" else content.orEmpty(),
            timestamp = createdAt.toEpochMillisOrNow(),
            reactions = reactions.map { it.emoji }
        )
    }

    private fun SentMessageDto.toUiMessage(): Message {
        return Message(
            id = id,
            senderId = senderId,
            text = content,
            timestamp = createdAt.toEpochMillisOrNow()
        )
    }
}
