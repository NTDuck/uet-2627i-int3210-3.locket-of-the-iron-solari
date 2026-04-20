package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class FeedBrowseViewModel(
    private val feedRepository: FeedRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var registeredViewPostIds by mutableStateOf<Set<String>>(emptySet())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val friendsResult = friendRepository.getFriends()) {
                is ApiResult.Success -> friends = friendsResult.data
                is ApiResult.Failure -> errorMessage = friendsResult.message
            }

            when (val feedResult = feedRepository.getFeed()) {
                is ApiResult.Success -> posts = feedResult.data
                is ApiResult.Failure -> if (errorMessage == null) errorMessage = feedResult.message
            }

            isLoading = false
        }
    }

    fun registerPostView(postId: String) {
        if (postId in registeredViewPostIds) {
            return
        }

        registeredViewPostIds = registeredViewPostIds + postId
        viewModelScope.launch {
            when (val result = feedRepository.registerPostView(postId)) {
                is ApiResult.Success -> Unit
                is ApiResult.Failure -> {
                    registeredViewPostIds = registeredViewPostIds - postId
                    errorMessage = result.message
                }
            }
        }
    }
}
