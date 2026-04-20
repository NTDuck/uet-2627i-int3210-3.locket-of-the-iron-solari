package com.solari.app.data.remote.friend

import com.solari.app.data.remote.common.ApiUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFriendsResponseDto(
    val items: List<ApiUserDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null,
    val limit: Int? = null
)

@Serializable
data class FriendRequestDto(
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    val direction: String,
    val requester: ApiUserDto,
    val receiver: ApiUserDto
)

@Serializable
data class GetFriendRequestsResponseDto(
    val items: List<FriendRequestDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null,
    val limit: Int? = null,
    val direction: String? = null
)

@Serializable
data class FriendRequestMutationResponseDto(
    val message: String,
    @SerialName("friend_request")
    val friendRequest: FriendRequestRecordDto
)

@Serializable
data class FriendRequestRecordDto(
    val id: String,
    @SerialName("requester_id")
    val requesterId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class FriendRequestDeleteResponseDto(
    val message: String
)
