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
    var activeThemeVariant by mutableStateOf(SolariThemeVariant.DEFAULT_DARK)

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
        activeThemeVariant = if (isDarkMode) SolariThemeVariant.DEFAULT_DARK else SolariThemeVariant.DEFAULT_LIGHT
    }

    fun toggleNotifications(enabled: Boolean) {
        isNotificationsEnabled = enabled
    }

    fun setTheme(variant: SolariThemeVariant) {
        activeThemeVariant = variant
        isDarkMode = ThemeMap[variant]?.isDark ?: true
    }
}
