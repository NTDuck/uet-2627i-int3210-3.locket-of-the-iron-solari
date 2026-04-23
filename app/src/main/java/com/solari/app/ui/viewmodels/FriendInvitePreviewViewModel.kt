package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.FriendRequestDirection
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

enum class FriendInviteRelationship {
    Self,
    None,
    PendingOutgoing,
    Friend,
    Blocked
}

data class FriendInviteFeedbackEvent(
    val sequence: Long,
    val message: String,
    val isSuccess: Boolean
)

data class FriendInvitePreviewState(
    val requestedUsername: String? = null,
    val user: User? = null,
    val relationship: FriendInviteRelationship = FriendInviteRelationship.None,
    val friendRequestId: String? = null,
    val isLoading: Boolean = false,
    val isMutating: Boolean = false,
    val errorMessage: String? = null
)

class FriendInvitePreviewViewModel(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var uiState by mutableStateOf(FriendInvitePreviewState())
        private set

    var feedbackEvent by mutableStateOf<FriendInviteFeedbackEvent?>(null)
        private set

    private var feedbackSequence = 0L

    fun show(username: String) {
        val normalizedUsername = username.trim()
        if (normalizedUsername.isEmpty()) return

        uiState = FriendInvitePreviewState(
            requestedUsername = normalizedUsername,
            isLoading = true
        )

        viewModelScope.launch {
            try {
                when (val meResult = userRepository.getMe()) {
                    is ApiResult.Success -> {
                        val currentUser = meResult.data
                        if (currentUser.username.equals(normalizedUsername, ignoreCase = true)) {
                            uiState = FriendInvitePreviewState(
                                requestedUsername = normalizedUsername,
                                user = currentUser,
                                relationship = FriendInviteRelationship.Self
                            )
                            return@launch
                        }
                    }

                    is ApiResult.Failure -> Unit
                }

                val friends = when (val result = friendRepository.getFriends()) {
                    is ApiResult.Success -> result.data
                    is ApiResult.Failure -> emptyList()
                }
                val blockedUsers = when (val result = userRepository.getBlockedUsers()) {
                    is ApiResult.Success -> result.data.map { it.user }
                    is ApiResult.Failure -> emptyList()
                }
                val outgoingRequests = when (val result = friendRepository.getFriendRequests()) {
                    is ApiResult.Success -> result.data.items.filter {
                        it.direction == FriendRequestDirection.Outgoing
                    }
                    is ApiResult.Failure -> emptyList()
                }

                val blockedUser = blockedUsers.findUsername(normalizedUsername)
                if (blockedUser != null) {
                    uiState = FriendInvitePreviewState(
                        requestedUsername = normalizedUsername,
                        user = blockedUser,
                        relationship = FriendInviteRelationship.Blocked
                    )
                    return@launch
                }

                val friend = friends.findUsername(normalizedUsername)
                if (friend != null) {
                    uiState = FriendInvitePreviewState(
                        requestedUsername = normalizedUsername,
                        user = friend,
                        relationship = FriendInviteRelationship.Friend
                    )
                    return@launch
                }

                val outgoingRequest = outgoingRequests.firstOrNull {
                    it.user.username.equals(normalizedUsername, ignoreCase = true)
                }
                if (outgoingRequest != null) {
                    uiState = FriendInvitePreviewState(
                        requestedUsername = normalizedUsername,
                        user = outgoingRequest.user,
                        relationship = FriendInviteRelationship.PendingOutgoing,
                        friendRequestId = outgoingRequest.id
                    )
                    return@launch
                }

                when (val profileResult = userRepository.getPublicProfile(normalizedUsername)) {
                    is ApiResult.Success -> {
                        uiState = FriendInvitePreviewState(
                            requestedUsername = normalizedUsername,
                            user = profileResult.data
                        )
                    }

                    is ApiResult.Failure -> {
                        uiState = FriendInvitePreviewState(
                            requestedUsername = normalizedUsername,
                            errorMessage = profileResult.message
                        )
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                uiState = FriendInvitePreviewState(
                    requestedUsername = normalizedUsername,
                    errorMessage = throwable.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun dismiss() {
        uiState = FriendInvitePreviewState()
    }

    fun clearFeedbackEvent() {
        feedbackEvent = null
    }

    fun sendFriendRequest() {
        val currentState = uiState
        val username = currentState.user?.username ?: currentState.requestedUsername ?: return

        mutateOptimistically(
            previousState = currentState,
            successMessage = "Friend request sent"
        ) { friendRepository.sendFriendRequest(username) }
    }

    fun unsendFriendRequest() {
        val currentState = uiState
        val requestId = currentState.friendRequestId ?: return

        mutateOptimistically(
            previousState = currentState,
            successMessage = "Friend request canceled"
        ) { friendRepository.deleteFriendRequest(requestId) }
    }

    fun unfriend() {
        val currentState = uiState
        val user = currentState.user ?: return

        mutateOptimistically(
            previousState = currentState,
            successMessage = "Unfriended ${user.displayName}"
        ) { friendRepository.unfriend(user.id) }
    }

    fun unblock() {
        val currentState = uiState
        val user = currentState.user ?: return

        mutateOptimistically(
            previousState = currentState,
            successMessage = "Unblocked ${user.displayName}"
        ) { userRepository.unblockUser(user.id) }
    }

    private fun mutateOptimistically(
        previousState: FriendInvitePreviewState,
        successMessage: String,
        request: suspend () -> ApiResult<Unit>
    ) {
        dismiss()

        viewModelScope.launch {
            try {
                when (val result = request()) {
                    is ApiResult.Success -> publishFeedback(successMessage, isSuccess = true)
                    is ApiResult.Failure -> {
                        uiState = previousState.copy(
                            errorMessage = result.message
                        )
                        publishFeedback(result.message, isSuccess = false)
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                val message = throwable.message ?: "Request failed"
                uiState = previousState.copy(
                    errorMessage = message
                )
                publishFeedback(message, isSuccess = false)
            }
        }
    }

    private fun publishFeedback(message: String, isSuccess: Boolean) {
        feedbackSequence += 1
        feedbackEvent = FriendInviteFeedbackEvent(
            sequence = feedbackSequence,
            message = message,
            isSuccess = isSuccess
        )
    }

    private fun List<User>.findUsername(username: String): User? {
        return firstOrNull { it.username.equals(username, ignoreCase = true) }
    }
}
