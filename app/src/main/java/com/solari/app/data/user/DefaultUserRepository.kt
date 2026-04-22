package com.solari.app.data.user

import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.user.BlockedUserDto
import com.solari.app.data.remote.user.DeleteAccountRequestDto
import com.solari.app.data.remote.user.UpdatePasswordRequestDto
import com.solari.app.data.remote.user.UserApi
import com.solari.app.ui.models.BlockedUser
import com.solari.app.ui.models.User
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DefaultUserRepository(
    private val userApi: UserApi,
    private val apiExecutor: ApiExecutor
) : UserRepository {
    override suspend fun getMe(): ApiResult<User> {
        return when (val result = apiExecutor.execute { userApi.getMe() }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.user.toUiUser())
        }
    }

    override suspend fun getPublicProfile(username: String): ApiResult<User> {
        return when (val result = apiExecutor.execute { userApi.getPublicProfile(username) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.profile.toUiUser())
        }
    }

    override suspend fun updateProfile(
        email: String?,
        displayName: String?,
        avatar: ProfileAvatarUpload?,
        removeDisplayName: Boolean,
        removeAvatar: Boolean
    ): ApiResult<User> {
        val fields = buildMap {
            email?.let { put("email", it.toPlainTextRequestBody()) }
            displayName?.let { put("display_name", it.toPlainTextRequestBody()) }
            if (removeDisplayName) put("remove_display_name", "true".toPlainTextRequestBody())
            if (removeAvatar) put("remove_avatar", "true".toPlainTextRequestBody())
        }
        val avatarPart = avatar?.toMultipartPart()

        return when (val result = apiExecutor.execute { userApi.updateProfile(fields, avatarPart) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.user.toUiUser())
        }
    }

    override suspend fun getCurrentStreak(timezone: String): ApiResult<Int> {
        return when (val result = apiExecutor.execute { userApi.getCurrentUserStreak(timezone) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.currentStreak)
        }
    }

    override suspend fun deleteAccount(verification: DeleteAccountVerification): ApiResult<Unit> {
        val request = when (verification) {
            is DeleteAccountVerification.Password -> {
                DeleteAccountRequestDto(password = verification.password)
            }

            is DeleteAccountVerification.GoogleIdToken -> {
                DeleteAccountRequestDto(googleIdToken = verification.idToken)
            }
        }

        return when (
            val result = apiExecutor.execute {
                userApi.deleteAccount(request)
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun updatePassword(oldPassword: String, newPassword: String): ApiResult<Unit> {
        return when (
            val result = apiExecutor.execute {
                userApi.updatePassword(
                    UpdatePasswordRequestDto(
                        oldPassword = oldPassword,
                        newPassword = newPassword
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun blockUser(userId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { userApi.blockUser(userId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun unblockUser(userId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { userApi.unblockUser(userId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun getBlockedUsers(sort: String): ApiResult<List<BlockedUser>> {
        return when (val result = apiExecutor.execute { userApi.getBlockedUsers(sort = sort) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiBlockedUser() })
        }
    }

    private fun String.toPlainTextRequestBody() =
        toRequestBody("text/plain".toMediaType())

    private fun ProfileAvatarUpload.toMultipartPart(): MultipartBody.Part {
        val body = bytes.toRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData("avatar", fileName, body)
    }

    private fun BlockedUserDto.toUiBlockedUser(): BlockedUser {
        return BlockedUser(
            user = User(
                id = id,
                displayName = displayName ?: username,
                username = username,
                email = email.orEmpty(),
                profileImageUrl = effectiveAvatarUrl,
                nickname = nickname
            ),
            blockedAt = blockedAt.toEpochMillisOrNow()
        )
    }
}
