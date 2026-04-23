package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val conversationRepository: ConversationRepository
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

    var successMessage by mutableStateOf<String?>(null)
        private set

    var postActivities by mutableStateOf<Map<String, List<PostActivityEntry>>>(emptyMap())
        private set

    var loadingPostActivityIds by mutableStateOf<Set<String>>(emptySet())
        private set

    private var registeredViewPostIds by mutableStateOf<Set<String>>(emptySet())

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
                is ApiResult.Success -> {
                    posts = feedResult.data
                    val livePostIds = feedResult.data.map { it.id }.toSet()
                    postActivities = postActivities.filterKeys { it in livePostIds }
                }
                is ApiResult.Failure -> if (errorMessage == null) errorMessage = feedResult.message
            }

            isLoading = false
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (val result = feedRepository.deletePost(postId)) {
                is ApiResult.Success -> {
                    posts = posts.filter { it.id != postId }
                    postActivities = postActivities - postId
                    loadingPostActivityIds = loadingPostActivityIds - postId
                }
                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun loadPostActivity(postId: String, force: Boolean = false) {
        if (!force && (postActivities.containsKey(postId) || postId in loadingPostActivityIds)) {
            return
        }

        viewModelScope.launch {
            loadingPostActivityIds = loadingPostActivityIds + postId

            val viewersResult = feedRepository.getPostViewers(postId)
            val reactionsResult = feedRepository.getPostReactions(postId)

            val activities = buildList {
                if (viewersResult is ApiResult.Success) addAll(viewersResult.data)
                if (reactionsResult is ApiResult.Success) addAll(reactionsResult.data)
            }.sortedByDescending { it.timestamp }

            if (activities.isNotEmpty() ||
                viewersResult is ApiResult.Success ||
                reactionsResult is ApiResult.Success
            ) {
                postActivities = postActivities + (postId to activities)
            }

            val failure = listOf(viewersResult, reactionsResult)
                .filterIsInstance<ApiResult.Failure>()
                .firstOrNull()
            if (failure != null && activities.isEmpty()) {
                errorMessage = failure.message
            }

            loadingPostActivityIds = loadingPostActivityIds - postId
        }
    }

    fun registerPostView(post: Post) {
        val currentUserId = currentUser?.id ?: return
        if (post.author.id == currentUserId || post.id in registeredViewPostIds) {
            return
        }

        registeredViewPostIds = registeredViewPostIds + post.id
        viewModelScope.launch {
            when (val result = feedRepository.registerPostView(post.id)) {
                is ApiResult.Success -> Unit
                is ApiResult.Failure -> {
                    registeredViewPostIds = registeredViewPostIds - post.id
                    errorMessage = result.message
                }
            }
        }
    }

    fun sendPostReaction(
        post: Post,
        emoji: String,
        note: String?,
        onSent: () -> Unit = {}
    ) {
        val trimmedNote = note?.trim()?.take(20)?.takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            when (val result = feedRepository.sendPostReaction(post.id, emoji, trimmedNote)) {
                is ApiResult.Success -> {
                    errorMessage = null
                    successMessage = "Reacted to ${post.author.displayName}'s post"
                    onSent()
                }
                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun sendPostReply(
        post: Post,
        content: String,
        onSent: () -> Unit = {}
    ) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) {
            return
        }

        viewModelScope.launch {
            when (val conversationResult = conversationRepository.createConversation(post.author.id)) {
                is ApiResult.Failure -> errorMessage = conversationResult.message
                is ApiResult.Success -> {
                    when (
                        val messageResult = conversationRepository.sendMessage(
                            conversationId = conversationResult.data,
                            content = trimmedContent,
                            referencedPostId = post.id
                        )
                    ) {
                        is ApiResult.Success -> {
                            errorMessage = null
                            successMessage = "Replied to ${post.author.displayName}'s post"
                            onSent()
                        }
                        is ApiResult.Failure -> errorMessage = messageResult.message
                    }
                }
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
