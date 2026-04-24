package com.solari.app.data.websocket

import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageReaction

/**
 * Strongly-typed domain events emitted by [WebSocketManager].
 * ViewModels collect from a SharedFlow of these events and apply UI updates.
 */
sealed interface WebSocketEvent {

    data class NewMessage(
        val conversationId: String,
        val message: Message
    ) : WebSocketEvent

    data class MessageUnsent(
        val conversationId: String,
        val messageId: String
    ) : WebSocketEvent

    data class NewReaction(
        val conversationId: String,
        val messageId: String,
        val reaction: MessageReaction
    ) : WebSocketEvent

    data class ReactionUpdated(
        val conversationId: String,
        val messageId: String,
        val reaction: MessageReaction
    ) : WebSocketEvent

    data class ReactionRemoved(
        val conversationId: String,
        val messageId: String,
        val userId: String
    ) : WebSocketEvent

    data class ConversationRead(
        val conversationId: String,
        val userId: String,
        val lastReadAtMillis: Long
    ) : WebSocketEvent

    data class TypingIndicator(
        val conversationId: String,
        val senderId: String,
        val isTyping: Boolean
    ) : WebSocketEvent

    data class PostProcessed(
        val postId: String,
        val status: String
    ) : WebSocketEvent

    data class PostFailed(
        val postId: String,
        val error: String
    ) : WebSocketEvent
}
