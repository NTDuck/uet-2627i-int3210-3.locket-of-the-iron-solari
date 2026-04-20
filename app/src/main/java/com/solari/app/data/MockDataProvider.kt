package com.solari.app.data

import com.solari.app.ui.models.*
import java.util.*

class MockDataProvider {
    val users: List<User>
    val posts: List<Post>
    val conversations: MutableList<Conversation>
    val friendRequests: MutableList<FriendRequest>
    val blockedUsers: MutableList<User>
    var currentUser: User

    init {
        val names = listOf(
            "Alice", "Bob", "Charlie", "David", "Eve", "Frank", 
            "Grace", "Heidi", "Ivan", "Judy", "Mallory", "Niaj"
        )
        
        users = names.mapIndexed { index, name ->
            User(
                id = "user_$index",
                displayName = name,
                username = name.lowercase(),
                email = "${name.lowercase()}@example.com",
                profileImageUrl = "https://picsum.photos/seed/user_$index/200/200"
            )
        }

        currentUser = users[0]

        posts = users.flatMap { user ->
            (1..Random().nextInt(11) + 30).map { i ->
                Post(
                    id = "post_${user.id}_$i",
                    author = user,
                    imageUrl = "https://picsum.photos/seed/post_${user.id}_$i/1080/1080",
                    timestamp = System.currentTimeMillis() - Random().nextLong() % (1000L * 60 * 60 * 24 * 365),
                    caption = "Post #$i by ${user.displayName}",
                    replies = if (Random().nextBoolean()) listOf(
                        Reply(
                            userId = users[Random().nextInt(users.size)].id,
                            text = "Nice photo!",
                            timestamp = System.currentTimeMillis()
                        )
                    ) else emptyList()
                )
            }.sortedByDescending { it.timestamp }
        }

        conversations = mutableListOf()
        for (i in 1 until users.size) {
            val otherUser = users[i]
            val messages = (1..Random().nextInt(101) + 100).map { m ->
                val isMe = Random().nextBoolean()
                Message(
                    senderId = if (isMe) currentUser.id else otherUser.id,
                    text = "Message $m between ${currentUser.displayName} and ${otherUser.displayName}",
                    timestamp = 1577836800000L + (Random().nextLong() % (System.currentTimeMillis() - 1577836800000L)), // Since 2020
                    isRead = Random().nextBoolean()
                )
            }.sortedBy { it.timestamp }
            
            conversations.add(
                Conversation(
                    id = "conv_$i",
                    otherUser = otherUser,
                    lastMessage = messages.last().text,
                    timestamp = messages.last().timestamp,
                    isUnread = messages.last().senderId != currentUser.id && !messages.last().isRead,
                    messages = messages
                )
            )
        }

        friendRequests = mutableListOf()
        blockedUsers = mutableListOf()
    }

    fun getConversation(chatId: String): Conversation? {
        return conversations.find { it.id == chatId }
    }

    fun addMessage(chatId: String, text: String) {
        val convIndex = conversations.indexOfFirst { it.id == chatId }
        if (convIndex != -1) {
            val conv = conversations[convIndex]
            val newMessage = Message(
                senderId = currentUser.id,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            val updatedMessages = conv.messages + newMessage
            conversations[convIndex] = conv.copy(
                messages = updatedMessages,
                lastMessage = text,
                timestamp = newMessage.timestamp
            )
        }
    }

    fun deleteAccount() {
        // Logic to delete account
    }
}
