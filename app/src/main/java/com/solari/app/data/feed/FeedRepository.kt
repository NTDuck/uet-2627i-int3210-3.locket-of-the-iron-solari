package com.solari.app.data.feed

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import kotlinx.coroutines.flow.StateFlow

data class PaginatedFeed(
    val posts: List<Post>,
    val nextCursor: String?
)

interface FeedRepository {
    val deletedPostIds: StateFlow<Set<String>>

    suspend fun getFeed(
        authorIds: Set<String> = emptySet(),
        sort: String = "default",
        limit: Int = 15,
        cursor: String? = null
    ): ApiResult<PaginatedFeed>

    suspend fun getPost(postId: String): ApiResult<Post>

    suspend fun deletePost(postId: String): ApiResult<Unit>

    suspend fun getPostViewers(postId: String): ApiResult<List<PostActivityEntry>>

    suspend fun getPostReactions(postId: String): ApiResult<List<PostActivityEntry>>

    suspend fun registerPostView(postId: String): ApiResult<Unit>

    suspend fun sendPostReaction(
        postId: String,
        emoji: String,
        note: String?
    ): ApiResult<Unit>

    suspend fun initiatePostUpload(
        request: InitiatePostUploadRequest
    ): ApiResult<PostUploadSession>

    suspend fun uploadPostBinary(
        uploadUrl: String,
        contentType: String,
        bytes: ByteArray
    ): ApiResult<Unit>

    suspend fun finalizePostUpload(
        postId: String,
        objectKey: String
    ): ApiResult<Unit>
}
