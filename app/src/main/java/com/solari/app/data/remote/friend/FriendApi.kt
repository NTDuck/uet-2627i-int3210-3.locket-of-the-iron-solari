package com.solari.app.data.remote.friend

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendApi {
    @GET("friends")
    suspend fun getFriends(
        @Query("limit") limit: Int = 100,
        @Query("sort") sort: String = "newest",
        @Query("cursor") cursor: String? = null
    ): Response<GetFriendsResponseDto>

    @GET("friend-requests")
    suspend fun getFriendRequests(
        @Query("limit") limit: Int = 100,
        @Query("direction") direction: String = "both",
        @Query("sort") sort: String = "newest",
        @Query("cursor") cursor: String? = null
    ): Response<GetFriendRequestsResponseDto>

    @PATCH("friend-requests/{requestId}")
    suspend fun acceptFriendRequest(
        @Path("requestId") requestId: String
    ): Response<FriendRequestMutationResponseDto>

    @DELETE("friend-requests/{requestId}")
    suspend fun deleteFriendRequest(
        @Path("requestId") requestId: String
    ): Response<FriendRequestDeleteResponseDto>
}
