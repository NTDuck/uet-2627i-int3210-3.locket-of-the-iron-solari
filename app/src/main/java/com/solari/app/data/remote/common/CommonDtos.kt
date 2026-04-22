package com.solari.app.data.remote.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiUserDto(
    val id: String,
    val username: String,
    val email: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("avatarUrl")
    val legacyAvatarUrl: String? = null,
    val nickname: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val effectiveAvatarUrl: String?
        get() = avatarUrl ?: legacyAvatarUrl
}

@Serializable
data class MessageResponseDto(
    val message: String? = null
)
