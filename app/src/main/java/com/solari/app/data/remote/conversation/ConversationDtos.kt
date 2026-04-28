package com.solari.app.data.remote.conversation

import com.solari.app.data.remote.common.ApiUserDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateConversationRequestDto(
    @SerialName("target_user_id")
    val targetUserId: String
)

@Serializable
data class CreateConversationResponseDto(
    val message: String? = null,
    val conversation: CreatedConversationDto
)

@Serializable
data class CreatedConversationDto(
    val id: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class GetConversationsResponseDto(
    val items: List<ConversationDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null
)

@Serializable
data class GetConversationResponseDto(
    val conversation: ConversationDto
)

@Serializable
data class ConversationDto(
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    val partner: ApiUserDto,
    @SerialName("last_message")
    val lastMessage: ConversationLastMessageDto? = null,
    @SerialName("current_user_last_read_at")
    val currentUserLastReadAt: String? = null,
    @SerialName("partner_last_read_at")
    val partnerLastReadAt: String? = null,
    @SerialName("is_readonly")
    val legacyIsReadOnly: Boolean = false,
    @SerialName("is_read_only")
    val isReadOnly: Boolean = false,
    @SerialName("is_muted")
    val isMuted: Boolean = false
)

@Serializable
data class ConversationLastMessageDto(
    val id: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class GetMessagesResponseDto(
    val items: List<MessageDto>,
    @SerialName("next_cursor")
    val nextCursor: String? = null,
    @SerialName("partner_last_read_at")
    val partnerLastReadAt: String? = null
)

@Serializable
data class GetMessageResponseDto(
    val message: MessageDto
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String? = null,
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("referenced_post_id")
    val referencedPostId: String? = null,
    @SerialName("replied_message_id")
    val repliedMessageId: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    val reactions: List<MessageReactionDto> = emptyList()
)

@Serializable
data class MessageReactionDto(
    @SerialName("user_id")
    val userId: String,
    val emoji: String
)

@Serializable
data class MessageReactionRequestDto(
    val emoji: String
)

@Serializable
data class MessageReactionResponseDto(
    val message: String,
    val data: MessageReactionRecordDto
)

@Serializable
data class MessageReactionRecordDto(
    val id: String,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("user_id")
    val userId: String,
    val emoji: String,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class SendMessageRequestDto(
    val content: String,
    @SerialName("referenced_post_id")
    val referencedPostId: String? = null,
    @SerialName("replied_message_id")
    val repliedMessageId: String? = null
)

@Serializable
data class SendMessageResponseDto(
    val message: String,
    val data: SentMessageDto
)

@Serializable
data class SentMessageDto(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    val content: String,
    @SerialName("referenced_post_id")
    val referencedPostId: String? = null,
    @SerialName("replied_message_id")
    val repliedMessageId: String? = null,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class UnsendMessageResponseDto(
    val message: String,
    val data: UnsentMessageDto
)

@Serializable
data class UnsentMessageDto(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("is_deleted")
    val isDeleted: Boolean
)

@Serializable
data class ToggleConversationMuteRequestDto(
    @SerialName("conversation_id")
    val conversationId: String
)

@Serializable
data class ToggleConversationMuteResponseDto(
    val message: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("is_muted")
    val isMuted: Boolean
)
