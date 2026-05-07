package com.solari.app.data.remote.user

import com.solari.app.data.remote.common.MessageResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {
    @GET("me")
    suspend fun getMe(): Response<GetMeResponseDto>

    @GET("users/public/{username}")
    suspend fun getPublicProfile(
        @Path("username") username: String
    ): Response<GetPublicProfileResponseDto>

    @Multipart
    @PATCH("users/me")
    suspend fun updateProfile(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part avatar: MultipartBody.Part? = null
    ): Response<UpdateUserProfileResponseDto>

    @GET("users/me/streak")
    suspend fun getCurrentUserStreak(
        @Query("timezone") timezone: String
    ): Response<UserStreakResponseDto>

    @HTTP(method = "DELETE", path = "users/me", hasBody = true)
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequestDto
    ): Response<DeleteAccountResponseDto>

    @PATCH("users/password")
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequestDto
    ): Response<UpdatePasswordResponseDto>

    @POST("users/me/devices")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequestDto
    ): Response<MessageResponseDto>

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
