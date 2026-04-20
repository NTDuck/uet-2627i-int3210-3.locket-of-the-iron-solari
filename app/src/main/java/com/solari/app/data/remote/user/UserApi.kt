package com.solari.app.data.remote.user

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.PartMap

interface UserApi {
    @GET("me")
    suspend fun getMe(): Response<GetMeResponseDto>

    @Multipart
    @PATCH("users/me")
    suspend fun updateProfile(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<UpdateUserProfileResponseDto>

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<DeleteAccountResponseDto>
}
