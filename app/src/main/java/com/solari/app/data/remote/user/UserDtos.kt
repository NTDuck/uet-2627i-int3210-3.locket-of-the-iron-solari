package com.solari.app.data.remote.user

import com.solari.app.data.remote.common.ApiUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMeResponseDto(
    val message: String,
    @SerialName("session_id")
    val sessionId: String,
    val user: ApiUserDto
)

@Serializable
data class UpdateUserProfileResponseDto(
    val message: String,
    val user: ApiUserDto
)

@Serializable
data class DeleteAccountResponseDto(
    val message: String
)
