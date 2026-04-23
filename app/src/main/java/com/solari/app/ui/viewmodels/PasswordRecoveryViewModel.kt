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

class PasswordRecoveryViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var email by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isCodeRequested by mutableStateOf(false)
        private set

    fun requestCode() {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank()) {
            errorMessage = "Enter your email"
            return
        }
        if (isSubmitting) return

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            isCodeRequested = false
            try {
                when (val result = authRepository.requestPasswordReset(normalizedEmail)) {
                    is ApiResult.Success -> {
                        email = normalizedEmail
                        isCodeRequested = true
                    }

                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to request password reset code"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun consumeCodeRequested() {
        isCodeRequested = false
    }

    fun clearError() {
        errorMessage = null
    }
}
