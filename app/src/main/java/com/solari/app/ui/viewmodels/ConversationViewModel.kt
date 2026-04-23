package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.FriendRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val FriendRequestPageSize = 4
private const val CollapsedFriendRequestCount = 3

class ConversationViewModel(
    private val conversationRepository: ConversationRepository,
    private val friendRepository: FriendRepository
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

    val visibleFriendRequests: List<FriendRequest>
        get() = if (isFriendRequestsExpanded) {
            friendRequests
        } else {
            friendRequests.take(CollapsedFriendRequestCount)
        }

    val canViewMoreFriendRequests: Boolean
        get() = !isFriendRequestsExpanded &&
                (friendRequests.size > CollapsedFriendRequestCount || friendRequestsNextCursor != null)

    val canViewLessFriendRequests: Boolean
        get() = isFriendRequestsExpanded && friendRequests.size > CollapsedFriendRequestCount

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            errorMessage = null

            val requestsJob = async {
                isLoadingFriendRequests = true
                try {
                    when (
                        val requestsResult = friendRepository.getFriendRequests(
                            limit = FriendRequestPageSize,
                            cursor = null
                        )
                    ) {
                        is ApiResult.Success -> {
                            friendRequests = requestsResult.data.items
                            friendRequestsNextCursor = requestsResult.data.nextCursor
                            isFriendRequestsExpanded = false
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
                        is ApiResult.Failure -> if (errorMessage == null) errorMessage = conversationsResult.message
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
                        friendRequests = friendRequests + result.data.items.filter { existingIds.add(it.id) }
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

        viewModelScope.launch {
            try {
                when (val result = friendRepository.acceptFriendRequest(requestId)) {
                    is ApiResult.Success -> {
                        successMessage = "Friend request accepted"
                        refresh()
                    }

                    is ApiResult.Failure -> {
                        friendRequests = previousRequests
                        errorMessage = result.message
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                friendRequests = previousRequests
                errorMessage = throwable.message ?: "Failed to accept friend request"
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

        viewModelScope.launch {
            try {
                when (val result = friendRepository.deleteFriendRequest(requestId)) {
                    is ApiResult.Success -> this@ConversationViewModel.successMessage = successMessage
                    is ApiResult.Failure -> {
                        friendRequests = previousRequests
                        errorMessage = result.message
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                friendRequests = previousRequests
                errorMessage = throwable.message ?: fallbackErrorMessage
            }
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
}
