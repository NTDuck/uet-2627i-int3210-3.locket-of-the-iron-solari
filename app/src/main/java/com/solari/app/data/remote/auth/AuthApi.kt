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

    @POST("signin/google")
    suspend fun signInWithGoogle(
        @Body request: GoogleSignInRequestDto
    ): Response<SignInResponseDto>

    @POST("sessions/refresh")
    suspend fun refreshSession(
        @Body request: RefreshSessionRequestDto
    ): Response<SignInResponseDto>

    @POST("signout")
    suspend fun signOut(
        @Body request: SignOutRequestDto
    ): Response<com.solari.app.data.remote.common.MessageResponseDto>

    @POST("password-resets")
    suspend fun requestPasswordReset(
        @Body request: PasswordResetRequestDto
    ): Response<com.solari.app.data.remote.common.MessageResponseDto>

    @POST("password-resets/verify")
    suspend fun verifyPasswordReset(
        @Body request: PasswordResetVerifyRequestDto
    ): Response<PasswordResetVerifyResponseDto>

    @POST("password-resets/complete")
    suspend fun completePasswordReset(
        @Body request: PasswordResetCompleteRequestDto
    ): Response<com.solari.app.data.remote.common.MessageResponseDto>
}
