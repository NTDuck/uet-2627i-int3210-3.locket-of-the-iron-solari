package com.solari.app.data.remote.user

import okhttp3.RequestBody
import okhttp3.MultipartBody
import com.solari.app.data.remote.common.MessageResponseDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("me")
    suspend fun getMe(): Response<GetMeResponseDto>

    @Multipart
    @PATCH("users/me")
    suspend fun updateProfile(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<UpdateUserProfileResponseDto>

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<DeleteAccountResponseDto>

    @POST("users/{targetId}/block")
    suspend fun blockUser(
        @Path("targetId") targetId: String
    ): Response<MessageResponseDto>

    @DELETE("users/{targetId}/block")
    suspend fun unblockUser(
        @Path("targetId") targetId: String
    ): Response<MessageResponseDto>

    @GET("users/me/blocked")
    suspend fun getBlockedUsers(
        @Query("limit") limit: Int = 100,
        @Query("sort") sort: String = "newest",
        @Query("cursor") cursor: String? = null
    ): Response<GetBlockedUsersResponseDto>
}
