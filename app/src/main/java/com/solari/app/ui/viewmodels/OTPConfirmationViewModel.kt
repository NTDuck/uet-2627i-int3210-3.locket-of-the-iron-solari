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

class OTPConfirmationViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var otpCode by mutableStateOf("")
    var email by mutableStateOf("")
        private set
    var isSubmitting by mutableStateOf(false)
        private set
    var isResending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isVerified by mutableStateOf(false)
        private set

    fun applyEmail(value: String) {
        if (email != value) {
            email = value
            otpCode = ""
            errorMessage = null
            isVerified = false
        }
    }

    fun onOtpChanged(value: String) {
        otpCode = value.filter(Char::isDigit).take(6)
        errorMessage = null
    }

    fun verify() {
        val normalizedEmail = email.trim()
        val normalizedCode = otpCode.trim()
        when {
            normalizedEmail.isBlank() -> {
                errorMessage = "Email is required"
                return
            }

            normalizedCode.length != 6 -> {
                errorMessage = "Enter the 6-digit code"
                return
            }
        }
        if (isSubmitting) return

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            isVerified = false
            try {
                when (
                    val result = authRepository.verifyPasswordResetCode(
                        email = normalizedEmail,
                        code = normalizedCode
                    )
                ) {
                    is ApiResult.Success -> isVerified = true
                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to verify reset code"
            } finally {
                isSubmitting = false
            }
        }
    }

    fun resend() {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || isResending) return

        viewModelScope.launch {
            isResending = true
            errorMessage = null
            try {
                when (val result = authRepository.requestPasswordReset(normalizedEmail)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Failure -> errorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to resend reset code"
            } finally {
                isResending = false
            }
        }
    }

    fun consumeVerified() {
        isVerified = false
    }

    fun clearError() {
        errorMessage = null
    }
}
