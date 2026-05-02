package com.solari.app.ui.models

import java.io.Serializable

data class Post(
    val id: String,
    val author: User,
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val mediaType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = "",
    val uploadStatus: PostUploadStatus = PostUploadStatus.None,
    val uploadError: String? = null,
    val replies: List<Reply> = emptyList()
) : Serializable

enum class PostUploadStatus : Serializable {
    None,
    Uploading,
    Processing,
    Failed
}

data class PostActivityEntry(
    val user: User,
    val emoji: String?,
    val caption: String?,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
