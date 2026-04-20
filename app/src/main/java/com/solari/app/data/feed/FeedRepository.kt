package com.solari.app.data.feed

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Post

interface FeedRepository {
    suspend fun getFeed(authorIds: Set<String> = emptySet()): ApiResult<List<Post>>

    suspend fun deletePost(postId: String): ApiResult<Unit>
}
