package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.websocket.WebSocketEvent
import com.solari.app.data.websocket.WebSocketManager
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.FriendRequest
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val FriendRequestPageSize = 4
private const val CollapsedFriendRequestCount = 3

class ConversationViewModel(
    private val conversationRepository: ConversationRepository,
    private val friendRepository: FriendRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    var friendRequests by mutableStateOf<List<FriendRequest>>(emptyList())
        private set

    var isFriendRequestsExpanded by mutableStateOf(false)
        private set

    var isLoadingMoreFriendRequests by mutableStateOf(false)
        private set

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
        private set

    var isLoadingFriendRequests by mutableStateOf(false)
        private set

    var isLoadingConversations by mutableStateOf(false)
        private set

    val isLoading: Boolean
        get() = isLoadingFriendRequests || isLoadingConversations

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private var friendRequestsNextCursor by mutableStateOf<String?>(null)
    private var consumedClearedConversationIds: Set<String> = emptySet()

    val visibleFriendRequests: List<FriendRequest>
        get() = if (isFriendRequestsExpanded) {
            friendRequests
        } else {
            friendRequests.take(CollapsedFriendRequestCount)
        }

    val canViewMoreFriendRequests: Boolean
        get() = !isFriendRequestsExpanded && (friendRequests.size > CollapsedFriendRequestCount || friendRequestsNextCursor != null)

    val canViewLessFriendRequests: Boolean
        get() = isFriendRequestsExpanded && friendRequests.size > CollapsedFriendRequestCount

    init {
        refresh()

        viewModelScope.launch {
            webSocketManager.events.collect { event -> handleWebSocketEvent(event) }
        }

        viewModelScope.launch {
            conversationRepository.clearedConversationIds.collectLatest { clearedIds ->
                val newClearedIds = clearedIds - consumedClearedConversationIds
                if (newClearedIds.isNotEmpty()) {
                    conversations = conversations.filterNot { it.id in newClearedIds }
                    consumedClearedConversationIds += newClearedIds
                }
            }
        }
    }

    private fun handleWebSocketEvent(event: WebSocketEvent) {
        when (event) {
            is WebSocketEvent.NewMessage -> {
                val targetId = event.conversationId
                val existing = conversations.firstOrNull { it.id == targetId }
                if (existing != null) {
                    val isFromPartner = event.message.senderId == existing.otherUser.id
                    val updated = existing.copy(
                        lastMessage = event.message.text,
                        lastMessageSenderId = event.message.senderId,
                        isLastMessageDeleted = false,
                        timestamp = event.message.timestamp,
                        isUnread = isFromPartner
                    )
                    conversations = listOf(updated) + conversations.filter { it.id != targetId }
                } else {
                    refreshConversationsFromRealtime()
                }
            }

            is WebSocketEvent.MessageUnsent -> {
            }

            is WebSocketEvent.ConversationRead -> {
                // When the current user reads a conversation, clear unread highlight
                conversations = conversations.map { conversation ->
                    if (conversation.id == event.conversationId &&
                        conversation.isUnread &&
                        event.userId != conversation.otherUser.id
                    ) {
                        conversation.copy(isUnread = false)
                    } else {
                        conversation
                    }
                }
            }

            is WebSocketEvent.NewFriendRequest -> {
                refreshFriendRequestsFromRealtime()
            }

            is WebSocketEvent.FriendRequestAccepted -> {
                val acceptedRequest = friendRequests.firstOrNull { it.id == event.requestId }
                friendRequests = friendRequests.filterNot { it.id == event.requestId }
                checkAndResetExpandedState()
                acceptedRequest?.user?.id?.let { partnerId ->
                    updateConversationFriendshipStatus(partnerId = partnerId, isFriend = true)
                }
                refreshFriendRequestsFromRealtime()
                refreshConversationsFromRealtime()
            }

            is WebSocketEvent.FriendRequestRemoved -> {
                friendRequests = friendRequests.filterNot { it.id == event.requestId }
                checkAndResetExpandedState()
                refreshFriendRequestsFromRealtime()
            }

            is WebSocketEvent.FriendshipStatusChanged -> {
                updateConversationFriendshipStatus(
                    partnerId = event.partnerId,
                    isFriend = event.isFriend
                )
                if (!event.isFriend) {
                    friendRequests = friendRequests.filterNot { it.user.id == event.partnerId }
                    checkAndResetExpandedState()
                }
                refreshConversationsFromRealtime()
            }

            is WebSocketEvent.FriendProfileUpdated -> {
                conversations = conversations.map { conversation ->
                    if (conversation.otherUser.id == event.userId) {
                        conversation.copy(otherUser = conversation.otherUser.withProfileUpdate(event))
                    } else {
                        conversation
                    }
                }
                friendRequests = friendRequests.map { request ->
                    if (request.user.id == event.userId) {
                        request.copy(user = request.user.withProfileUpdate(event))
                    } else {
                        request
                    }
                }
            }

            // Reaction events don't affect the conversation list UI
            is WebSocketEvent.NewReaction,
            is WebSocketEvent.ReactionUpdated,
            is WebSocketEvent.ReactionRemoved,
            is WebSocketEvent.TypingIndicator,
            is WebSocketEvent.PostProcessed,
            is WebSocketEvent.PostFailed -> Unit
        }
    }

    private fun refreshFriendRequestsFromRealtime() {
        viewModelScope.launch {
            try {
                val limit = if (isFriendRequestsExpanded) {
                    friendRequests.size.coerceAtLeast(FriendRequestPageSize)
                } else {
                    FriendRequestPageSize
                }
                when (
                    val result = friendRepository.getFriendRequests(
                        limit = limit,
                        cursor = null
                    )
                ) {
                    is ApiResult.Success -> {
                        friendRequests = result.data.items
                        friendRequestsNextCursor = result.data.nextCursor
                        checkAndResetExpandedState()
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to refresh friend requests"
            }
        }
    }

    private fun refreshConversationsFromRealtime() {
        viewModelScope.launch {
            try {
                when (val result = conversationRepository.getConversations()) {
                    is ApiResult.Success -> conversations = result.data
                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to refresh conversations"
            }
        }
    }

    private fun updateConversationFriendshipStatus(partnerId: String, isFriend: Boolean) {
        conversations = conversations.map { conversation ->
            if (conversation.otherUser.id == partnerId) {
                conversation.copy(isReadOnly = !isFriend)
            } else {
                conversation
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            errorMessage = null

            val requestsJob = async {
                isLoadingFriendRequests = true
                isFriendRequestsExpanded = false
                try {
                    val limit = if (isFriendRequestsExpanded) {
                        friendRequests.size.coerceAtLeast(FriendRequestPageSize)
                    } else {
                        FriendRequestPageSize
                    }
                    when (
                        val requestsResult = friendRepository.getFriendRequests(
                            limit = limit,
                            cursor = null
                        )
                    ) {
                        is ApiResult.Success -> {
                            friendRequests = requestsResult.data.items
                            friendRequestsNextCursor = requestsResult.data.nextCursor
                        }

                        is ApiResult.Failure -> errorMessage = requestsResult.message
                    }
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    errorMessage = throwable.message ?: "Failed to load friend requests"
                } finally {
                    isLoadingFriendRequests = false
                }
            }

            val conversationsJob = async {
                isLoadingConversations = true
                try {
                    when (val conversationsResult = conversationRepository.getConversations()) {
                        is ApiResult.Success -> conversations = conversationsResult.data
                        is ApiResult.Failure -> if (errorMessage == null) errorMessage =
                            conversationsResult.message
                    }
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    errorMessage = throwable.message ?: "Failed to load conversations"
                } finally {
                    isLoadingConversations = false
                }
            }

            requestsJob.await()
            conversationsJob.await()
        }
    }

    fun expandFriendRequests() {
        if (isFriendRequestsExpanded) return
        isFriendRequestsExpanded = true
        loadMoreFriendRequests()
    }

    fun collapseFriendRequests() {
        isFriendRequestsExpanded = false
    }

    private fun loadMoreFriendRequests() {
        val cursor = friendRequestsNextCursor ?: return
        if (isLoadingMoreFriendRequests) return

        viewModelScope.launch {
            isLoadingMoreFriendRequests = true

            try {
                when (
                    val result = friendRepository.getFriendRequests(
                        limit = FriendRequestPageSize,
                        cursor = cursor
                    )
                ) {
                    is ApiResult.Success -> {
                        val existingIds = friendRequests.mapTo(mutableSetOf()) { it.id }
                        friendRequests =
                            friendRequests + result.data.items.filter { existingIds.add(it.id) }
                        friendRequestsNextCursor = result.data.nextCursor
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load friend requests"
            } finally {
                isLoadingMoreFriendRequests = false
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        val previousRequests = friendRequests
        if (previousRequests.none { it.id == requestId }) return
        friendRequests = previousRequests.filterNot { it.id == requestId }
        checkAndResetExpandedState()

        viewModelScope.launch {
            try {
                when (val result = friendRepository.acceptFriendRequest(requestId)) {
                    is ApiResult.Success -> {
                        successMessage = "Friend request accepted"
                        refreshFriendRequestsFromRealtime()
                        refreshConversationsFromRealtime()
                    }

                    is ApiResult.Failure -> {
                        friendRequests = previousRequests
                        errorMessage = result.message
                        checkAndResetExpandedState()
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                friendRequests = previousRequests
                errorMessage = throwable.message ?: "Failed to accept friend request"
                checkAndResetExpandedState()
            }
        }
    }

    fun declineFriendRequest(requestId: String) {
        removeFriendRequest(
            requestId = requestId,
            successMessage = "Friend request rejected",
            fallbackErrorMessage = "Failed to reject friend request"
        )
    }

    fun cancelFriendRequest(requestId: String) {
        removeFriendRequest(
            requestId = requestId,
            successMessage = "Friend request canceled",
            fallbackErrorMessage = "Failed to cancel friend request"
        )
    }

    private fun removeFriendRequest(
        requestId: String,
        successMessage: String,
        fallbackErrorMessage: String
    ) {
        val previousRequests = friendRequests
        if (previousRequests.none { it.id == requestId }) return
        friendRequests = previousRequests.filterNot { it.id == requestId }
        checkAndResetExpandedState()

        viewModelScope.launch {
            try {
                when (val result = friendRepository.deleteFriendRequest(requestId)) {
                    is ApiResult.Success -> this@ConversationViewModel.successMessage =
                        successMessage

                    is ApiResult.Failure -> {
                        friendRequests = previousRequests
                        errorMessage = result.message
                        checkAndResetExpandedState()
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                friendRequests = previousRequests
                errorMessage = throwable.message ?: fallbackErrorMessage
                checkAndResetExpandedState()
            }
        }
    }

    private fun checkAndResetExpandedState() {
        if (isFriendRequestsExpanded && friendRequests.size <= CollapsedFriendRequestCount) {
            isFriendRequestsExpanded = false
        }
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }

    // Optimistic update: clears unread highlight immediately without waiting for backend sync
    fun markConversationAsRead(conversationId: String) {
        conversations = conversations.map { conversation ->
            if (conversation.id == conversationId && conversation.isUnread) {
                conversation.copy(isUnread = false)
            } else {
                conversation
            }
        }
    }

    private fun User.withProfileUpdate(event: WebSocketEvent.FriendProfileUpdated): User {
        val updatedUsername = event.username ?: username
        val profileDisplayName = event.displayName ?: updatedUsername
        return copy(
            username = updatedUsername,
            displayName = nickname?.takeIf { it.isNotBlank() } ?: profileDisplayName,
            profileImageUrl = event.avatarUrl
        )
    }
}
