package com.solari.app.data.remote.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("signup")
    suspend fun signUp(
        @Body request: SignUpRequestDto
    ): Response<SignUpResponseDto>

    @POST("signin")
    suspend fun signIn(
        @Body request: SignInRequestDto
    ): Response<SignInResponseDto>

    @POST("sessions/refresh")
    suspend fun refreshSession(
        @Body request: RefreshSessionRequestDto
    ): Response<SignInResponseDto>
}
