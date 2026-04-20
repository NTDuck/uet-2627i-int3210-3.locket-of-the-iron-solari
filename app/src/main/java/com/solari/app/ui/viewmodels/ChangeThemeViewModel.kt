package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.ui.theme.SolariThemeVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChangeThemeViewModel : ViewModel() {
    var availableVariants by mutableStateOf<List<SolariThemeVariant>>(emptyList())
    var isLoading by mutableStateOf(true)

    init {
        loadSchemes()
    }

    private fun loadSchemes() {
        viewModelScope.launch {
            delay(200) // Simulate a very fast loading for UX
            availableVariants = SolariThemeVariant.entries
            isLoading = false
        }
    }
}
