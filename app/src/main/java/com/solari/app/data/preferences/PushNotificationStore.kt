package com.solari.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.pushNotificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "solari_push_notifications"
)

class PushNotificationStore(context: Context) {
    private val dataStore = context.applicationContext.pushNotificationDataStore

    suspend fun hasRequestedNotificationPermission(): Boolean {
        return dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences -> preferences[NotificationPermissionRequestedKey] ?: false }
            .first()
    }

    suspend fun markNotificationPermissionRequested() {
        dataStore.edit { preferences ->
            preferences[NotificationPermissionRequestedKey] = true
        }
    }

    suspend fun getCurrentDeviceToken(): String? {
        return dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences -> preferences[CurrentDeviceTokenKey] }
            .first()
            ?.takeIf { it.isNotBlank() }
    }

    suspend fun saveCurrentDeviceToken(token: String) {
        dataStore.edit { preferences ->
            preferences[CurrentDeviceTokenKey] = token
        }
    }

    suspend fun getRegisteredDeviceToken(): String? {
        return dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }
            .map { preferences -> preferences[RegisteredDeviceTokenKey] }
            .first()
            ?.takeIf { it.isNotBlank() }
    }

    suspend fun markRegisteredDeviceToken(token: String) {
        dataStore.edit { preferences ->
            preferences[RegisteredDeviceTokenKey] = token
        }
    }

    suspend fun clearRegisteredDeviceToken() {
        dataStore.edit { preferences ->
            preferences.remove(RegisteredDeviceTokenKey)
        }
    }

    private companion object {
        val NotificationPermissionRequestedKey =
            booleanPreferencesKey("notification_permission_requested")
        val CurrentDeviceTokenKey = stringPreferencesKey("current_device_token")
        val RegisteredDeviceTokenKey = stringPreferencesKey("registered_device_token")
    }
}
