package com.solari.app.data.remote.friend

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendApi {
    @GET("friends")
    suspend fun getFriends(
        @Query("limit") limit: Int = 100,
        @Query("sort") sort: String? = null,
        @Query("cursor") cursor: String? = null
    ): Response<GetFriendsResponseDto>

    @GET("nicknames")
    suspend fun getNicknames(
        @Query("limit") limit: Int = 100,
        @Query("cursor") cursor: String? = null
    ): Response<GetNicknamesResponseDto>

    @GET("friend-requests")
    suspend fun getFriendRequests(
        @Query("limit") limit: Int = 100,
        @Query("direction") direction: String = "both",
        @Query("sort") sort: String = "newest",
        @Query("cursor") cursor: String? = null
    ): Response<GetFriendRequestsResponseDto>

    @POST("friend-requests")
    suspend fun sendFriendRequest(
        @Body request: SendFriendRequestDto
    ): Response<FriendRequestMutationResponseDto>

    @PATCH("friend-requests/{requestId}")
    suspend fun acceptFriendRequest(
        @Path("requestId") requestId: String
    ): Response<FriendRequestMutationResponseDto>

    @DELETE("friend-requests/{requestId}")
    suspend fun deleteFriendRequest(
        @Path("requestId") requestId: String
    ): Response<FriendRequestDeleteResponseDto>

    @DELETE("friendships/{friendId}")
    suspend fun unfriend(
        @Path("friendId") friendId: String
    ): Response<FriendshipMutationResponseDto>

    @POST("nicknames/{targetUserId}")
    suspend fun setNickname(
        @Path("targetUserId") targetUserId: String,
        @Body request: SetNicknameRequestDto
    ): Response<NicknameMutationResponseDto>

    @PATCH("nicknames/{targetUserId}")
    suspend fun updateNickname(
        @Path("targetUserId") targetUserId: String,
        @Body request: UpdateNicknameRequestDto
    ): Response<NicknameMutationResponseDto>

    @DELETE("nicknames/{targetUserId}")
    suspend fun removeNickname(
        @Path("targetUserId") targetUserId: String
    ): Response<NicknameMutationResponseDto>
}
