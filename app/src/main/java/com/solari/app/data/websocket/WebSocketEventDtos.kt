package com.solari.app.data.websocket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Top-level envelope for all server-pushed WebSocket events.
 * The [payload] is kept as raw [JsonElement] so we can deserialize it
 * into the correct event-specific type after inspecting [type].
 */
@Serializable
data class WebSocketEnvelopeDto(
    val type: String,
    val payload: JsonElement
)

// -- Conversation-related event payloads --

@Serializable
data class NewMessagePayloadDto(
    @SerialName("conversationId") val conversationId: String,
    val message: WsMessageDto
)

@Serializable
data class WsMessageDto(
    val id: String,
    @SerialName("conversationId") val conversationId: String,
    @SerialName("senderId") val senderId: String,
    val content: String? = null,
    @SerialName("referencedPostId") val referencedPostId: String? = null,
    @SerialName("repliedMessageId") val repliedMessageId: String? = null,
    @SerialName("createdAt") val createdAt: String
)

@Serializable
data class MessageUnsentPayloadDto(
    @SerialName("conversationId") val conversationId: String,
    @SerialName("messageId") val messageId: String,
    @SerialName("isDeleted") val isDeleted: Boolean
)

@Serializable
data class NewReactionPayloadDto(
    @SerialName("conversationId") val conversationId: String,
    val reaction: WsReactionDto
)

@Serializable
data class WsReactionDto(
    val id: String,
    @SerialName("messageId") val messageId: String,
    @SerialName("userId") val userId: String,
    val emoji: String,
    @SerialName("createdAt") val createdAt: String
)

@Serializable
data class ReactionUpdatedPayloadDto(
    @SerialName("conversationId") val conversationId: String,
    val reaction: WsReactionDto
)

@Serializable
data class ReactionRemovedPayloadDto(
    @SerialName("conversationId") val conversationId: String,
    @SerialName("messageId") val messageId: String,
    @SerialName("userId") val userId: String
)

@Serializable
data class ConversationReadPayloadDto(
    @SerialName("conversationId") val conversationId: String,
    @SerialName("userId") val userId: String,
    @SerialName("lastReadAt") val lastReadAt: String
)

@Serializable
data class TypingIndicatorPayloadDto(
    @SerialName("conversationId") val conversationId: String,
    @SerialName("senderId") val senderId: String,
    @SerialName("isTyping") val isTyping: Boolean
)

@Serializable
data class PostProcessedPayloadDto(
    @SerialName("postId") val postId: String,
    val status: String
)

@Serializable
data class PostFailedPayloadDto(
    @SerialName("postId") val postId: String,
    val error: String
)

// -- Client-to-server action envelope --

@Serializable
data class WebSocketActionDto(
    val action: String,
    val payload: JsonElement
)

@Serializable
data class SendTypingStatePayloadDto(
    @SerialName("conversationId") val conversationId: String,
    @SerialName("receiverId") val receiverId: String,
    @SerialName("isTyping") val isTyping: Boolean
)
