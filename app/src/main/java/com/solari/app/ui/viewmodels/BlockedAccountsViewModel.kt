package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.BlockedUser
import kotlinx.coroutines.launch

class BlockedAccountsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    var blockedUsers by mutableStateOf<List<BlockedUser>>(emptyList())
        private set

    var sort by mutableStateOf("default")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadBlockedUsers()
    }

    fun updateSort(newSort: String) {
        if (sort == newSort) return
        sort = newSort
        loadBlockedUsers()
    }

    fun refresh() {
        loadBlockedUsers()
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            when (val result = userRepository.unblockUser(userId)) {
                is ApiResult.Success -> {
                    blockedUsers = blockedUsers.filterNot { it.user.id == userId }
                    errorMessage = null
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val backendSort = when (sort) {
                "oldest" -> "oldest"
                else -> "newest"
            }
            when (val result = userRepository.getBlockedUsers(sort = backendSort)) {
                is ApiResult.Success -> blockedUsers = result.data
                is ApiResult.Failure -> errorMessage = result.message
            }
            isLoading = false
        }
    }
}
