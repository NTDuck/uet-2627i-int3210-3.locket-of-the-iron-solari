package com.solari.app.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WelcomeFeedback(
    val sequence: Long,
    val message: String,
    val isSuccess: Boolean
)

data class WelcomeUiState(
    val isGoogleSignInLoading: Boolean = false,
    val isSignedInWithGoogle: Boolean = false,
    val feedback: WelcomeFeedback? = null
)

class WelcomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    private var feedbackSequence = 0L

    fun onGoogleCredentialRequestStarted(): Boolean {
        if (_uiState.value.isGoogleSignInLoading) return false

        _uiState.update {
            it.copy(
                isGoogleSignInLoading = true,
                feedback = null
            )
        }
        return true
    }

    fun onGoogleCredentialRequestFailed(message: String) {
        _uiState.update {
            it.copy(
                isGoogleSignInLoading = false,
                feedback = nextFeedback(
                    message = message,
                    isSuccess = false
                )
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        if (!_uiState.value.isGoogleSignInLoading) return

        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isGoogleSignInLoading = false,
                            isSignedInWithGoogle = true,
                            feedback = nextFeedback(
                                message = "Sign in with Google successfully",
                                isSuccess = true
                            )
                        )
                    }
                }

                is ApiResult.Failure -> {
                    Log.d(
                        GoogleSignInLogTag,
                        "Backend Google sign-in failed. statusCode=${result.statusCode}, type=${result.type}, message=${result.message}",
                        result.cause
                    )
                    _uiState.update {
                        it.copy(
                            isGoogleSignInLoading = false,
                            feedback = nextFeedback(
                                message = result.message,
                                isSuccess = false
                            )
                        )
                    }
                }
            }
        }
    }

    fun clearFeedback(sequence: Long) {
        _uiState.update { state ->
            if (state.feedback?.sequence == sequence) {
                state.copy(feedback = null)
            } else {
                state
            }
        }
    }

    fun consumeSignedInWithGoogle() {
        _uiState.update { it.copy(isSignedInWithGoogle = false) }
    }

    private fun nextFeedback(message: String, isSuccess: Boolean): WelcomeFeedback {
        feedbackSequence += 1
        return WelcomeFeedback(
            sequence = feedbackSequence,
            message = message,
            isSuccess = isSuccess
        )
    }

    private companion object {
        const val GoogleSignInLogTag = "SolariGoogleSignIn"
    }
}
