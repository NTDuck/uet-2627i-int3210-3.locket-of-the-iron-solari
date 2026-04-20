package com.solari.app.ui.models

import java.io.Serializable
import java.util.UUID

data class Conversation(
    val id: String,
    val otherUser: User,
    val lastMessage: String,
    val lastMessageSenderId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isUnread: Boolean = false,
    val isMuted: Boolean = false,
    val messages: List<Message> = emptyList()
) : Serializable

data class FriendRequest(
    val id: String,
    val user: User,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDeleted: Boolean = false,
    val referencedPostId: String? = null,
    val referencedPostThumbnailUrl: String? = null,
    val repliedMessageId: String? = null,
    val repliedMessagePreview: String? = null,
    val reactions: List<MessageReaction> = emptyList()
): Serializable

data class MessageReaction(
    val userId: String,
    val emoji: String
) : Serializable

data class Reply(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
): Serializable
