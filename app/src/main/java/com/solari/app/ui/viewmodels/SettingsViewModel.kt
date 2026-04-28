package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.theme.SolariThemeVariant
import com.solari.app.ui.theme.ThemeMap

class SettingsViewModel : ViewModel() {
    var isDarkMode by mutableStateOf(true)
    var isNotificationsEnabled by mutableStateOf(false)
    
    var currentLightTheme by mutableStateOf(SolariThemeVariant.DEFAULT_LIGHT)
    var currentDarkTheme by mutableStateOf(SolariThemeVariant.DEFAULT_DARK)

    val activeThemeVariant: SolariThemeVariant
        get() = if (isDarkMode) currentDarkTheme else currentLightTheme

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        isNotificationsEnabled = enabled
    }

    fun setTheme(variant: SolariThemeVariant) {
        val isThemeDark = ThemeMap[variant]?.isDark ?: true
        if (isThemeDark) {
            currentDarkTheme = variant
            isDarkMode = true
        } else {
            currentLightTheme = variant
            isDarkMode = false
        }
    }
}
