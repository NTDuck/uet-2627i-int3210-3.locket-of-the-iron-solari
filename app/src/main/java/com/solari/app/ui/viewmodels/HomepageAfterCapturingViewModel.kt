package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class HomepageAfterCapturingViewModel(
    private val friendRepository: FriendRepository
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            when (val result = friendRepository.getFriends()) {
                is ApiResult.Success -> {
                    friends = result.data
                    errorMessage = null
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }
}
