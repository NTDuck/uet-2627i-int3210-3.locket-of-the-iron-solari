package com.solari.app.ui.models

import java.util.UUID

data class Conversation(
    val id: String,
    val otherUser: User,
    val lastMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUnread: Boolean = false,
    val messages: List<Message> = emptyList()
)

data class FriendRequest(
    val id: String,
    val user: User,
    val timestamp: Long = System.currentTimeMillis()
)

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val reactions: List<String> = emptyList()
)

data class Reply(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
