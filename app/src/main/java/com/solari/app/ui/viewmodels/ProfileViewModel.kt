package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.auth.AuthSignInMethod
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.DeleteAccountVerification
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
    var isDeletingAccount by mutableStateOf(false)
        private set
    var isSignedInWithGoogle by mutableStateOf(false)
        private set

    var successMessage by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadMe()
        observeAuthSession()
    }

    private fun observeAuthSession() {
        viewModelScope.launch {
            authRepository.currentSession.collect { session ->
                isSignedInWithGoogle = session?.signInMethod == AuthSignInMethod.Google
            }
        }
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

    fun removeDisplayName() {
        viewModelScope.launch {
            try {
                when (val result = userRepository.updateProfile(removeDisplayName = true)) {
                    is ApiResult.Success -> {
                        user = result.data
                        successMessage = "Display name removed"
                        errorMessage = null
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to remove display name"
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

    fun removeAvatar(onSuccess: () -> Unit = {}) {
        if (isUpdatingAvatar) return

        viewModelScope.launch {
            isUpdatingAvatar = true
            try {
                when (val result = userRepository.updateProfile(removeAvatar = true)) {
                    is ApiResult.Success -> {
                        user = result.data
                        successMessage = "Avatar removed"
                        errorMessage = null
                        onSuccess()
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to remove avatar"
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
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit = {}
    ) {
        if (isDeletingAccount) return
        val trimmedPassword = password.trim()
        if (trimmedPassword.isBlank()) {
            val message = "Enter your password"
            errorMessage = message
            onFailure(message)
            return
        }

        viewModelScope.launch {
            isDeletingAccount = true
            errorMessage = null
            successMessage = null
            try {
                when (
                    val result = userRepository.deleteAccount(
                        DeleteAccountVerification.Password(trimmedPassword)
                    )
                ) {
                    is ApiResult.Success -> {
                        authRepository.clearSession()
                        onSuccess()
                    }

                    is ApiResult.Failure -> {
                        errorMessage = result.message
                        onFailure(result.message)
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to delete account"
                onFailure(errorMessage ?: "Failed to delete account")
            } finally {
                isDeletingAccount = false
            }
        }
    }

    fun deleteAccountWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit = {}
    ) {
        if (isDeletingAccount) return
        val normalizedIdToken = idToken.trim()
        if (normalizedIdToken.isBlank()) {
            val message = "Google verification did not return an ID token."
            errorMessage = message
            onFailure(message)
            return
        }

        viewModelScope.launch {
            isDeletingAccount = true
            errorMessage = null
            successMessage = null
            try {
                when (
                    val result = userRepository.deleteAccount(
                        DeleteAccountVerification.GoogleIdToken(normalizedIdToken)
                    )
                ) {
                    is ApiResult.Success -> {
                        authRepository.clearSession()
                        onSuccess()
                    }

                    is ApiResult.Failure -> {
                        errorMessage = result.message
                        onFailure(result.message)
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to delete account"
                onFailure(errorMessage ?: "Failed to delete account")
            } finally {
                isDeletingAccount = false
            }
        }
    }
}
