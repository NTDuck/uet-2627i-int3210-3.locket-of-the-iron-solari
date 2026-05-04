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

    suspend fun registerStoredDeviceIfAuthenticated() {
        if (authRepository.getCurrentSession() == null) {
            return
        }

        val token = pushNotificationStore.getCurrentDeviceToken()
            ?: fetchAndStoreCurrentToken()
            ?: return
        registerDeviceToken(token)
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

    private suspend fun registerDeviceToken(token: String) {
        if (pushNotificationStore.getRegisteredDeviceToken() == token) {
            return
        }

        when (val result = userRepository.registerDevice(token, platform = ANDROID_PLATFORM)) {
            is ApiResult.Success -> pushNotificationStore.markRegisteredDeviceToken(token)
            is ApiResult.Failure -> {
                Log.w(
                    LOG_TAG,
                    "Failed to register device. statusCode=${result.statusCode}, type=${result.type}, message=${result.message}"
                )
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
