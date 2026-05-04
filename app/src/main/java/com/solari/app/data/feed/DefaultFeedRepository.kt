package com.solari.app.data.feed

import android.util.Log
import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.feed.FeedApi
import com.solari.app.data.remote.feed.FeedPostDto
import com.solari.app.data.remote.feed.FinalizePostUploadRequestDto
import com.solari.app.data.remote.feed.InitiatePostUploadRequestDto
import com.solari.app.data.remote.feed.PostReactionDto
import com.solari.app.data.remote.feed.PostViewerDto
import com.solari.app.data.remote.feed.SendPostReactionRequestDto
import com.solari.app.ui.models.CaptionMetadata
import com.solari.app.data.remote.feed.CaptionMetadataDto
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class DefaultFeedRepository(
    private val feedApi: FeedApi,
    private val apiExecutor: ApiExecutor,
    private val uploadClient: OkHttpClient = OkHttpClient()
) : FeedRepository {
    private val uploadLogTag = "SolariUpload"
    private val _deletedPostIds = MutableStateFlow<Set<String>>(emptySet())

    override val deletedPostIds: StateFlow<Set<String>> = _deletedPostIds.asStateFlow()

    override suspend fun getFeed(
        authorIds: Set<String>,
        sort: String,
        limit: Int,
        cursor: String?
    ): ApiResult<PaginatedFeed> {
        val authors = authorIds.takeIf { it.isNotEmpty() }?.joinToString(",")
        val feedSort = when (sort) {
            "oldest" -> "oldest"
            else -> "newest"
        }
        return when (
            val result = apiExecutor.execute {
                feedApi.getFeed(limit = limit, cursor = cursor, authors = authors, sort = feedSort)
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(
                PaginatedFeed(
                    posts = result.data.items.map { it.toUiPost() },
                    nextCursor = result.data.nextCursor
                )
            )
        }
    }

    override suspend fun getPost(postId: String): ApiResult<Post> {
        return when (val result = apiExecutor.execute { feedApi.getPost(postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.post.toUiPost())
        }
    }

    override suspend fun deletePost(postId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { feedApi.deletePost(postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                _deletedPostIds.update { it + postId }
                ApiResult.Success(Unit)
            }
        }
    }

    override suspend fun getPostViewers(postId: String): ApiResult<List<PostActivityEntry>> {
        return when (val result = apiExecutor.execute { feedApi.getPostViewers(postId = postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiActivityEntry() })
        }
    }

    override suspend fun getPostReactions(postId: String): ApiResult<List<PostActivityEntry>> {
        return when (val result =
            apiExecutor.execute { feedApi.getPostReactions(postId = postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiActivityEntry() })
        }
    }

    override suspend fun registerPostView(postId: String): ApiResult<Unit> {
        return when (val result =
            apiExecutor.execute { feedApi.registerPostView(postId = postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun sendPostReaction(
        postId: String,
        emoji: String,
        note: String?
    ): ApiResult<Unit> {
        val trimmedNote = note?.trim()?.takeIf { it.isNotEmpty() }
        return when (
            val result = apiExecutor.execute {
                feedApi.sendPostReaction(
                    postId = postId,
                    request = SendPostReactionRequestDto(
                        emoji = emoji,
                        note = trimmedNote
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun initiatePostUpload(
        request: InitiatePostUploadRequest
    ): ApiResult<PostUploadSession> {
        return when (
            val result = apiExecutor.execute {
                feedApi.initiatePostUpload(
                    request = InitiatePostUploadRequestDto(
                        contentType = request.contentType,
                        caption = request.caption,
                        captionType = request.captionType,
                        captionMetadata = request.captionMetadata?.toDto(),
                        audienceType = request.audienceType,
                        viewerIds = request.viewerIds.takeIf { it.isNotEmpty() }?.joinToString(","),
                        width = request.width,
                        height = request.height,
                        byteSize = request.byteSize,
                        durationMs = request.durationMs,
                        timezone = request.timezone
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(
                PostUploadSession(
                    postId = result.data.postId,
                    objectKey = result.data.objectKey,
                    uploadUrl = result.data.uploadUrl
                )
            )
        }
    }

    override suspend fun uploadPostBinary(
        uploadUrl: String,
        contentType: String,
        bytes: ByteArray
    ): ApiResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = uploadClient.newCall(
                    Request.Builder()
                        .url(uploadUrl)
                        .put(bytes.toRequestBody(contentType.toMediaType()))
                        .header("Content-Type", contentType)
                        .build()
                ).execute()

                response.use {
                    if (it.isSuccessful) {
                        ApiResult.Success(Unit)
                    } else {
                        val errorBody = it.body?.string()?.take(2_000).orEmpty()
                        Log.e(
                            uploadLogTag,
                            "Presigned upload failed: status=${it.code}, " +
                                    "contentType=$contentType, bytes=${bytes.size}, " +
                                    "host=${it.request.url.host}, path=${it.request.url.encodedPath}, " +
                                    "body=$errorBody"
                        )
                        ApiResult.Failure(
                            statusCode = it.code,
                            type = "UPLOAD_FAILED",
                            message = "Media upload failed (${it.code}). Check Logcat tag $uploadLogTag."
                        )
                    }
                }
            } catch (error: Exception) {
                Log.e(
                    uploadLogTag,
                    "Presigned upload crashed: contentType=$contentType, bytes=${bytes.size}",
                    error
                )
                ApiResult.Failure(
                    statusCode = null,
                    type = "UPLOAD_FAILED",
                    message = error.message
                        ?: "Media upload failed. Check Logcat tag $uploadLogTag.",
                    cause = error
                )
            }
        }
    }

    override suspend fun finalizePostUpload(
        postId: String,
        objectKey: String
    ): ApiResult<Unit> {
        return when (
            val result = apiExecutor.execute {
                feedApi.finalizePostUpload(
                    request = FinalizePostUploadRequestDto(
                        postId = postId,
                        objectKey = objectKey
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    private fun FeedPostDto.toUiPost(): Post {
        return Post(
            id = id,
            author = author.toUiUser(),
            imageUrl = media.url,
            thumbnailUrl = media.thumbnailUrl.orEmpty(),
            mediaType = media.mediaType,
            timestamp = createdAt.toEpochMillisOrNow(),
            caption = caption.orEmpty(),
            captionType = captionType ?: "text",
            captionMetadata = captionMetadata?.toUiMetadata()
        )
    }

    private fun CaptionMetadataDto.toUiMetadata(): CaptionMetadata = when (this) {
        is CaptionMetadataDto.Clock -> CaptionMetadata.Clock(data.time)
        is CaptionMetadataDto.Location -> CaptionMetadata.Location(data.placeName)
        is CaptionMetadataDto.Ootd -> CaptionMetadata.Ootd
        is CaptionMetadataDto.Rating -> CaptionMetadata.Rating(data.starRating, data.review)
        is CaptionMetadataDto.Text -> CaptionMetadata.Text(data)
        is CaptionMetadataDto.Weather -> CaptionMetadata.Weather(data.condition, data.temperatureC)
    }

    private fun CaptionMetadata.toDto(): CaptionMetadataDto = when (this) {
        is CaptionMetadata.Clock -> CaptionMetadataDto.Clock(CaptionMetadataDto.Clock.ClockDataDto(time))
        is CaptionMetadata.Location -> CaptionMetadataDto.Location(CaptionMetadataDto.Location.LocationDataDto(placeName))
        CaptionMetadata.Ootd -> CaptionMetadataDto.Ootd(null)
        is CaptionMetadata.Rating -> CaptionMetadataDto.Rating(CaptionMetadataDto.Rating.RatingDataDto(starRating, review))
        is CaptionMetadata.Text -> CaptionMetadataDto.Text(data)
        is CaptionMetadata.Weather -> CaptionMetadataDto.Weather(CaptionMetadataDto.Weather.WeatherDataDto(condition, temperatureC))
    }

    private fun PostViewerDto.toUiActivityEntry(): PostActivityEntry {
        return PostActivityEntry(
            user = User(
                id = id,
                displayName = displayName ?: username,
                username = username,
                email = email.orEmpty(),
                profileImageUrl = effectiveAvatarUrl
            ),
            emoji = null,
            caption = null,
            description = "Viewed! ✨",
            timestamp = viewedAt.toEpochMillisOrNow()
        )
    }

    private fun PostReactionDto.toUiActivityEntry(): PostActivityEntry {
        return PostActivityEntry(
            user = user.toUiUser(),
            emoji = emoji,
            caption = note?.takeIf { it.isNotBlank() },
            description = "Reacted",
            timestamp = createdAt.toEpochMillisOrNow()
        )
    }
}
