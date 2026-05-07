package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.UserPreferencesStore
import com.solari.app.data.user.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.TimeZone

class HomepageBeforeCapturingViewModel(
    private val userRepository: UserRepository,
    private val userPreferencesStore: UserPreferencesStore
) : ViewModel() {
    var currentStreak by mutableIntStateOf(0)
        private set

    var streakErrorMessage by mutableStateOf<String?>(null)
        private set

    var isFlashEnabled by mutableStateOf(false)
        private set

    var timerValue by mutableIntStateOf(0)
        private set

    init {
        loadCurrentStreak()
        userPreferencesStore.userPreferencesFlow
            .onEach { preferences ->
                isFlashEnabled = preferences.isFlashEnabled
                timerValue = preferences.timerValue
            }
            .launchIn(viewModelScope)
    }

    fun loadCurrentStreak() {
        viewModelScope.launch {
            try {
                val timezone = TimeZone.getDefault().id
                when (val result = userRepository.getCurrentStreak(timezone)) {
                    is ApiResult.Success -> {
                        currentStreak = result.data
                        streakErrorMessage = null
                    }

                    is ApiResult.Failure -> streakErrorMessage = result.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                streakErrorMessage = throwable.message ?: "Failed to load streak"
            }
        }
    }

    fun toggleFlash() {
        viewModelScope.launch {
            userPreferencesStore.updateFlashEnabled(!isFlashEnabled)
        }
    }

    fun rotateTimer() {
        val nextValue = when (timerValue) {
            0 -> 3
            3 -> 10
            else -> 0
        }
        viewModelScope.launch {
            userPreferencesStore.updateTimerValue(nextValue)
        }
    }
}
