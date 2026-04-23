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

data class AppAuthUiState(
    val isCheckingSession: Boolean = true,
    val isAuthenticated: Boolean = false
)

class AppAuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppAuthUiState())
    val uiState: StateFlow<AppAuthUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        viewModelScope.launch {
            when (authRepository.restoreSession()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCheckingSession = false,
                            isAuthenticated = true
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isCheckingSession = false,
                            isAuthenticated = false
                        )
                    }
                }
            }
        }
    }

    fun onSignedIn() {
        _uiState.update { it.copy(isAuthenticated = true) }
    }

    fun signOutLocal() {
        viewModelScope.launch {
            authRepository.clearSession()
            _uiState.update {
                it.copy(
                    isCheckingSession = false,
                    isAuthenticated = false
                )
            }
        }
    }
}
