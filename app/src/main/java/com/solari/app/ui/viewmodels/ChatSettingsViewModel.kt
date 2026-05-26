package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ChatSettingsViewModel(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository
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

    var isNicknameMutating by mutableStateOf(false)
        private set

    private var partnerProfileDisplayName: String? = null

    fun setInitialPartner(initialPartner: User?) {
        if (partner == null) {
            partner = initialPartner
            username = initialPartner?.username
            if (initialPartner?.nickname.isNullOrBlank()) {
                partnerProfileDisplayName = initialPartner?.displayName
            }
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

            try {
                when (val result = conversationRepository.getConversation(chatId)) {
                    is ApiResult.Success -> {
                        applyConversation(result.data)
                    }

                    is ApiResult.Failure -> {
                        errorMessage = result.message
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load chat settings"
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

    fun setNickname(chatId: String, nickname: String) {
        submitNicknameMutation(
            chatId = chatId,
            nickname = nickname,
            successMessage = "Nickname set"
        ) { partnerId, trimmedNickname ->
            friendRepository.setNickname(partnerId, trimmedNickname)
        }
    }

    fun updateNickname(chatId: String, nickname: String) {
        submitNicknameMutation(
            chatId = chatId,
            nickname = nickname,
            successMessage = "Nickname updated"
        ) { partnerId, trimmedNickname ->
            friendRepository.updateNickname(partnerId, trimmedNickname)
        }
    }

    fun removeNickname(chatId: String) {
        val targetPartner = partner ?: run {
            errorMessage = "Missing chat partner"
            return
        }
        if (isNicknameMutating) return

        successMessage = null
        errorMessage = null

        viewModelScope.launch {
            isNicknameMutating = true
            try {
                when (val result = friendRepository.removeNickname(targetPartner.id)) {
                    is ApiResult.Success -> {
                        partner = targetPartner.copy(
                            displayName = partnerProfileDisplayName
                                ?: targetPartner.username,
                            nickname = null
                        )
                        refreshConversation(chatId)
                        successMessage = "Nickname removed"
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to remove nickname"
            } finally {
                isNicknameMutating = false
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    @Suppress("SameParameterValue")
    private fun submitNicknameMutation(
        chatId: String,
        nickname: String,
        successMessage: String,
        request: suspend (partnerId: String, nickname: String) -> ApiResult<Unit>
    ) {
        val targetPartner = partner ?: run {
            errorMessage = "Missing chat partner"
            return
        }
        if (isNicknameMutating) return

        val trimmedNickname = nickname.trim()
        if (trimmedNickname.isBlank()) {
            return
        }

        this.successMessage = null
        errorMessage = null

        viewModelScope.launch {
            isNicknameMutating = true
            try {
                when (val result = request(targetPartner.id, trimmedNickname)) {
                    is ApiResult.Success -> {
                        partner = targetPartner.copy(
                            displayName = trimmedNickname,
                            nickname = trimmedNickname
                        )
                        refreshConversation(chatId)
                        this@ChatSettingsViewModel.successMessage = successMessage
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to update nickname"
            } finally {
                isNicknameMutating = false
            }
        }
    }

    private suspend fun refreshConversation(chatId: String) {
        if (chatId.isBlank()) return
        when (val result = conversationRepository.getConversation(chatId)) {
            is ApiResult.Success -> applyConversation(result.data)
            is ApiResult.Failure -> errorMessage = result.message
        }
    }

    private suspend fun applyConversation(conversation: Conversation) {
        val rawPartner = conversation.otherUser
        val nickname = if (conversation.isReadOnly) {
            null
        } else {
            when (val result = friendRepository.getNicknames()) {
                is ApiResult.Success -> result.data[rawPartner.id]?.takeIf { it.isNotBlank() }
                    ?: rawPartner.nickname?.takeIf { it.isNotBlank() }

                is ApiResult.Failure -> {
                    if (errorMessage == null) {
                        errorMessage = result.message
                    }
                    rawPartner.nickname?.takeIf { it.isNotBlank() }
                }
            }
        }

        partnerProfileDisplayName = rawPartner.displayName
        partner = if (nickname == null || conversation.isReadOnly) {
            rawPartner.copy(nickname = null)
        } else {
            rawPartner.copy(
                displayName = nickname,
                nickname = nickname
            )
        }
        username = rawPartner.username
        isMuted = conversation.isMuted
        isReadOnly = conversation.isReadOnly
    }
}
