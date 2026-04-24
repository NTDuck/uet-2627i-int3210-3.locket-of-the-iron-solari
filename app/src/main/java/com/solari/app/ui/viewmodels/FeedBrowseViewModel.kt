package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.feed.PostUploadEntry
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.models.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FeedBrowseViewModel(
    private val feedRepository: FeedRepository,
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository,
    private val postUploadCoordinator: PostUploadCoordinator
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var registeredViewPostIds by mutableStateOf<Set<String>>(emptySet())
    private var remotePosts: List<Post> = emptyList()
    private var uploadEntries: List<PostUploadEntry> = emptyList()

    init {
        viewModelScope.launch {
            postUploadCoordinator.uploads.collectLatest { uploads ->
                uploadEntries = uploads
                applyDisplayPosts()
            }
        }

        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            when (val meResult = userRepository.getMe()) {
                is ApiResult.Success -> {
                    currentUser = meResult.data
                    applyDisplayPosts()
                }
                is ApiResult.Failure -> errorMessage = meResult.message
            }

            when (val friendsResult = friendRepository.getFriends()) {
                is ApiResult.Success -> friends = friendsResult.data
                is ApiResult.Failure -> errorMessage = friendsResult.message
            }

            when (val feedResult = feedRepository.getFeed()) {
                is ApiResult.Success -> {
                    remotePosts = feedResult.data
                    postUploadCoordinator.removeSyncedUploads(feedResult.data.map { it.id }.toSet())
                    applyDisplayPosts()
                }
                is ApiResult.Failure -> if (errorMessage == null) errorMessage = feedResult.message
            }

            isLoading = false
        }
    }

    fun registerPostView(postId: String) {
        if (postId in registeredViewPostIds ||
            posts.any { it.id == postId && it.uploadStatus != PostUploadStatus.None }
        ) {
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

    private fun applyDisplayPosts() {
        val user = currentUser
        val uploadPosts = if (user == null) {
            emptyList()
        } else {
            uploadEntries
                .sortedByDescending { it.draft.createdAt }
                .map { entry ->
                    entry.remotePost ?: entry.draft.toPost(user)
                }
        }
        val uploadPostIds = uploadPosts.map(Post::id).toSet()
        posts = uploadPosts + remotePosts.filterNot { it.id in uploadPostIds }
    }

    private fun OptimisticPostDraft.toPost(author: User): Post {
        return Post(
            id = id,
            author = author,
            imageUrl = mediaUri.toString(),
            thumbnailUrl = mediaUri.toString(),
            mediaType = contentType,
            timestamp = createdAt,
            caption = caption,
            uploadStatus = uploadStatus,
            uploadError = uploadError
        )
    }
}
