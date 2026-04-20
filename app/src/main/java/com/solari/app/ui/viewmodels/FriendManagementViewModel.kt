package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class FriendManagementViewModel(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSendingRequest by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadFriends()
    }

    fun loadFriends(sort: String? = null) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                when (val result = friendRepository.getFriends(sort = sort)) {
                    is ApiResult.Success -> friends = result.data
                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load friends"
            } finally {
                isLoading = false
            }
        }
    }

    fun sendFriendRequest(
        identifier: String,
        onSuccess: () -> Unit
    ) {
        val trimmedIdentifier = identifier.trim()
        if (trimmedIdentifier.isBlank()) {
            errorMessage = "Enter a username or email"
            return
        }

        viewModelScope.launch {
            isSendingRequest = true
            errorMessage = null
            successMessage = null

            try {
                when (val result = friendRepository.sendFriendRequest(trimmedIdentifier)) {
                    is ApiResult.Success -> {
                        successMessage = "Friend request sent"
                        onSuccess()
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to send friend request"
            } finally {
                isSendingRequest = false
            }
        }
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }

    fun blockFriend(friend: User) {
        val previousFriends = friends
        friends = friends.filterNot { it.id == friend.id }

        viewModelScope.launch {
            when (val result = userRepository.blockUser(friend.id)) {
                is ApiResult.Success -> successMessage = "${friend.displayName} blocked"
                is ApiResult.Failure -> {
                    friends = previousFriends
                    errorMessage = result.message
                }
            }
        }
    }
}
