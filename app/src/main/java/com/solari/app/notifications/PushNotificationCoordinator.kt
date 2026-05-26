package com.solari.app.notifications

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.PushNotificationStore
import com.solari.app.data.user.UserRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PushNotificationCoordinator(
    private val pushNotificationStore: PushNotificationStore,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend fun hasRequestedNotificationPermission(): Boolean {
        return pushNotificationStore.hasRequestedNotificationPermission()
    }

    suspend fun markNotificationPermissionRequested() {
        pushNotificationStore.markNotificationPermissionRequested()
    }

    suspend fun preparePushToken(): String? {
        val existingToken = pushNotificationStore.getCurrentDeviceToken()
        if (!existingToken.isNullOrBlank()) {
            return existingToken
        }

        return fetchAndStoreCurrentToken()
    }

    suspend fun fetchAndStoreCurrentToken(): String? {
        return runCatching {
            FirebaseMessaging.getInstance().token.await()
        }.onFailure { error ->
            Log.e(LOG_TAG, "Failed to fetch FCM token.", error)
        }.getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.also { token ->
                pushNotificationStore.saveCurrentDeviceToken(token)
            }
    }

    suspend fun registerStoredDeviceIfAuthenticated(): Boolean {
        if (authRepository.getCurrentSession() == null) {
            return false
        }

        val token = pushNotificationStore.getCurrentDeviceToken()
            ?: fetchAndStoreCurrentToken()
            ?: return false
        return registerDeviceToken(token)
    }

    suspend fun onNewToken(token: String) {
        val normalizedToken = token.trim()
        if (normalizedToken.isEmpty()) {
            return
        }

        pushNotificationStore.saveCurrentDeviceToken(normalizedToken)
        pushNotificationStore.clearRegisteredDeviceToken()
        registerStoredDeviceIfAuthenticated()
    }

    /**
     * Unregisters the current device from push notifications on the backend.
     * Returns true if the device was successfully unregistered, false otherwise.
     */
    suspend fun unregisterDevice(): Boolean {
        val token = pushNotificationStore.getCurrentDeviceToken() ?: return false

        return when (userRepository.unregisterDevice(token)) {
            is ApiResult.Success -> {
                pushNotificationStore.clearRegisteredDeviceToken()
                true
            }
            is ApiResult.Failure -> {
                Log.w(LOG_TAG, "Failed to unregister device.")
                false
            }
        }
    }

    /**
     * Checks whether the current device is registered for push notifications on the backend.
     * Returns true if registered, false if not registered or if the check fails.
     */
    suspend fun getDeviceNotificationStatus(): Boolean {
        val token = pushNotificationStore.getCurrentDeviceToken() ?: return false

        return when (val result = userRepository.getDeviceNotificationStatus(token)) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> false
        }
    }

    private suspend fun registerDeviceToken(token: String): Boolean {
        if (pushNotificationStore.getRegisteredDeviceToken() == token) {
            return true
        }

        return when (val result = userRepository.registerDevice(token, platform = ANDROID_PLATFORM)) {
            is ApiResult.Success -> {
                pushNotificationStore.markRegisteredDeviceToken(token)
                true
            }
            is ApiResult.Failure -> {
                Log.w(
                    LOG_TAG,
                    "Failed to register device. statusCode=${result.statusCode}, type=${result.type}, message=${result.message}"
                )
                false
            }
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                continuation.resumeWithException(
                    task.exception ?: IllegalStateException("Task failed")
                )
            }
        }
    }

    private companion object {
        const val LOG_TAG = "SolariPush"
        const val ANDROID_PLATFORM = "android"
    }
}
