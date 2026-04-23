package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CompletePasswordResetViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
        private set
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set

    fun applyEmail(value: String) {
        if (email != value) {
            email = value
            errorMessage = null
            successMessage = null
        }
    }

    fun submit() {
        val normalizedEmail = email.trim()
        val newPasswordValue = newPassword.trim()
        val confirmPasswordValue = confirmPassword.trim()

        when {
            normalizedEmail.isBlank() -> {
                errorMessage = "Email is required"
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
        if (isSubmitting) return

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            successMessage = null
            try {
                when (
                    val result = authRepository.completePasswordReset(
                        email = normalizedEmail,
                        newPassword = newPasswordValue
                    )
                ) {
                    is ApiResult.Success -> {
                        newPassword = ""
                        confirmPassword = ""
                        successMessage = "Password reset successfully"
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to reset password"
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
