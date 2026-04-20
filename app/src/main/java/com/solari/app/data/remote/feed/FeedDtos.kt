package com.solari.app.data.remote.feed

import com.solari.app.data.remote.common.ApiUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFeedResponseDto(
    val items: List<FeedPostDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

@Serializable
data class FeedPostDto(
    val id: String,
    val caption: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    val author: ApiUserDto,
    val media: FeedMediaDto
)

@Serializable
data class FeedMediaDto(
    val url: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("media_type")
    val mediaType: String,
    val width: Int,
    val height: Int,
    @SerialName("duration_ms")
    val durationMs: Int? = null
)

@Serializable
data class DeletePostResponseDto(
    val message: String
)
