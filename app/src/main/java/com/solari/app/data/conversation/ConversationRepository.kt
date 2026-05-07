package com.solari.app.data.conversation

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageReaction
import kotlinx.coroutines.flow.StateFlow

data class ConversationMessagesPage(
    val messages: List<Message>,
    val nextCursor: String?,
    val partnerLastReadAt: Long?
)

interface ConversationRepository {
    val clearedConversationIds: StateFlow<Set<String>>

    suspend fun createConversation(targetUserId: String): ApiResult<String>

    suspend fun getConversations(): ApiResult<List<Conversation>>

    suspend fun getConversation(conversationId: String): ApiResult<Conversation>

    suspend fun getMessages(
        conversationId: String,
        limit: Int = 50,
        cursor: String? = null
    ): ApiResult<ConversationMessagesPage>

    suspend fun getMessage(messageId: String): ApiResult<Message>

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        referencedPostId: String? = null,
        repliedMessageId: String? = null
    ): ApiResult<Message>

    suspend fun unsendMessage(
        conversationId: String,
        messageId: String
    ): ApiResult<Unit>

    suspend fun toggleConversationMute(
        conversationId: String
    ): ApiResult<Boolean>

    suspend fun clearConversationHistory(conversationId: String): ApiResult<Unit>

    suspend fun reactToMessage(
        messageId: String,
        emoji: String
    ): ApiResult<MessageReaction>

    suspend fun removeMessageReaction(messageId: String): ApiResult<Unit>

    suspend fun markConversationRead(conversationId: String): ApiResult<Unit>
}
