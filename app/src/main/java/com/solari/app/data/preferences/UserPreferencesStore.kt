package com.solari.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.solari.app.ui.theme.SolariThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "solari_user_preferences"
)

class UserPreferencesStore(context: Context) {
    private val applicationContext = context.applicationContext
    private val dataStore = applicationContext.userPreferencesDataStore

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            UserPreferences(
                isDarkMode = preferences[IsDarkModeKey] ?: true,
                currentLightTheme = preferences[CurrentLightThemeKey]?.let {
                    runCatching { SolariThemeVariant.valueOf(it) }.getOrNull()
                } ?: SolariThemeVariant.DEFAULT_LIGHT,
                currentDarkTheme = preferences[CurrentDarkThemeKey]?.let {
                    runCatching { SolariThemeVariant.valueOf(it) }.getOrNull()
                } ?: SolariThemeVariant.DEFAULT_DARK,
                isFlashEnabled = preferences[IsFlashEnabledKey] ?: false,
                timerValue = preferences[TimerValueKey] ?: 0,
                lastFeedViewedTimestamp = preferences[LastFeedViewedTimestampKey] ?: 0L
            )
        }

    suspend fun updateTheme(variant: SolariThemeVariant, isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            if (isDarkMode) {
                preferences[CurrentDarkThemeKey] = variant.name
            } else {
                preferences[CurrentLightThemeKey] = variant.name
            }
            preferences[IsDarkModeKey] = isDarkMode
        }
    }

    suspend fun updateFlashEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IsFlashEnabledKey] = enabled
        }
    }

    suspend fun updateTimerValue(value: Int) {
        dataStore.edit { preferences ->
            preferences[TimerValueKey] = value
        }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IsDarkModeKey] = enabled
        }
    }

    suspend fun updateLastFeedViewedTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LastFeedViewedTimestampKey] = timestamp
        }
        // Broadcast widget update so the unseen count resets
        val intent = android.content.Intent(applicationContext, com.solari.app.widget.SolariWidgetProvider::class.java).apply {
            action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = android.appwidget.AppWidgetManager.getInstance(applicationContext)
            .getAppWidgetIds(android.content.ComponentName(applicationContext, com.solari.app.widget.SolariWidgetProvider::class.java))
        intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        applicationContext.sendBroadcast(intent)
    }

    suspend fun updateLightTheme(variant: SolariThemeVariant) {
        dataStore.edit { preferences ->
            preferences[CurrentLightThemeKey] = variant.name
        }
    }

    suspend fun updateDarkTheme(variant: SolariThemeVariant) {
        dataStore.edit { preferences ->
            preferences[CurrentDarkThemeKey] = variant.name
        }
    }

    private companion object {
        val IsDarkModeKey = booleanPreferencesKey("is_dark_mode")
        val CurrentLightThemeKey = stringPreferencesKey("current_light_theme")
        val CurrentDarkThemeKey = stringPreferencesKey("current_dark_theme")
        val IsFlashEnabledKey = booleanPreferencesKey("is_flash_enabled")
        val TimerValueKey = intPreferencesKey("timer_value")
        val LastFeedViewedTimestampKey = androidx.datastore.preferences.core.longPreferencesKey("last_feed_viewed_timestamp")
    }
}

data class UserPreferences(
    val isDarkMode: Boolean,
    val currentLightTheme: SolariThemeVariant,
    val currentDarkTheme: SolariThemeVariant,
    val isFlashEnabled: Boolean,
    val timerValue: Int,
    val lastFeedViewedTimestamp: Long
)
