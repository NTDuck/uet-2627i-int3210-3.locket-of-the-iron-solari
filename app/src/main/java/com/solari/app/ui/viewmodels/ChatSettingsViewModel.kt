package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class ChatSettingsViewModel(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var partner by mutableStateOf<User?>(null)
        private set

    var username by mutableStateOf<String?>(null)
        private set

    var isMuted by mutableStateOf<Boolean?>(null)
        private set
    var isReadOnly by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun setInitialPartner(initialPartner: User?) {
        if (partner == null) {
            partner = initialPartner
        }
    }

    fun loadSettings(chatId: String) {
        if (chatId.isBlank()) {
            errorMessage = "Missing chat id"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            when (val result = conversationRepository.getConversation(chatId)) {
                is ApiResult.Success -> {
                    val conversation = result.data
                    partner = conversation.otherUser
                    username = conversation.otherUser.username
                    isMuted = conversation.isMuted
                    isReadOnly = conversation.isReadOnly
                }

                is ApiResult.Failure -> {
                    errorMessage = result.message
                }
            }

            isLoading = false
        }
    }

    fun toggleMute(chatId: String) {
        if (chatId.isBlank()) return
        val previousMuted = isMuted ?: return
        isMuted = !previousMuted

        viewModelScope.launch {
            when (val result = conversationRepository.toggleConversationMute(chatId)) {
                is ApiResult.Success -> {
                    isMuted = result.data
                }

                is ApiResult.Failure -> {
                    isMuted = previousMuted
                    errorMessage = result.message
                }
            }
        }
    }

    fun clearChatHistory(chatId: String, onCleared: () -> Unit = {}) {
        if (chatId.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val result = conversationRepository.clearConversationHistory(chatId)) {
                is ApiResult.Success -> {
                    successMessage = "Chat history cleared"
                    onCleared()
                }
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun blockPartner(onBlocked: () -> Unit = {}) {
        val partnerId = partner?.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            when (val result = userRepository.blockUser(partnerId)) {
                is ApiResult.Success -> {
                    successMessage = "User blocked"
                    onBlocked()
                }
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
