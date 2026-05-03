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
    var selectedSort by mutableStateOf("newest")
        private set

    var selectedFriendIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var feedListFirstVisibleItemIndex by mutableStateOf(0)
        private set

    var feedListFirstVisibleItemScrollOffset by mutableStateOf(0)
        private set

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

    var isFetchingNextPage by mutableStateOf(false)
        private set

    var hasReachedEnd by mutableStateOf(false)
        private set

    private var deletedPostIds by mutableStateOf<Set<String>>(emptySet())
    private var nextCursor: String? = null
    private var feedRequestSequence = 0

    private var registeredViewPostIds by mutableStateOf<Set<String>>(emptySet())
    private var remotePosts: List<Post> = emptyList()
    private var uploadEntries: List<PostUploadEntry> = emptyList()
    private var hasAppliedInitialAuthorFilter = false

    init {
        viewModelScope.launch {
            postUploadCoordinator.uploads.collectLatest { uploads ->
                uploadEntries = uploads
                applyDisplayPosts()
            }
        }

        viewModelScope.launch {
            feedRepository.deletedPostIds.collectLatest { deletedIds ->
                deletedPostIds = deletedIds
                remotePosts = remotePosts.filterNot { it.id in deletedIds }
                registeredViewPostIds = registeredViewPostIds - deletedIds
                applyDisplayPosts()
            }
        }

        refresh()
    }

    fun applyInitialAuthorFilter(authorId: String?) {
        if (hasAppliedInitialAuthorFilter) return
        hasAppliedInitialAuthorFilter = true
        val initialAuthorIds = authorId?.takeIf { it.isNotBlank() }?.let { setOf(it) }.orEmpty()
        if (initialAuthorIds == selectedFriendIds) return

        selectedFriendIds = initialAuthorIds
        resetFeedListScroll()
        refresh(resetPagination = true)
    }

    fun updateSelectedSort(sort: String) {
        val normalizedSort = sort.toFeedSort()
        if (normalizedSort == selectedSort) return
        selectedSort = normalizedSort
        resetFeedListScroll()
        refresh(resetPagination = true)
    }

    fun toggleFriendFilter(userId: String) {
        selectedFriendIds = if (userId in selectedFriendIds) {
            selectedFriendIds - userId
        } else {
            selectedFriendIds + userId
        }
        resetFeedListScroll()
        refresh(resetPagination = true)
    }

    fun clearFriendFilters() {
        if (selectedFriendIds.isEmpty()) return
        selectedFriendIds = emptySet()
        resetFeedListScroll()
        refresh(resetPagination = true)
    }

    fun loadNextPage() {
        if (isLoading || isFetchingNextPage || hasReachedEnd) return
        refresh(resetPagination = false)
    }

    fun updateFeedListScroll(
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int
    ) {
        feedListFirstVisibleItemIndex = firstVisibleItemIndex.coerceAtLeast(0)
        feedListFirstVisibleItemScrollOffset = firstVisibleItemScrollOffset.coerceAtLeast(0)
    }

    fun resetFeedListScroll() {
        feedListFirstVisibleItemIndex = 0
        feedListFirstVisibleItemScrollOffset = 0
    }

    fun refresh(resetPagination: Boolean = true) {
        viewModelScope.launch {
            val requestSequence = if (resetPagination) {
                feedRequestSequence += 1
                feedRequestSequence
            } else {
                feedRequestSequence
            }
            val requestAuthorIds = selectedFriendIds
            val requestSort = selectedSort.toFeedSort()
            val requestCursor = if (resetPagination) null else nextCursor

            if (resetPagination) {
                isLoading = true
                isFetchingNextPage = false
                nextCursor = null
                hasReachedEnd = false
                remotePosts = emptyList()
                errorMessage = null

                when (val meResult = userRepository.getMe()) {
                    is ApiResult.Success -> currentUser = meResult.data
                    is ApiResult.Failure -> errorMessage = meResult.message
                }
                when (val friendsResult = friendRepository.getFriends()) {
                    is ApiResult.Success -> friends = friendsResult.data
                    is ApiResult.Failure -> errorMessage = friendsResult.message
                }
            } else {
                isFetchingNextPage = true
            }

            when (
                val feedResult = feedRepository.getFeed(
                    authorIds = requestAuthorIds,
                    sort = requestSort,
                    limit = 15,
                    cursor = requestCursor
                )
            ) {
                is ApiResult.Success -> {
                    if (requestSequence != feedRequestSequence) return@launch

                    val newPosts = feedResult.data.posts
                    nextCursor = feedResult.data.nextCursor
                    hasReachedEnd = nextCursor == null

                    remotePosts = if (resetPagination) {
                        newPosts
                    } else {
                        (remotePosts + newPosts).distinctBy(Post::id)
                    }
                    postUploadCoordinator.removeSyncedUploads(newPosts.map { it.id }.toSet())
                    applyDisplayPosts()
                }
                is ApiResult.Failure -> {
                    if (requestSequence != feedRequestSequence) return@launch
                    if (errorMessage == null) errorMessage = feedResult.message
                }
            }

            if (resetPagination) isLoading = false else isFetchingNextPage = false
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
        val visibleUploadPosts = uploadPosts.filter { post ->
            post.id !in deletedPostIds &&
                    (selectedFriendIds.isEmpty() || post.author.id in selectedFriendIds)
        }
        val uploadPostIds = visibleUploadPosts.map(Post::id).toSet()
        val combinedPosts = visibleUploadPosts + remotePosts.filterNot { it.id in uploadPostIds || it.id in deletedPostIds }
        posts = when (selectedSort) {
            "oldest" -> combinedPosts.sortedBy(Post::timestamp)
            else -> combinedPosts.sortedByDescending(Post::timestamp)
        }
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

    private fun String.toFeedSort(): String {
        return when (this) {
            "oldest" -> "oldest"
            else -> "newest"
        }
    }
}
