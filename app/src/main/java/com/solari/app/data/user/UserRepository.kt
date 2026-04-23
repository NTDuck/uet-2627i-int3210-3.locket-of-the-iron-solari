package com.solari.app.data.user

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.BlockedUser
import com.solari.app.ui.models.User

data class ProfileAvatarUpload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
)

sealed interface DeleteAccountVerification {
    data class Password(val password: String) : DeleteAccountVerification
    data class GoogleIdToken(val idToken: String) : DeleteAccountVerification
}

interface UserRepository {
    suspend fun getMe(): ApiResult<User>

    suspend fun getPublicProfile(username: String): ApiResult<User>

    suspend fun updateProfile(
        email: String? = null,
        displayName: String? = null,
        avatar: ProfileAvatarUpload? = null,
        removeDisplayName: Boolean = false,
        removeAvatar: Boolean = false
    ): ApiResult<User>

    suspend fun getCurrentStreak(timezone: String): ApiResult<Int>

    suspend fun blockUser(userId: String): ApiResult<Unit>

    suspend fun unblockUser(userId: String): ApiResult<Unit>

    suspend fun getBlockedUsers(sort: String = "newest"): ApiResult<List<BlockedUser>>

    suspend fun updatePassword(oldPassword: String, newPassword: String): ApiResult<Unit>

    suspend fun deleteAccount(verification: DeleteAccountVerification): ApiResult<Unit>
}
