package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    var conversation by mutableStateOf<Conversation?>(null)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set
    
    var messageText by mutableStateOf("")

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadConversation(chatId: String) {
        viewModelScope.launch {
            when (val userResult = userRepository.getMe()) {
                is ApiResult.Success -> currentUser = userResult.data
                is ApiResult.Failure -> errorMessage = userResult.message
            }

            val conversationResult = conversationRepository.getConversation(chatId)
            val messagesResult = conversationRepository.getMessages(chatId)

            when {
                conversationResult is ApiResult.Success && messagesResult is ApiResult.Success -> {
                    conversation = conversationResult.data.copy(messages = messagesResult.data)
                    errorMessage = null
                }

                conversationResult is ApiResult.Failure -> errorMessage = conversationResult.message
                messagesResult is ApiResult.Failure -> errorMessage = messagesResult.message
            }
        }
    }

    fun sendMessage(chatId: String) {
        val content = messageText.trim()
        if (content.isBlank()) return

        viewModelScope.launch {
            when (val result = conversationRepository.sendMessage(chatId, content)) {
                is ApiResult.Success -> {
                    messageText = ""
                    val currentConversation = conversation
                    if (currentConversation != null) {
                        conversation = currentConversation.copy(
                            messages = currentConversation.messages + result.data,
                            lastMessage = result.data.text,
                            timestamp = result.data.timestamp
                        )
                    } else {
                        loadConversation(chatId)
                    }
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }
    
    fun unsendMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            when (val result = conversationRepository.unsendMessage(chatId, messageId)) {
                is ApiResult.Success -> {
                    val conv = conversation ?: return@launch
                    conversation = conv.copy(messages = conv.messages.filter { it.id != messageId })
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }
}
