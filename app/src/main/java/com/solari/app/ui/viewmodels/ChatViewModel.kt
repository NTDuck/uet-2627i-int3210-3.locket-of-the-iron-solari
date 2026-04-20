package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.data.ServiceLocator
import com.solari.app.ui.models.Conversation

class ChatViewModel : ViewModel() {
    var conversation by mutableStateOf<Conversation?>(null)
        private set
    
    var messageText by mutableStateOf("")

    fun loadConversation(chatId: String) {
        conversation = ServiceLocator.mockDataProvider.getConversation(chatId)
    }

    fun sendMessage(chatId: String) {
        if (messageText.isBlank()) return
        ServiceLocator.mockDataProvider.addMessage(chatId, messageText)
        messageText = ""
        loadConversation(chatId) // Refresh
    }
    
    fun unsendMessage(chatId: String, messageId: String) {
        // Mock unsend
        val conv = conversation ?: return
        val updatedMessages = conv.messages.filter { it.id != messageId }
        conversation = conv.copy(messages = updatedMessages)
        // In real app, update provider
    }
}
