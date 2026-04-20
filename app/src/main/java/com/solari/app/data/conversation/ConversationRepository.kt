package com.solari.app.data.conversation

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message

interface ConversationRepository {
    suspend fun getConversations(): ApiResult<List<Conversation>>

    suspend fun getConversation(conversationId: String): ApiResult<Conversation>

    suspend fun getMessages(conversationId: String): ApiResult<List<Message>>

    suspend fun sendMessage(
        conversationId: String,
        content: String
    ): ApiResult<Message>

    suspend fun unsendMessage(
        conversationId: String,
        messageId: String
    ): ApiResult<Unit>
}
