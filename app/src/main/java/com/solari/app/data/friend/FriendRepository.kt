package com.solari.app.data.friend

import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.FriendRequest
import com.solari.app.ui.models.User

interface FriendRepository {
    suspend fun getFriends(sort: String? = null): ApiResult<List<User>>

    suspend fun getFriendRequests(): ApiResult<List<FriendRequest>>

    suspend fun sendFriendRequest(identifier: String): ApiResult<Unit>

    suspend fun acceptFriendRequest(requestId: String): ApiResult<Unit>

    suspend fun deleteFriendRequest(requestId: String): ApiResult<Unit>
}
