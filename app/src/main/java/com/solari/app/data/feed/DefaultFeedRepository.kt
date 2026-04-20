package com.solari.app.data.feed

import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.feed.FeedApi
import com.solari.app.data.remote.feed.FeedPostDto
import com.solari.app.ui.models.Post

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

    private fun FeedPostDto.toUiPost(): Post {
        return Post(
            id = id,
            author = author.toUiUser(),
            imageUrl = media.thumbnailUrl ?: media.url,
            timestamp = createdAt.toEpochMillisOrNow(),
            caption = caption.orEmpty()
        )
    }
}
