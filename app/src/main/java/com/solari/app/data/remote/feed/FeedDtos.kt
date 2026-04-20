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

@Serializable
data class GetPostViewersResponseDto(
    val items: List<PostViewerDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

@Serializable
data class PostViewerDto(
    val id: String,
    val username: String,
    val email: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_key")
    val avatarKey: String? = null,
    @SerialName("avatarUrl")
    val avatarUrl: String? = null,
    @SerialName("viewed_at")
    val viewedAt: String
)

@Serializable
data class GetPostReactionsResponseDto(
    val items: List<PostReactionDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

@Serializable
data class PostReactionDto(
    val id: String,
    val emoji: String,
    val note: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    val user: ApiUserDto
)

@Serializable
data class SendPostReactionRequestDto(
    val emoji: String,
    val note: String? = null
)
