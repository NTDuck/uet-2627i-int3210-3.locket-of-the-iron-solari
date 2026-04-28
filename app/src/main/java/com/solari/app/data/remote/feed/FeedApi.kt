package com.solari.app.data.remote.feed

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.solari.app.data.remote.common.MessageResponseDto

interface FeedApi {
    @GET("feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = 15,
        @Query("cursor") cursor: String? = null,
        @Query("authors") authors: String? = null,
        @Query("sort") sort: String? = null
    ): Response<GetFeedResponseDto>

    @GET("posts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: String
    ): Response<GetPostResponseDto>

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ): Response<DeletePostResponseDto>

    @GET("posts/{postId}/viewers")
    suspend fun getPostViewers(
        @Path("postId") postId: String,
        @Query("limit") limit: Int = 50,
        @Query("cursor") cursor: String? = null
    ): Response<GetPostViewersResponseDto>

    @GET("posts/{postId}/reactions")
    suspend fun getPostReactions(
        @Path("postId") postId: String,
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): Response<GetPostReactionsResponseDto>

    @POST("posts/{postId}/views")
    suspend fun registerPostView(
        @Path("postId") postId: String
    ): Response<MessageResponseDto>

    @POST("posts/{postId}/reactions")
    suspend fun sendPostReaction(
        @Path("postId") postId: String,
        @Body request: SendPostReactionRequestDto
    ): Response<MessageResponseDto>

    @POST("posts/initiate")
    suspend fun initiatePostUpload(
        @Body request: InitiatePostUploadRequestDto
    ): Response<InitiatePostUploadResponseDto>

    @POST("posts/finalize")
    suspend fun finalizePostUpload(
        @Body request: FinalizePostUploadRequestDto
    ): Response<FinalizePostUploadResponseDto>
}
