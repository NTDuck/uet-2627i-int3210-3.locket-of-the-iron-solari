package com.solari.app.data.feed

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class PaginatedFeed(
    val posts: List<Post>,
    val nextCursor: String?
)

data class PaginatedPostActivityEntries(
    val activities: List<PostActivityEntry>,
    val nextCursor: String?
)

interface FeedRepository {
    val deletedPostIds: StateFlow<Set<String>>
    val newlyPublishedPosts: SharedFlow<Post>

    suspend fun emitNewlyPublishedPost(postId: String)
    suspend fun emitNewlyPublishedPost(post: Post)

    suspend fun getFeed(
        authorIds: Set<String> = emptySet(),
        sort: String = "default",
        limit: Int = 15,
        cursor: String? = null
    ): ApiResult<PaginatedFeed>

    suspend fun getPost(postId: String): ApiResult<Post>

    suspend fun deletePost(postId: String): ApiResult<Unit>

    suspend fun getPostViewers(postId: String): ApiResult<List<PostActivityEntry>>

    suspend fun getPostViewersPage(
        postId: String,
        limit: Int,
        cursor: String? = null
    ): ApiResult<PaginatedPostActivityEntries>

    suspend fun getPostReactions(postId: String): ApiResult<List<PostActivityEntry>>

    suspend fun getPostReactionsPage(
        postId: String,
        limit: Int,
        cursor: String? = null
    ): ApiResult<PaginatedPostActivityEntries>

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
