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
data class GetNicknamesResponseDto(
    val items: List<NicknameDto> = emptyList(),
    val nicknames: List<NicknameDto> = emptyList(),
    @SerialName("next_cursor")
    val nextCursor: String? = null,
    val limit: Int? = null
) {
    val effectiveItems: List<NicknameDto>
        get() = items.ifEmpty { nicknames }
}

@Serializable
data class NicknameDto(
    val id: String? = null,
    @SerialName("target_user_id")
    val targetUserId: String? = null,
    @SerialName("target_id")
    val targetId: String? = null,
    @SerialName("friend_id")
    val friendId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("target_user")
    val targetUser: ApiUserDto? = null,
    val user: ApiUserDto? = null,
    val nickname: String
) {
    val effectiveTargetUserId: String?
        get() = targetUserId ?: targetId ?: friendId ?: targetUser?.id ?: userId ?: user?.id
}

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
data class SendFriendRequestDto(
    val identifier: String
)

@Serializable
data class SetNicknameRequestDto(
    val nickname: String
)

@Serializable
data class UpdateNicknameRequestDto(
    @SerialName("new_nickname")
    val newNickname: String
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

@Serializable
data class FriendshipMutationResponseDto(
    val message: String
)

@Serializable
data class NicknameMutationResponseDto(
    val message: String
)
