package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class PasswordResetViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    var oldPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    fun submit() {
        val oldPasswordValue = oldPassword.trim()
        val newPasswordValue = newPassword.trim()
        val confirmPasswordValue = confirmPassword.trim()

        when {
            oldPasswordValue.isBlank() -> {
                errorMessage = "Enter your current password"
                return
            }

            newPasswordValue.isBlank() -> {
                errorMessage = "Enter a new password"
                return
            }

            newPasswordValue != confirmPasswordValue -> {
                errorMessage = "Passwords do not match"
                return
            }
        }

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            successMessage = null

            try {
                when (val result =
                    userRepository.updatePassword(oldPasswordValue, newPasswordValue)) {
                    is ApiResult.Success -> {
                        oldPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        successMessage = "Password changed successfully"
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to change password"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearSuccess() {
        successMessage = null
    }
}
