package com.solari.app.ui.models

data class Post(
    val id: String,
    val author: User,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = "",
    val replies: List<Reply> = emptyList()
)
