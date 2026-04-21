package com.solari.app.data.friend

import com.solari.app.data.mappers.toEpochMillisOrNow
import com.solari.app.data.mappers.toUiUser
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.friend.FriendApi
import com.solari.app.data.remote.friend.FriendRequestDto
import com.solari.app.data.remote.friend.SendFriendRequestDto
import com.solari.app.ui.models.FriendRequest
import com.solari.app.ui.models.FriendRequestDirection
import com.solari.app.ui.models.User

class DefaultFriendRepository(
    private val friendApi: FriendApi,
    private val apiExecutor: ApiExecutor
) : FriendRepository {
    override suspend fun getFriends(sort: String?): ApiResult<List<User>> {
        return when (val result = apiExecutor.execute { friendApi.getFriends(sort = sort) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(result.data.items.map { it.toUiUser() })
        }
    }

    override suspend fun getFriendRequests(
        limit: Int,
        cursor: String?
    ): ApiResult<FriendRequestPage> {
        return when (
            val result = apiExecutor.execute {
                friendApi.getFriendRequests(
                    limit = limit,
                    cursor = cursor
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                ApiResult.Success(
                    FriendRequestPage(
                        items = result.data.items.map { it.toUiFriendRequest() },
                        nextCursor = result.data.nextCursor
                    )
                )
            }
        }
    }

    override suspend fun sendFriendRequest(identifier: String): ApiResult<Unit> {
        return when (
            val result = apiExecutor.execute {
                friendApi.sendFriendRequest(SendFriendRequestDto(identifier = identifier))
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { friendApi.acceptFriendRequest(requestId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun deleteFriendRequest(requestId: String): ApiResult<Unit> {
        return when (val result = apiExecutor.execute { friendApi.deleteFriendRequest(requestId) }) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    private fun FriendRequestDto.toUiFriendRequest(): FriendRequest {
        val counterparty = if (direction == "incoming") requester else receiver
        val requestDirection = if (direction == "outgoing") {
            FriendRequestDirection.Outgoing
        } else {
            FriendRequestDirection.Incoming
        }
        return FriendRequest(
            id = id,
            user = counterparty.toUiUser(),
            direction = requestDirection,
            timestamp = createdAt.toEpochMillisOrNow()
        )
    }
}
