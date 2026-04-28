package com.solari.app.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthSessionInvalidationEvent(
    val message: String = "Your session has expired. Please sign in again."
)

class AuthSessionInvalidationNotifier {
    private val _events = MutableStateFlow<AuthSessionInvalidationEvent?>(null)
    val events: StateFlow<AuthSessionInvalidationEvent?> = _events.asStateFlow()

    fun notifySessionInvalidated(
        message: String = "Your session has expired. Please sign in again."
    ) {
        _events.value = AuthSessionInvalidationEvent(message = message)
    }

    fun clear() {
        _events.value = null
    }
}
