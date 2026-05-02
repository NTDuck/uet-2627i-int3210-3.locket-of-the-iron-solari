package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.ui.theme.DarkThemes
import com.solari.app.ui.theme.LightThemes
import com.solari.app.ui.theme.SolariThemeVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangeThemeViewModel : ViewModel() {
    var availableVariants by mutableStateOf<List<SolariThemeVariant>>(emptyList())
    var isLoading by mutableStateOf(true)

    fun loadSchemes(isDarkMode: Boolean) {
        isLoading = true
        viewModelScope.launch {
            delay(150) // Simulate a very fast loading for UX
            availableVariants = if (isDarkMode) DarkThemes else LightThemes
            isLoading = false
        }
    }
}
