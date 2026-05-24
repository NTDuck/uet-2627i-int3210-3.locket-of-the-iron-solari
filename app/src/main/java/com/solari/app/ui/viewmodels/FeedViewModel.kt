package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.feed.PostUploadEntry
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.models.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val PostActivityPageSize = 50

private data class PostActivityPagination(
    val viewersNextCursor: String?,
    val reactionsNextCursor: String?
) {
    val canLoadMore: Boolean
        get() = viewersNextCursor != null || reactionsNextCursor != null
}

class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val conversationRepository: ConversationRepository,
    private val postUploadCoordinator: PostUploadCoordinator,
    private val userPreferencesStore: com.solari.app.data.preferences.UserPreferencesStore
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

    var loadingMorePostActivityIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var authorFilterIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var sortMode by mutableStateOf("default")
        private set

    var isFetchingNextPage by mutableStateOf(false)
        private set

    var hasReachedEnd by mutableStateOf(false)
        private set

    var deletedPostIds by mutableStateOf<Set<String>>(emptySet())
        private set

    private var nextCursor: String? = null
    private var remotePosts: List<Post> = emptyList()
    private var uploadEntries: List<PostUploadEntry> = emptyList()
    private var postActivityPaginationByPostId by mutableStateOf<Map<String, PostActivityPagination>>(emptyMap())
    private val postActivityLoadGenerations = mutableMapOf<String, Int>()

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
                postActivities = postActivities.filterKeys { it !in deletedIds }
                loadingPostActivityIds = loadingPostActivityIds - deletedIds
                loadingMorePostActivityIds = loadingMorePostActivityIds - deletedIds
                postActivityPaginationByPostId = postActivityPaginationByPostId.filterKeys { it !in deletedIds }
                applyDisplayPosts()
            }
        }

        viewModelScope.launch {
            feedRepository.newlyPublishedPosts.collectLatest { newPost ->
                if (newPost.id !in deletedPostIds) {
                    val isDuplicate = remotePosts.any { it.id == newPost.id }
                    // When a post is prepended, we apply filters to ensure it matches
                    val matchesAuthorFilter = authorFilterIds.isEmpty() || newPost.author.id in authorFilterIds
                    if (!isDuplicate && matchesAuthorFilter) {
                        remotePosts = listOf(newPost) + remotePosts
                        applyDisplayPosts()
                    }
                }
            }
        }

        refresh()
    }

    fun updateFilters(authorIds: Set<String>, sort: String, targetPostId: String? = null) {
        if (authorFilterIds == authorIds && sortMode == sort && (targetPostId == null || posts.any { it.id == targetPostId })) return
        authorFilterIds = authorIds
        sortMode = sort
        refresh(resetPagination = true, targetPostId = targetPostId)
    }

    fun resetFilters() {
        if (authorFilterIds.isEmpty() && sortMode == "default") return
        authorFilterIds = emptySet()
        sortMode = "default"
        refresh(resetPagination = true)
    }

    fun loadNextPage() {
        if (isLoading || isFetchingNextPage || hasReachedEnd) return
        refresh(resetPagination = false)
    }

    fun refresh(resetPagination: Boolean = true, targetPostId: String? = null) {
        viewModelScope.launch {
            if (resetPagination) {
                isLoading = true
                nextCursor = null
                hasReachedEnd = false
                remotePosts = emptyList()
                errorMessage = null

                when (val userResult = userRepository.getMe()) {
                    is ApiResult.Success -> {
                        currentUser = userResult.data
                    }

                    is ApiResult.Failure -> errorMessage = userResult.message
                }

                when (val friendsResult = friendRepository.getFriends()) {
                    is ApiResult.Success -> users = friendsResult.data
                    is ApiResult.Failure -> if (errorMessage == null) errorMessage =
                        friendsResult.message
                }
            } else {
                isFetchingNextPage = true
            }

            when (val feedResult = feedRepository.getFeed(
                authorIds = authorFilterIds,
                sort = sortMode,
                cursor = nextCursor,
                limit = if (resetPagination) 20 else 15
            )) {
                is ApiResult.Success -> {
                    val newPosts = feedResult.data.posts
                    nextCursor = feedResult.data.nextCursor
                    hasReachedEnd = nextCursor == null
                    viewModelScope.launch {
                        userPreferencesStore.updateLastFeedViewedTimestamp(System.currentTimeMillis())
                    }

                    var postsToDisplay = newPosts
                    if (resetPagination && targetPostId != null && newPosts.none { it.id == targetPostId }) {
                        when (val postResult = feedRepository.getPost(targetPostId)) {
                            is ApiResult.Success -> {
                                postsToDisplay =
                                    listOf(postResult.data) + newPosts.filter { it.id != targetPostId }
                            }

                            is ApiResult.Failure -> {
                                // Fallback to just newPosts if fetching target fails
                            }
                        }
                    }

                    remotePosts = if (resetPagination) {
                        postsToDisplay
                    } else {
                        val existingIds = remotePosts.map { it.id }.toSet()
                        remotePosts + newPosts.filter { it.id !in existingIds }
                    }

                    val livePostIds = newPosts.map { it.id }.toSet()
                    postUploadCoordinator.removeSyncedUploads(livePostIds)
                    applyDisplayPosts()

                    if (postActivities.size > 100) {
                        postActivities = postActivities.toList().takeLast(100).toMap()
                        postActivityPaginationByPostId = postActivityPaginationByPostId
                            .filterKeys { it in postActivities.keys }
                    }
                }

                is ApiResult.Failure -> if (errorMessage == null) errorMessage = feedResult.message
            }

            if (resetPagination) isLoading = false else isFetchingNextPage = false
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            when (val result = feedRepository.deletePost(postId)) {
                is ApiResult.Success -> {
                    postUploadCoordinator.removePost(postId)
                    remotePosts = remotePosts.filter { it.id != postId }
                    applyDisplayPosts()
                    postActivities = postActivities - postId
                    loadingPostActivityIds = loadingPostActivityIds - postId
                    loadingMorePostActivityIds = loadingMorePostActivityIds - postId
                    postActivityPaginationByPostId = postActivityPaginationByPostId - postId
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun loadPostActivity(postId: String, force: Boolean = false) {
        if (uploadEntries.any { it.draft.id == postId && it.draft.uploadStatus != PostUploadStatus.None }) {
            postActivities = postActivities + (postId to emptyList())
            postActivityPaginationByPostId = postActivityPaginationByPostId - postId
            return
        }

        if (!force && (postActivities.containsKey(postId) || postId in loadingPostActivityIds)) {
            return
        }

        val requestGeneration = nextPostActivityLoadGeneration(postId)
        viewModelScope.launch {
            loadingPostActivityIds = loadingPostActivityIds + postId
            loadingMorePostActivityIds = loadingMorePostActivityIds - postId
            postActivityPaginationByPostId = postActivityPaginationByPostId - postId

            try {
                val viewersDeferred = async {
                    feedRepository.getPostViewersPage(
                        postId = postId,
                        limit = PostActivityPageSize,
                        cursor = null
                    )
                }
                val reactionsDeferred = async {
                    feedRepository.getPostReactionsPage(
                        postId = postId,
                        limit = PostActivityPageSize,
                        cursor = null
                    )
                }
                val viewersResult = viewersDeferred.await()
                val reactionsResult = reactionsDeferred.await()

                if (!isCurrentPostActivityRequest(postId, requestGeneration)) {
                    return@launch
                }

                val activities = buildList {
                    if (viewersResult is ApiResult.Success) addAll(viewersResult.data.activities)
                    if (reactionsResult is ApiResult.Success) addAll(reactionsResult.data.activities)
                }.sortedByDescending { it.timestamp }

                if (activities.isNotEmpty() ||
                    viewersResult is ApiResult.Success ||
                    reactionsResult is ApiResult.Success
                ) {
                    postActivities = postActivities + (postId to activities)
                    postActivityPaginationByPostId = postActivityPaginationByPostId + (
                        postId to PostActivityPagination(
                            viewersNextCursor = (viewersResult as? ApiResult.Success)?.data?.nextCursor,
                            reactionsNextCursor = (reactionsResult as? ApiResult.Success)?.data?.nextCursor
                        )
                    )
                }

                val failure = listOf(viewersResult, reactionsResult)
                    .filterIsInstance<ApiResult.Failure>()
                    .firstOrNull()
                if (failure != null && activities.isEmpty()) {
                    errorMessage = failure.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                if (isCurrentPostActivityRequest(postId, requestGeneration)) {
                    errorMessage = throwable.message ?: "Failed to load post activity"
                }
            } finally {
                if (isCurrentPostActivityRequest(postId, requestGeneration)) {
                    loadingPostActivityIds = loadingPostActivityIds - postId
                }
            }
        }
    }

    fun canLoadMorePostActivity(postId: String): Boolean {
        return postActivityPaginationByPostId[postId]?.canLoadMore == true
    }

    fun loadMorePostActivity(postId: String) {
        val pagination = postActivityPaginationByPostId[postId] ?: return
        if (!pagination.canLoadMore ||
            postId in loadingPostActivityIds ||
            postId in loadingMorePostActivityIds
        ) {
            return
        }

        val requestGeneration = postActivityLoadGenerations[postId] ?: 0
        viewModelScope.launch {
            loadingMorePostActivityIds = loadingMorePostActivityIds + postId

            try {
                val viewersDeferred = pagination.viewersNextCursor?.let { cursor ->
                    async {
                        feedRepository.getPostViewersPage(
                            postId = postId,
                            limit = PostActivityPageSize,
                            cursor = cursor
                        )
                    }
                }
                val reactionsDeferred = pagination.reactionsNextCursor?.let { cursor ->
                    async {
                        feedRepository.getPostReactionsPage(
                            postId = postId,
                            limit = PostActivityPageSize,
                            cursor = cursor
                        )
                    }
                }

                val viewersResult = viewersDeferred?.await()
                val reactionsResult = reactionsDeferred?.await()

                if (!isCurrentPostActivityRequest(postId, requestGeneration)) {
                    return@launch
                }

                val newActivities = buildList {
                    if (viewersResult is ApiResult.Success) addAll(viewersResult.data.activities)
                    if (reactionsResult is ApiResult.Success) addAll(reactionsResult.data.activities)
                }

                val mergedActivities = mergePostActivities(
                    currentActivities = postActivities[postId].orEmpty(),
                    newActivities = newActivities
                )
                postActivities = postActivities + (postId to mergedActivities)
                postActivityPaginationByPostId = postActivityPaginationByPostId + (
                    postId to PostActivityPagination(
                        viewersNextCursor = when (viewersResult) {
                            is ApiResult.Success -> viewersResult.data.nextCursor
                            is ApiResult.Failure -> null
                            null -> null
                        },
                        reactionsNextCursor = when (reactionsResult) {
                            is ApiResult.Success -> reactionsResult.data.nextCursor
                            is ApiResult.Failure -> null
                            null -> null
                        }
                    )
                )

                val failure = listOfNotNull(viewersResult, reactionsResult)
                    .filterIsInstance<ApiResult.Failure>()
                    .firstOrNull()
                if (failure != null && newActivities.isEmpty()) {
                    errorMessage = failure.message
                }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                if (isCurrentPostActivityRequest(postId, requestGeneration)) {
                    errorMessage = throwable.message ?: "Failed to load more post activity"
                    postActivityPaginationByPostId = postActivityPaginationByPostId + (
                        postId to PostActivityPagination(
                            viewersNextCursor = null,
                            reactionsNextCursor = null
                        )
                    )
                }
            } finally {
                if (isCurrentPostActivityRequest(postId, requestGeneration)) {
                    loadingMorePostActivityIds = loadingMorePostActivityIds - postId
                }
            }
        }
    }

    private fun nextPostActivityLoadGeneration(postId: String): Int {
        val nextGeneration = (postActivityLoadGenerations[postId] ?: 0) + 1
        postActivityLoadGenerations[postId] = nextGeneration
        return nextGeneration
    }

    private fun isCurrentPostActivityRequest(postId: String, generation: Int): Boolean {
        return postActivityLoadGenerations[postId] == generation
    }

    private fun mergePostActivities(
        currentActivities: List<PostActivityEntry>,
        newActivities: List<PostActivityEntry>
    ): List<PostActivityEntry> {
        val seenActivityKeys = currentActivities.mapTo(mutableSetOf()) { it.activityKey() }
        return (currentActivities + newActivities.filter { seenActivityKeys.add(it.activityKey()) })
            .sortedByDescending { it.timestamp }
    }

    private fun PostActivityEntry.activityKey(): String {
        return listOf(
            user.id,
            description,
            timestamp.toString(),
            emoji.orEmpty(),
            caption.orEmpty()
        ).joinToString(separator = "|")
    }

    fun retryPostUpload(postId: String) {
        val didRetry = postUploadCoordinator.retryUpload(postId)
        if (!didRetry) {
            errorMessage = "Upload can only be retried after it fails."
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
            when (val conversationResult =
                conversationRepository.createConversation(post.author.id)) {
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

    fun addOptimisticPost() = Unit

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
        val visibleUploadPosts = uploadPosts.filterNot { it.id in deletedPostIds }
        val uploadPostIds = visibleUploadPosts.map(Post::id).toSet()
        val allPosts =
            visibleUploadPosts + remotePosts.filterNot { it.id in uploadPostIds || it.id in deletedPostIds }

        val filteredPosts = if (authorFilterIds.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { it.author.id in authorFilterIds }
        }

        posts = when (sortMode) {
            "newest" -> filteredPosts.sortedByDescending { it.timestamp }
            "oldest" -> filteredPosts.sortedBy { it.timestamp }
            else -> filteredPosts
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
            captionType = captionType,
            captionMetadata = captionMetadata,
            uploadStatus = uploadStatus,
            uploadError = uploadError
        )
    }
}
