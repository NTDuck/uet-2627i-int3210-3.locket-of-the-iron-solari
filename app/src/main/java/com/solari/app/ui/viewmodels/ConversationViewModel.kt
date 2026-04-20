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
import kotlinx.coroutines.launch

class ConversationViewModel(
    private val conversationRepository: ConversationRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {
    var friendRequests by mutableStateOf<List<FriendRequest>>(emptyList())
        private set

    var conversations by mutableStateOf<List<Conversation>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val requestsResult = friendRepository.getFriendRequests()) {
                is ApiResult.Success -> friendRequests = requestsResult.data
                is ApiResult.Failure -> errorMessage = requestsResult.message
            }

            when (val conversationsResult = conversationRepository.getConversations()) {
                is ApiResult.Success -> conversations = conversationsResult.data
                is ApiResult.Failure -> if (errorMessage == null) errorMessage = conversationsResult.message
            }

            isLoading = false
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = friendRepository.acceptFriendRequest(requestId)) {
                is ApiResult.Success -> {
                    friendRequests = friendRequests.filter { it.id != requestId }
                    refresh()
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun declineFriendRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = friendRepository.deleteFriendRequest(requestId)) {
                is ApiResult.Success -> friendRequests = friendRequests.filter { it.id != requestId }
                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }
}
