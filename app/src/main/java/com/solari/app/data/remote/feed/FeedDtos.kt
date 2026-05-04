package com.solari.app.data.remote.feed

import com.solari.app.data.remote.common.ApiUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class CaptionMetadataDto {
    @Serializable
    @SerialName("text")
    data class Text(val data: String? = null) : CaptionMetadataDto()

    @Serializable
    @SerialName("ootd")
    data class Ootd(val data: JsonObject? = null) : CaptionMetadataDto()

    @Serializable
    @SerialName("weather")
    data class Weather(val data: WeatherDataDto) : CaptionMetadataDto() {
        @Serializable
        data class WeatherDataDto(val condition: String, @SerialName("temperature_c") val temperatureC: Float? = null)
    }

    @Serializable
    @SerialName("location")
    data class Location(val data: LocationDataDto) : CaptionMetadataDto() {
        @Serializable
        data class LocationDataDto(@SerialName("place_name") val placeName: String)
    }

    @Serializable
    @SerialName("rating")
    data class Rating(val data: RatingDataDto) : CaptionMetadataDto() {
        @Serializable
        data class RatingDataDto(@SerialName("star_rating") val starRating: Float, val review: String? = null)
    }

    @Serializable
    @SerialName("clock")
    data class Clock(val data: ClockDataDto) : CaptionMetadataDto() {
        @Serializable
        data class ClockDataDto(val time: String)
    }
}

@Serializable
data class GetFeedResponseDto(
    val items: List<FeedPostDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

@Serializable
data class GetPostResponseDto(
    val post: FeedPostDto
)

@Serializable
data class FeedPostDto(
    val id: String,
    val caption: String? = null,
    @SerialName("caption_type")
    val captionType: String? = null,
    @SerialName("caption_metadata")
    val captionMetadata: CaptionMetadataDto? = null,
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
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("avatarUrl")
    val legacyAvatarUrl: String? = null,
    @SerialName("viewed_at")
    val viewedAt: String
) {
    val effectiveAvatarUrl: String?
        get() = avatarUrl ?: legacyAvatarUrl
}

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

@Serializable
data class InitiatePostUploadRequestDto(
    @SerialName("content_type")
    val contentType: String,
    val caption: String? = null,
    @SerialName("caption_type")
    val captionType: String? = null,
    @SerialName("caption_metadata")
    val captionMetadata: CaptionMetadataDto? = null,
    @SerialName("audience_type")
    val audienceType: String,
    @SerialName("viewer_ids")
    val viewerIds: String? = null,
    val width: Int,
    val height: Int,
    @SerialName("byte_size")
    val byteSize: Long,
    @SerialName("duration_ms")
    val durationMs: Long? = null,
    val timezone: String
)

@Serializable
data class InitiatePostUploadResponseDto(
    @SerialName("post_id")
    val postId: String,
    @SerialName("object_key")
    val objectKey: String,
    @SerialName("upload_url")
    val uploadUrl: String
)

@Serializable
data class FinalizePostUploadRequestDto(
    @SerialName("post_id")
    val postId: String,
    @SerialName("object_key")
    val objectKey: String
)

@Serializable
data class FinalizePostUploadResponseDto(
    val message: String,
    @SerialName("post_id")
    val postId: String,
    val status: String
)
