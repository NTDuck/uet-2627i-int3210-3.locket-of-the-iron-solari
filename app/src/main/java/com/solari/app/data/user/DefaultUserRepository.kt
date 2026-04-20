package com.solari.app.data.user

import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.user.UserApi
import com.solari.app.ui.models.User
import okhttp3.MediaType.Companion.toMediaType
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

    override suspend fun updateProfile(
        email: String?,
        displayName: String?
    ): ApiResult<User> {
        val fields = buildMap {
            email?.let { put("email", it.toPlainTextRequestBody()) }
            displayName?.let { put("display_name", it.toPlainTextRequestBody()) }
        }

        return when (val result = apiExecutor.execute { userApi.updateProfile(fields) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.user.toUiUser())
        }
    }

    override suspend fun deleteAccount(): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { userApi.deleteAccount() }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    private fun String.toPlainTextRequestBody() =
        toRequestBody("text/plain".toMediaType())
}
