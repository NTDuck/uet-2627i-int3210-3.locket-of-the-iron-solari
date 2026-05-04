package com.solari.app.ui.models

import java.io.Serializable

sealed class CaptionMetadata : Serializable {
    data class Text(val data: String? = null) : CaptionMetadata()
    object Ootd : CaptionMetadata()
    data class Weather(val condition: String, val temperatureC: Float? = null) : CaptionMetadata()
    data class Location(val placeName: String) : CaptionMetadata()
    data class Rating(val starRating: Float, val review: String? = null) : CaptionMetadata()
    data class Clock(val time: String) : CaptionMetadata()
}

data class Post(
    val id: String,
    val author: User,
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val mediaType: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val caption: String = "",
    val captionType: String = "text",
    val captionMetadata: CaptionMetadata? = null,
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
