package com.solari.app.data.feed

import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.feed.FeedApi
import com.solari.app.data.remote.feed.FeedPostDto
import com.solari.app.data.remote.feed.PostReactionDto
import com.solari.app.data.remote.feed.PostViewerDto
import com.solari.app.data.remote.feed.SendPostReactionRequestDto
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.User

class DefaultFeedRepository(
    private val feedApi: FeedApi,
    private val apiExecutor: ApiExecutor
) : FeedRepository {
    override suspend fun getFeed(authorIds: Set<String>): ApiResult<List<Post>> {
        val authors = authorIds.takeIf { it.isNotEmpty() }?.joinToString(",")
        return when (val result = apiExecutor.execute { feedApi.getFeed(authors = authors) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiPost() })
        }
    }

    override suspend fun deletePost(postId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { feedApi.deletePost(postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun getPostViewers(postId: String): ApiResult<List<PostActivityEntry>> {
        return when (val result = apiExecutor.execute { feedApi.getPostViewers(postId = postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiActivityEntry() })
        }
    }

    override suspend fun getPostReactions(postId: String): ApiResult<List<PostActivityEntry>> {
        return when (val result = apiExecutor.execute { feedApi.getPostReactions(postId = postId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiActivityEntry() })
        }
    }

    override suspend fun registerPostView(postId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { feedApi.registerPostView(postId = postId) }) {
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

    private fun FeedPostDto.toUiPost(): Post {
        return Post(
            id = id,
            author = author.toUiUser(),
            imageUrl = media.url,
            thumbnailUrl = media.thumbnailUrl.orEmpty(),
            mediaType = media.mediaType,
            timestamp = createdAt.toEpochMillisOrNow(),
            caption = caption.orEmpty()
        )
    }

    private fun PostViewerDto.toUiActivityEntry(): PostActivityEntry {
        return PostActivityEntry(
            user = User(
                id = id,
                displayName = displayName ?: username,
                username = username,
                email = email.orEmpty(),
                profileImageUrl = avatarUrl.orEmpty()
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
