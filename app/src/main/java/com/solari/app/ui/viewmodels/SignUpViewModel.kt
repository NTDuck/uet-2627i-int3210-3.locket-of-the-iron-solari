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

class SignUpViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun createAccount(onSuccess: () -> Unit) {
        if (isSubmitting) return

        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim()

        if (password != confirmPassword) {
            errorMessage = "Passwords do not match."
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null

            try {
                when (val signUpResult = authRepository.signUp(normalizedUsername, normalizedEmail, password)) {
                    is ApiResult.Failure -> errorMessage = signUpResult.message
                    is ApiResult.Success -> {
                        when (val signInResult = authRepository.signIn(normalizedUsername, password)) {
                            is ApiResult.Failure -> errorMessage = signInResult.message
                            is ApiResult.Success -> onSuccess()
                        }
                    }
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                errorMessage = throwable.message ?: "Failed to create account."
            } finally {
                isSubmitting = false
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
    }
}
