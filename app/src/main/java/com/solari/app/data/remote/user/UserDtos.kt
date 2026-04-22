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
data class GetPublicProfileResponseDto(
    val profile: ApiUserDto
)

@Serializable
data class UpdateUserProfileResponseDto(
    val message: String,
    val user: ApiUserDto
)

@Serializable
data class UserStreakResponseDto(
    val currentStreak: Int,
    val longestStreak: Int? = null,
    val lastPostDate: String? = null,
    val isAlive: Boolean = false,
    val postedToday: Boolean = false
)

@Serializable
data class DeleteAccountResponseDto(
    val message: String
)

@Serializable
data class DeleteAccountRequestDto(
    val password: String
)

@Serializable
data class UpdatePasswordRequestDto(
    @SerialName("old_password")
    val oldPassword: String,
    @SerialName("new_password")
    val newPassword: String
)

@Serializable
data class UpdatePasswordResponseDto(
    val message: String
)

@Serializable
data class GetBlockedUsersResponseDto(
    val items: List<BlockedUserDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null,
    val limit: Int? = null
)

@Serializable
data class BlockedUserDto(
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
    @SerialName("blocked_at")
    val blockedAt: String
) {
    val effectiveAvatarUrl: String?
        get() = avatarUrl ?: legacyAvatarUrl
}
