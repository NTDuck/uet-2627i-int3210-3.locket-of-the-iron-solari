package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.ProfileAvatarUpload
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    var user by mutableStateOf<User?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isUpdatingAvatar by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadMe()
    }

    fun loadMe() {
        viewModelScope.launch {
            isLoading = true
            try {
                when (val result = userRepository.getMe()) {
                    is ApiResult.Success -> {
                        user = result.data
                        errorMessage = null
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to load profile"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            when (val result = userRepository.updateProfile(displayName = newName)) {
                is ApiResult.Success -> {
                    user = result.data
                    successMessage = "Display name updated successfully"
                    errorMessage = null
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            when (val result = userRepository.updateProfile(email = newEmail)) {
                is ApiResult.Success -> {
                    user = result.data
                    successMessage = "Email updated successfully"
                    errorMessage = null
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun updateAvatar(
        avatar: ProfileAvatarUpload,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        if (isUpdatingAvatar) return

        viewModelScope.launch {
            isUpdatingAvatar = true
            try {
                when (val result = userRepository.updateProfile(avatar = avatar)) {
                    is ApiResult.Success -> {
                        user = result.data
                        successMessage = "Avatar updated successfully"
                        errorMessage = null
                        onSuccess()
                    }

                    is ApiResult.Failure -> {
                        errorMessage = result.message
                        onFailure()
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to update avatar"
                onFailure()
            } finally {
                isUpdatingAvatar = false
            }
        }
    }

    fun clearMessages() {
        successMessage = null
        errorMessage = null
    }

    fun deleteAccount(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = userRepository.deleteAccount()) {
                is ApiResult.Success -> {
                    authRepository.clearSession()
                    onSuccess()
                }

                is ApiResult.Failure -> {
                    errorMessage = result.message
                    onFailure(result.message)
                }
            }
        }
    }
}
