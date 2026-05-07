package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.preferences.UserPreferencesStore
import com.solari.app.ui.theme.SolariThemeVariant
import com.solari.app.ui.theme.ThemeMap
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesStore: UserPreferencesStore
) : ViewModel() {
    var isDarkMode by mutableStateOf(true)

    var currentLightTheme by mutableStateOf(SolariThemeVariant.DEFAULT_LIGHT)
    var currentDarkTheme by mutableStateOf(SolariThemeVariant.DEFAULT_DARK)

    val activeThemeVariant: SolariThemeVariant
        get() = if (isDarkMode) currentDarkTheme else currentLightTheme

    init {
        userPreferencesStore.userPreferencesFlow
            .onEach { preferences ->
                isDarkMode = preferences.isDarkMode
                currentLightTheme = preferences.currentLightTheme
                currentDarkTheme = preferences.currentDarkTheme
            }
            .launchIn(viewModelScope)
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.updateDarkMode(enabled)
        }
    }

    fun setTheme(variant: SolariThemeVariant) {
        val isThemeDark = ThemeMap[variant]?.isDark ?: true
        viewModelScope.launch {
            userPreferencesStore.updateTheme(variant, isThemeDark)
        }
    }
}
