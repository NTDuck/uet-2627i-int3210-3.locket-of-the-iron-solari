package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import java.util.TimeZone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class HomepageBeforeCapturingViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    var currentStreak by mutableIntStateOf(0)
        private set

    var streakErrorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadCurrentStreak()
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
}
