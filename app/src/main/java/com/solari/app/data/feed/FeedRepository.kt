package com.solari.app.data.feed

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry

interface FeedRepository {
    suspend fun getFeed(authorIds: Set<String> = emptySet()): ApiResult<List<Post>>

    suspend fun deletePost(postId: String): ApiResult<Unit>

    suspend fun getPostViewers(postId: String): ApiResult<List<PostActivityEntry>>

    suspend fun getPostReactions(postId: String): ApiResult<List<PostActivityEntry>>

    suspend fun registerPostView(postId: String): ApiResult<Unit>

    suspend fun sendPostReaction(
        postId: String,
        emoji: String,
        note: String?
    ): ApiResult<Unit>
}
