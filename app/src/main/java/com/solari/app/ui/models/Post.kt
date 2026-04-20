package com.solari.app.ui.models

data class Post(
    val id: String,
    val author: User,
    val imageUrl: String = "https://www.politicon.com/wp-content/uploads/2017/06/Charlie-Kirk-2019-1024x1024.jpg",
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = "",
    val replies: List<Reply> = emptyList()
)
