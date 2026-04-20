package com.solari.app.data.user

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.User

interface UserRepository {
    suspend fun getMe(): ApiResult<User>

    suspend fun updateProfile(
        email: String? = null,
        displayName: String? = null
    ): ApiResult<User>

    suspend fun deleteAccount(): ApiResult<Unit>
}
