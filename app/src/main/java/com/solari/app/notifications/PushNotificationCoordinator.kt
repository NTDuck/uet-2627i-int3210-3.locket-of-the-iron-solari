package com.solari.app.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.Task
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.PushNotificationStore
import com.solari.app.data.user.UserRepository
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class PushNotificationCoordinator(
    context: Context,
    private val pushNotificationStore: PushNotificationStore,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    private val applicationContext = context.applicationContext

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
            Log.e(LogTag, "Failed to fetch FCM token.", error)
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

    suspend fun getCurrentDeviceToken(): String? {
        return pushNotificationStore.getCurrentDeviceToken()
    }

    suspend fun markDeviceUnregistered() {
        pushNotificationStore.clearRegisteredDeviceToken()
    }

    private suspend fun registerDeviceToken(token: String) {
        if (pushNotificationStore.getRegisteredDeviceToken() == token) {
            return
        }

        when (val result = userRepository.registerDevice(token, platform = AndroidPlatform)) {
            is ApiResult.Success -> pushNotificationStore.markRegisteredDeviceToken(token)
            is ApiResult.Failure -> {
                Log.w(
                    LogTag,
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
                continuation.resumeWithException(task.exception ?: IllegalStateException("Task failed"))
            }
        }
    }

    private companion object {
        const val LogTag = "SolariPush"
        const val AndroidPlatform = "android"
    }
}
