package com.solari.app.data.remote.feed

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedApi {
    @GET("feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null,
        @Query("authors") authors: String? = null
    ): Response<GetFeedResponseDto>

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String
    ): Response<DeletePostResponseDto>
}
