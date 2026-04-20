package com.solari.app.ui.models

data class Post(
    val id: String,
    val author: User,
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val mediaType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = "",
    val replies: List<Reply> = emptyList()
)

data class PostActivityEntry(
    val user: User,
    val emoji: String?,
    val caption: String?,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
