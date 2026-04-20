package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {
    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val userResult = userRepository.getMe()) {
                is ApiResult.Success -> currentUser = userResult.data
                is ApiResult.Failure -> errorMessage = userResult.message
            }

            when (val friendsResult = friendRepository.getFriends()) {
                is ApiResult.Success -> users = friendsResult.data
                is ApiResult.Failure -> if (errorMessage == null) errorMessage = friendsResult.message
            }

            when (val feedResult = feedRepository.getFeed()) {
                is ApiResult.Success -> posts = feedResult.data
                is ApiResult.Failure -> if (errorMessage == null) errorMessage = feedResult.message
            }

            isLoading = false
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (val result = feedRepository.deletePost(postId)) {
                is ApiResult.Success -> posts = posts.filter { it.id != postId }
                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }
}
