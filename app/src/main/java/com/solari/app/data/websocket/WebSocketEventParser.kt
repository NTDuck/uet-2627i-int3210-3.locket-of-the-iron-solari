package com.solari.app.data.websocket

import android.util.Log
import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageReaction
import kotlinx.serialization.json.Json

/**
 * Parses a raw WebSocket text frame into a domain [WebSocketEvent].
 * Returns null for unknown or unparseable event types rather than crashing,
 * since the server may introduce new event types the client doesn't yet handle.
 */
class WebSocketEventParser(
    private val json: Json
) {
    fun parse(rawText: String): WebSocketEvent? {
        val envelope = try {
            json.decodeFromString<WebSocketEnvelopeDto>(rawText)
        } catch (e: Exception) {
            Log.w(Tag, "Failed to parse WebSocket envelope: ${e.message}")
            return null
        }

        return try {
            when (envelope.type) {
                "NEW_MESSAGE" -> parseNewMessage(envelope)
                "MESSAGE_UNSENT" -> parseMessageUnsent(envelope)
                "NEW_REACTION" -> parseNewReaction(envelope)
                "REACTION_UPDATED" -> parseReactionUpdated(envelope)
                "REACTION_REMOVED" -> parseReactionRemoved(envelope)
                "TYPING_INDICATOR" -> parseTypingIndicator(envelope)
                "CONVERSATION_READ" -> parseConversationRead(envelope)
                "POST_PROCESSED" -> parsePostProcessed(envelope)
                "POST_FAILED" -> parsePostFailed(envelope)
                "NEW_FRIEND_REQUEST" -> parseNewFriendRequest(envelope)
                "FRIEND_REQUEST_ACCEPTED" -> parseFriendRequestAccepted(envelope)
                "FRIEND_REQUEST_REMOVED" -> parseFriendRequestRemoved(envelope)
                "FRIENDSHIP_STATUS_CHANGED" -> parseFriendshipStatusChanged(envelope)
                "FRIEND_PROFILE_UPDATED" -> parseFriendProfileUpdated(envelope)
                else -> {
                    Log.d(Tag, "Unhandled WebSocket event type: ${envelope.type}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(Tag, "Failed to parse payload for ${envelope.type}: ${e.message}")
            null
        }
    }

    private fun parseNewMessage(envelope: WebSocketEnvelopeDto): WebSocketEvent.NewMessage {
        val payload = json.decodeFromString<NewMessagePayloadDto>(envelope.payload.toString())
        val msg = payload.message
        return WebSocketEvent.NewMessage(
            conversationId = payload.conversationId,
            message = Message(
                id = msg.id,
                senderId = msg.senderId,
                text = msg.content.orEmpty(),
                timestamp = msg.createdAt.toEpochMillisOrNow(),
                referencedPostId = msg.referencedPostId,
                repliedMessageId = msg.repliedMessageId
            )
        )
    }

    private fun parseMessageUnsent(envelope: WebSocketEnvelopeDto): WebSocketEvent.MessageUnsent {
        val payload = json.decodeFromString<MessageUnsentPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.MessageUnsent(
            conversationId = payload.conversationId,
            messageId = payload.messageId
        )
    }

    private fun parseNewReaction(envelope: WebSocketEnvelopeDto): WebSocketEvent.NewReaction {
        val payload = json.decodeFromString<NewReactionPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.NewReaction(
            conversationId = payload.conversationId,
            messageId = payload.reaction.messageId,
            reaction = MessageReaction(
                userId = payload.reaction.userId,
                emoji = payload.reaction.emoji
            )
        )
    }

    private fun parseReactionUpdated(envelope: WebSocketEnvelopeDto): WebSocketEvent.ReactionUpdated {
        val payload = json.decodeFromString<ReactionUpdatedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.ReactionUpdated(
            conversationId = payload.conversationId,
            messageId = payload.reaction.messageId,
            reaction = MessageReaction(
                userId = payload.reaction.userId,
                emoji = payload.reaction.emoji
            )
        )
    }

    private fun parseReactionRemoved(envelope: WebSocketEnvelopeDto): WebSocketEvent.ReactionRemoved {
        val payload = json.decodeFromString<ReactionRemovedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.ReactionRemoved(
            conversationId = payload.conversationId,
            messageId = payload.messageId,
            userId = payload.userId
        )
    }

    private fun parseConversationRead(envelope: WebSocketEnvelopeDto): WebSocketEvent.ConversationRead {
        val payload = json.decodeFromString<ConversationReadPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.ConversationRead(
            conversationId = payload.conversationId,
            userId = payload.userId,
            lastReadAtMillis = payload.lastReadAt.toEpochMillisOrNow()
        )
    }

    private fun parseTypingIndicator(envelope: WebSocketEnvelopeDto): WebSocketEvent.TypingIndicator {
        val payload = json.decodeFromString<TypingIndicatorPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.TypingIndicator(
            conversationId = payload.conversationId,
            senderId = payload.senderId,
            isTyping = payload.isTyping
        )
    }

    private fun parsePostProcessed(envelope: WebSocketEnvelopeDto): WebSocketEvent.PostProcessed {
        val payload = json.decodeFromString<PostProcessedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.PostProcessed(
            postId = payload.postId,
            status = payload.status
        )
    }

    private fun parsePostFailed(envelope: WebSocketEnvelopeDto): WebSocketEvent.PostFailed {
        val payload = json.decodeFromString<PostFailedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.PostFailed(
            postId = payload.postId,
            error = payload.error
        )
    }

    private fun parseNewFriendRequest(envelope: WebSocketEnvelopeDto): WebSocketEvent.NewFriendRequest {
        val payload =
            json.decodeFromString<FriendRequestCreatedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.NewFriendRequest(
            requestId = payload.id,
            requesterId = payload.requesterId,
            receiverId = payload.receiverId,
            createdAtMillis = payload.createdAt.toEpochMillisOrNow()
        )
    }

    private fun parseFriendRequestAccepted(envelope: WebSocketEnvelopeDto): WebSocketEvent.FriendRequestAccepted {
        val payload =
            json.decodeFromString<FriendRequestAcceptedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.FriendRequestAccepted(
            requestId = payload.id,
            requesterId = payload.requesterId,
            receiverId = payload.receiverId,
            createdAtMillis = payload.createdAt.toEpochMillisOrNow()
        )
    }

    private fun parseFriendRequestRemoved(envelope: WebSocketEnvelopeDto): WebSocketEvent.FriendRequestRemoved {
        val payload =
            json.decodeFromString<FriendRequestRemovedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.FriendRequestRemoved(
            requestId = payload.requestId,
            requesterId = payload.requesterId,
            receiverId = payload.receiverId
        )
    }

    private fun parseFriendshipStatusChanged(
        envelope: WebSocketEnvelopeDto
    ): WebSocketEvent.FriendshipStatusChanged {
        val payload =
            json.decodeFromString<FriendshipStatusChangedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.FriendshipStatusChanged(
            partnerId = payload.partnerId,
            isFriend = payload.isFriend
        )
    }

    private fun parseFriendProfileUpdated(envelope: WebSocketEnvelopeDto): WebSocketEvent.FriendProfileUpdated {
        val payload =
            json.decodeFromString<FriendProfileUpdatedPayloadDto>(envelope.payload.toString())
        return WebSocketEvent.FriendProfileUpdated(
            userId = payload.userId,
            username = payload.username,
            displayName = payload.effectiveDisplayName,
            avatarUrl = payload.effectiveAvatarUrl
        )
    }

    private companion object {
        const val Tag = "WsEventParser"
    }
}
