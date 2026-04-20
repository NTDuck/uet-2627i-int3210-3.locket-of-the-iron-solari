package com.solari.app.data.user

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.BlockedUser
import com.solari.app.ui.models.User

interface UserRepository {
    suspend fun getMe(): ApiResult<User>

    suspend fun updateProfile(
        email: String? = null,
        displayName: String? = null
    ): ApiResult<User>

    suspend fun blockUser(userId: String): ApiResult<Unit>

    suspend fun unblockUser(userId: String): ApiResult<Unit>

    suspend fun getBlockedUsers(sort: String = "newest"): ApiResult<List<BlockedUser>>

    suspend fun deleteAccount(): ApiResult<Unit>
}
