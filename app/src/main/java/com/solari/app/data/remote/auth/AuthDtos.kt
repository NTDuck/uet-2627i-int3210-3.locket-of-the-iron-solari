package com.solari.app.data.remote.auth

import com.solari.app.data.remote.common.ApiUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequestDto(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class SignUpResponseDto(
    val message: String,
    val user: ApiUserDto
)

@Serializable
data class SignInRequestDto(
    val identifier: String,
    val password: String
)

@Serializable
data class RefreshSessionRequestDto(
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
data class SignInResponseDto(
    val message: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_at")
    val expiresAt: String
)
