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
    var currentUser by mutableStateOf<User?>(null)
        private set

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

    private var currentSort: String? = null

    init {
        loadCurrentUser()
        loadFriends()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                when (val result = userRepository.getMe()) {
                    is ApiResult.Success -> currentUser = result.data
                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load profile"
            }
        }
    }

    fun loadFriends(sort: String? = null) {
        currentSort = sort
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val friendsResult = friendRepository.getFriends(sort = sort)
                val nicknamesResult = friendRepository.getNicknames()

                when (friendsResult) {
                    is ApiResult.Success -> {
                        val nicknamesByUserId = when (nicknamesResult) {
                            is ApiResult.Success -> nicknamesResult.data
                            is ApiResult.Failure -> emptyMap()
                        }
                        friends = friendsResult.data.map { friend ->
                            val nickname = nicknamesByUserId[friend.id]
                            if (nickname.isNullOrBlank()) {
                                friend
                            } else {
                                friend.copy(
                                    displayName = nickname,
                                    nickname = nickname
                                )
                            }
                        }
                        if (nicknamesResult is ApiResult.Failure && errorMessage == null) {
                            errorMessage = nicknamesResult.message
                        }
                    }

                    is ApiResult.Failure -> errorMessage = friendsResult.message
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

    fun unfriend(friend: User) {
        val previousFriends = friends
        friends = friends.filterNot { it.id == friend.id }
        successMessage = null
        errorMessage = null

        viewModelScope.launch {
            try {
                when (val result = friendRepository.unfriend(friend.id)) {
                    is ApiResult.Success -> successMessage = "Unfriended ${friend.displayName}"
                    is ApiResult.Failure -> {
                        friends = previousFriends
                        errorMessage = result.message
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                friends = previousFriends
                errorMessage = throwable.message ?: "Failed to unfriend ${friend.displayName}"
            }
        }
    }

    fun setNickname(friend: User, nickname: String) {
        submitNicknameMutation(
            friend = friend,
            nickname = nickname,
            emptyMessage = "Enter a nickname",
            successMessage = "Nickname set"
        ) { trimmedNickname ->
            friendRepository.setNickname(friend.id, trimmedNickname)
        }
    }

    fun updateNickname(friend: User, nickname: String) {
        submitNicknameMutation(
            friend = friend,
            nickname = nickname,
            emptyMessage = "Enter a nickname",
            successMessage = "Nickname updated"
        ) { trimmedNickname ->
            friendRepository.updateNickname(friend.id, trimmedNickname)
        }
    }

    fun removeNickname(friend: User) {
        successMessage = null
        errorMessage = null

        viewModelScope.launch {
            try {
                when (val result = friendRepository.removeNickname(friend.id)) {
                    is ApiResult.Success -> {
                        successMessage = "Nickname removed"
                        loadFriends(currentSort)
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to remove nickname"
            }
        }
    }

    private fun submitNicknameMutation(
        friend: User,
        nickname: String,
        emptyMessage: String,
        successMessage: String,
        request: suspend (String) -> ApiResult<Unit>
    ) {
        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isBlank()) {
            errorMessage = emptyMessage
            return
        }

        this.successMessage = null
        errorMessage = null

        viewModelScope.launch {
            try {
                when (val result = request(trimmedNickname)) {
                    is ApiResult.Success -> {
                        this@FriendManagementViewModel.successMessage = successMessage
                        loadFriends(currentSort)
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to update nickname"
            }
        }
    }
}
