package com.solari.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val emailOrUsername: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedIn: Boolean = false
)

class SignInViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun onEmailOrUsernameChanged(value: String) {
        _uiState.update {
            it.copy(
                emailOrUsername = value,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null
            )
        }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            when (
                val result = authRepository.signIn(
                    identifier = state.emailOrUsername,
                    password = state.password
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignedIn = true,
                            password = ""
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun consumeSignedIn() {
        _uiState.update { it.copy(isSignedIn = false) }
    }
}
