package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.data.ServiceLocator
import com.solari.app.ui.models.User

class ProfileViewModel : ViewModel() {
    var user by mutableStateOf(ServiceLocator.mockDataProvider.currentUser)
        private set

    var successMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    fun updateDisplayName(newName: String) {
        user = user.copy(displayName = newName)
        ServiceLocator.mockDataProvider.currentUser = user
    }

    fun updateUsername(newUsername: String): Boolean {
        if (newUsername == user.username) return true
        val exists = ServiceLocator.mockDataProvider.users.any { it.username == newUsername && it.id != user.id }
        if (exists) {
            errorMessage = "Username already exists"
            return false
        }
        user = user.copy(username = newUsername)
        ServiceLocator.mockDataProvider.currentUser = user
        successMessage = "Username updated successfully"
        errorMessage = null
        return true
    }

    fun updateEmail(newEmail: String): Boolean {
        if (newEmail == user.email) return true
        val exists = ServiceLocator.mockDataProvider.users.any { it.email == newEmail && it.id != user.id }
        if (exists) {
            errorMessage = "Email already exists"
            return false
        }
        user = user.copy(email = newEmail)
        ServiceLocator.mockDataProvider.currentUser = user
        successMessage = "Email updated successfully"
        errorMessage = null
        return true
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }

    fun deleteAccount(password: String): Boolean {
        // Mock password check
        if (password == "password") {
            ServiceLocator.mockDataProvider.deleteAccount()
            return true
        }
        return false
    }
}
