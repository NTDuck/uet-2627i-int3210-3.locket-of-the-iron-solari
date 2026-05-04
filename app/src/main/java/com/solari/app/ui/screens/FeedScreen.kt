package com.solari.app.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.models.User
import com.solari.app.ui.models.CaptionMetadata
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.PersistentMediaCache
import com.solari.app.ui.util.PersistentMediaCacheKind
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.FeedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private enum class FeedInputOverlayMode {
    Reaction,
    Message
}

private const val FeedSharedMediaTransitionMillis = 200
private val FeedSharedMediaCornerRadius = 8.dp
private val FeedPostMediaCornerRadius = 14.dp
private val FeedInputOverlayKeyboardGap = 14.dp
private val FeedInputOverlayBottomBarCompensation = 104.dp
private const val FeedNeighborPrefetchDistance = 2
private const val FeedNeighborPrefetchParallelism = 4

private data class FeedActivityUserGroup(
    val user: User,
    val activities: List<PostActivityEntry>
)

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    initialPostId: String? = null,
    initialPost: Post? = null,
    initialPosts: List<Post>? = null,
    enableInitialSharedTransition: Boolean = false,
    authorFilterIds: Set<String> = emptySet(),
    sortMode: String = "default",
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onNavigateToBrowse: (String?) -> Unit,
    isFeedVisible: Boolean = true,
    onActivityPanelVisibilityChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showMenuForPost by remember { mutableStateOf<Post?>(null) }
    var postPendingDelete by remember { mutableStateOf<Post?>(null) }
    var postPendingLegacyDownload by remember { mutableStateOf<Post?>(null) }
    var isActivitySheetVisible by remember { mutableStateOf(false) }
    var activitySheetPostId by remember { mutableStateOf<String?>(null) }
    var feedbackPillVisible by remember { mutableStateOf(false) }
    var feedbackPillMessage by remember { mutableStateOf("") }
    var feedbackPillEventId by remember { mutableIntStateOf(0) }
    var isInputOverlayVisible by remember { mutableStateOf(false) }
    var isUserRefreshing by remember { mutableStateOf(false) }
    var isOpeningMediaSharedTransitionEnabled by remember(
        initialPostId,
        enableInitialSharedTransition
    ) {
        mutableStateOf(enableInitialSharedTransition && initialPostId != null && initialPosts != null)
    }
    var hasOpeningMediaSharedTransitionStarted by remember(initialPostId) { mutableStateOf(false) }
    val sourcePosts = viewModel.posts
    val deletedPostIds = viewModel.deletedPostIds
    val isSourceListSyncedToRequestedFilters = viewModel.authorFilterIds == authorFilterIds &&
            viewModel.sortMode == sortMode &&
            !viewModel.isLoading
    val posts = remember(
        sourcePosts,
        initialPostId,
        initialPost,
        initialPosts,
        deletedPostIds,
        authorFilterIds,
        sortMode,
        isSourceListSyncedToRequestedFilters
    ) {
        val refreshedPostsById = sourcePosts
            .filterNot { it.id in deletedPostIds }
            .associateBy(Post::id)

        if (!initialPosts.isNullOrEmpty()) {
            val visibleInitialPosts = initialPosts.filterNot { it.id in deletedPostIds }
            val initialPostIds = visibleInitialPosts.map(Post::id).toSet()
            val syncedInitialPosts = visibleInitialPosts.map { post ->
                refreshedPostsById[post.id]?.withCaptionFieldsFromFallback(post) ?: post
            }
            val additionalPosts = if (isSourceListSyncedToRequestedFilters) {
                sourcePosts.filterNot { it.id in initialPostIds || it.id in deletedPostIds }
            } else {
                emptyList()
            }
            syncedInitialPosts + additionalPosts
        } else {
            val initialSelectedPost = initialPost?.takeIf { it.id !in deletedPostIds }
            val selectedPost = initialPostId
                ?.let { targetPostId ->
                    val refreshedPost = refreshedPostsById[targetPostId]
                    if (refreshedPost != null && initialSelectedPost?.id == refreshedPost.id) {
                        refreshedPost.withCaptionFieldsFromFallback(initialSelectedPost)
                    } else {
                        refreshedPost
                    }
                }
                ?: initialSelectedPost
            val remainingPosts = selectedPost
                ?.let { selected -> sourcePosts.filterNot { it.id == selected.id || it.id in deletedPostIds } }
                ?: sourcePosts.filterNot { it.id in deletedPostIds }
            val filteredPosts = if (authorFilterIds.isEmpty()) {
                remainingPosts
            } else {
                remainingPosts.filter { it.author.id in authorFilterIds }
            }

            val sortedPosts = when (sortMode) {
                "newest" -> filteredPosts.sortedByDescending { it.timestamp }
                "oldest" -> filteredPosts.sortedBy { it.timestamp }
                else -> filteredPosts
            }

            if (selectedPost != null) {
                listOf(selectedPost) + sortedPosts
            } else {
                sortedPosts
            }
        }
    }
    val currentUser = viewModel.currentUser
    val initialPostPage = remember(posts, initialPostId) {
        if (initialPostId != null) {
            val index = posts.indexOfFirst { it.id == initialPostId }
            if (index >= 0) index else 0
        } else 0
    }
    val pagerState = rememberPagerState(initialPage = initialPostPage) { posts.size }
    var isInitialScrollDone by remember { mutableStateOf(initialPostId == null) }

    LaunchedEffect(initialPostId, posts.size) {
        if (initialPostId != null && !isInitialScrollDone) {
            val index = posts.indexOfFirst { it.id == initialPostId }
            if (index >= 0) {
                pagerState.scrollToPage(index)
                isInitialScrollDone = true
            }
        }
    }

    LaunchedEffect(authorFilterIds, sortMode, initialPostId) {
        viewModel.updateFilters(authorFilterIds, sortMode, initialPostId)
    }

    LaunchedEffect(
        isOpeningMediaSharedTransitionEnabled,
        sharedTransitionScope.isTransitionActive
    ) {
        if (!isOpeningMediaSharedTransitionEnabled) return@LaunchedEffect

        if (sharedTransitionScope.isTransitionActive) {
            hasOpeningMediaSharedTransitionStarted = true
        } else if (hasOpeningMediaSharedTransitionStarted) {
            isOpeningMediaSharedTransitionEnabled = false
            hasOpeningMediaSharedTransitionStarted = false
        }
    }

    LaunchedEffect(pagerState.currentPage, posts.size) {
        if (posts.isNotEmpty() && pagerState.currentPage >= posts.size - 3) {
            viewModel.loadNextPage()
        }
    }

    val imageLoader = remember(context) { coil.Coil.imageLoader(context) }

    LaunchedEffect(pagerState.currentPage, posts) {
        val currentPage = pagerState.currentPage
        if (posts.isEmpty() || currentPage !in posts.indices) return@LaunchedEffect

        val prefetchPosts =
            ((currentPage - FeedNeighborPrefetchDistance)..(currentPage + FeedNeighborPrefetchDistance))
                .filter { page -> page in posts.indices && page != currentPage }
                .map { posts[it] }

        val imagePrefetchUrls = prefetchPosts
            .filter { !it.isVideoMedia() }
            .flatMap { it.feedMediaPrefetchUrls() }
            .distinct()

        val videoPrefetchUrls = prefetchPosts
            .filter { it.isVideoMedia() }
            .flatMap { it.feedMediaPrefetchUrls() }
            .distinct()

        imagePrefetchUrls.forEach { url ->
            imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .build()
            )
        }

        videoPrefetchUrls.chunked(FeedNeighborPrefetchParallelism).forEach { urlChunk ->
            urlChunk.map { url ->
                async {
                    PersistentMediaCache.resolve(
                        context = context,
                        url = url,
                        kind = PersistentMediaCacheKind.FeedMedia
                    )
                }
            }.awaitAll()
        }
    }

    LaunchedEffect(pagerState.currentPage, posts, currentUser?.id) {
        val currentPage = pagerState.currentPage
        if (posts.isEmpty() || currentPage !in posts.indices) return@LaunchedEffect

        val currentUserId = currentUser?.id ?: return@LaunchedEffect
        // Load activity for current and adjacent posts if they belong to the current user
        val prefetchRange = (currentPage - 1)..(currentPage + 1)
        prefetchRange.filter { it in posts.indices }.forEach { index ->
            val post = posts[index]
            if (post.author.id == currentUserId) {
                viewModel.loadPostActivity(post.id)
            }
        }
    }

    var activeInputOverlay by remember { mutableStateOf<FeedInputOverlayMode?>(null) }
    var messageText by remember { mutableStateOf("") }
    var reactionNote by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val feedScreenDensity = LocalDensity.current
    val imeInsets = WindowInsets.ime
    var hasInputOverlayKeyboardOpened by remember { mutableStateOf(false) }
    var lastInputOverlayKeyboardBottom by remember { mutableIntStateOf(0) }
    // When true, the overlay was dismissed via keyboard-close (back gesture);
    // skip the exit animation so the input bar disappears instantly.
    var isOverlayDismissedByKeyboard by remember { mutableStateOf(false) }

    fun dismissInputOverlay(fromKeyboard: Boolean = false) {
        isOverlayDismissedByKeyboard = fromKeyboard
        activeInputOverlay = null
        showEmojiPicker = false
        isInputOverlayVisible = false
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    fun showFeedback(message: String) {
        feedbackPillMessage = message
        feedbackPillVisible = true
        feedbackPillEventId += 1
    }

    fun downloadPost(post: Post) {
        coroutineScope.launch {
            saveFeedPostMediaToPictures(context, post)
                .onSuccess { showFeedback("Saved to Pictures/Solari") }
                .onFailure { error ->
                    showFeedback(error.message ?: "Failed to save media.")
                }
        }
    }

    fun sendReactionOptimistically(emoji: String) {
        val post = posts.getOrNull(pagerState.currentPage) ?: return
        val note = reactionNote.trim().takeIf { it.isNotEmpty() }
        reactionNote = ""
        dismissInputOverlay()
        viewModel.sendPostReaction(post, emoji, note) {}
    }

    fun sendMessageOptimistically() {
        val post = posts.getOrNull(pagerState.currentPage) ?: return
        val content = messageText.trim()
        if (content.isEmpty()) return

        messageText = ""
        dismissInputOverlay()
        viewModel.sendPostReply(post, content) {}
    }

    LaunchedEffect(activeInputOverlay) {
        hasInputOverlayKeyboardOpened = false
        lastInputOverlayKeyboardBottom = 0
    }

    LaunchedEffect(activeInputOverlay, feedScreenDensity) {
        if (activeInputOverlay == null) return@LaunchedEffect

        snapshotFlow { imeInsets.getBottom(feedScreenDensity) }
            .collect { keyboardBottom ->
                when {
                    hasInputOverlayKeyboardOpened &&
                            lastInputOverlayKeyboardBottom > 0 &&
                            keyboardBottom < lastInputOverlayKeyboardBottom -> {
                        dismissInputOverlay(fromKeyboard = true)
                    }

                    keyboardBottom > 0 -> {
                        hasInputOverlayKeyboardOpened = true
                        lastInputOverlayKeyboardBottom = keyboardBottom
                    }

                    hasInputOverlayKeyboardOpened -> dismissInputOverlay(fromKeyboard = true)
                }
            }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pendingPost = postPendingLegacyDownload
        postPendingLegacyDownload = null
        if (granted && pendingPost != null) {
            downloadPost(pendingPost)
        }
    }

    LaunchedEffect(isActivitySheetVisible, isInputOverlayVisible) {
        onActivityPanelVisibilityChanged(isActivitySheetVisible || isInputOverlayVisible)
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading) {
            isUserRefreshing = false
        }
    }

    LaunchedEffect(viewModel.successMessage, isFeedVisible) {
        val message = viewModel.successMessage ?: return@LaunchedEffect
        if (!isFeedVisible) {
            feedbackPillVisible = false
            feedbackPillMessage = ""
            viewModel.clearMessages()
            return@LaunchedEffect
        }

        showFeedback(message)
        delay(260)
        viewModel.clearMessages()
    }

    LaunchedEffect(feedbackPillEventId) {
        if (feedbackPillEventId > 0) {
            delay(1000)
            feedbackPillVisible = false
        }
    }

    BackHandler(enabled = activeInputOverlay != null) {
        dismissInputOverlay()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
    ) {
        if (posts.isEmpty()) {
            PullToRefreshBox(
                isRefreshing = isUserRefreshing,
                onRefresh = {
                    isUserRefreshing = true
                    viewModel.refresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!viewModel.isLoading) {
                                Text(
                                    text = viewModel.errorMessage ?: "No posts yet",
                                    color = SolariTheme.colors.onBackground,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isActivitySheetVisible && !isInputOverlayVisible,
                key = { page -> posts[page].id }
            ) { page ->
                val post = posts[page]
                val transitionPlaceholderUrl = post.thumbnailUrl
                    .takeIf { post.id == initialPostId && it.isNotBlank() }
                FeedPost(
                    post = post,
                    isActive = page == pagerState.currentPage,
                    enableMediaSharedTransition = isOpeningMediaSharedTransitionEnabled &&
                            post.id == initialPostId,
                    transitionPlaceholderUrl = transitionPlaceholderUrl,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onLongPress = { showMenuForPost = post },
                    onMoreClick = { showMenuForPost = post },
                    activityEntries = viewModel.postActivities[post.id].orEmpty(),
                    onShowActivity = {
                        activitySheetPostId = post.id
                        viewModel.loadPostActivity(post.id, force = true)
                        isActivitySheetVisible = true
                    },
                    onNavigateToBrowse = onNavigateToBrowse,
                    currentUser = currentUser,
                    onShowInputOverlay = {
                        isOverlayDismissedByKeyboard = false
                        isInputOverlayVisible = true
                        activeInputOverlay = it
                    },
                    reactionNote = reactionNote,
                    onReactionNoteChange = { reactionNote = it },
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onOpenEmojiPicker = { showEmojiPicker = true },
                    onSendReaction = ::sendReactionOptimistically,
                    onSendMessage = ::sendMessageOptimistically
                )
            }
        }

        val currentActivitySheetPostId = activitySheetPostId
        FeedActivitySheet(
            visible = isActivitySheetVisible,
            activities = currentActivitySheetPostId
                ?.let { viewModel.postActivities[it] }
                .orEmpty(),
            isLoading = currentActivitySheetPostId in viewModel.loadingPostActivityIds,
            onDismiss = { isActivitySheetVisible = false }
        )

        if (showMenuForPost != null) {
            val post = showMenuForPost!!
            Dialog(
                onDismissRequest = { showMenuForPost = null },
            ) {
                Surface(
                    color = SolariTheme.colors.surface,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier.width(220.dp)
                ) {
                    Column {
                        val canDeletePost = currentUser != null && post.author.id == currentUser.id
                        FeedPostActionButton(
                            text = "Download",
                            textColor = SolariTheme.colors.onSurface,
                            shape = if (canDeletePost) {
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            } else {
                                RoundedCornerShape(14.dp)
                            },
                            onClick = {
                                showMenuForPost = null
                                if (
                                    Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    postPendingLegacyDownload = post
                                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                } else {
                                    downloadPost(post)
                                }
                            }
                        )

                        if (canDeletePost) {
                            FeedPostActionButton(
                                text = "Delete",
                                textColor = SolariTheme.colors.error,
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 14.dp,
                                    bottomStart = 14.dp
                                ),
                                onClick = {
                                    postPendingDelete = post
                                    showMenuForPost = null
                                }
                            )
                        }
                    }
                }
            }
        }

        postPendingDelete?.let { post ->
            SolariConfirmationDialog(
                title = "Delete post?",
                message = "This post will be removed from your feed.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deletePost(post.id)
                    postPendingDelete = null
                },
                onDismiss = { postPendingDelete = null }
            )
        }

        AnimatedVisibility(
            visible = feedbackPillVisible,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 260),
                initialOffsetY = { -it * 2 }
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220),
                targetOffsetY = { -it * 2 }
            ) + fadeOut(animationSpec = tween(durationMillis = 160)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 24.dp, end = 24.dp)
        ) {
            FeedFeedbackPill(message = feedbackPillMessage)
        }

        // Reset keyboard-dismiss flag when overlay is shown again
        LaunchedEffect(activeInputOverlay) {
            if (activeInputOverlay != null) {
                isOverlayDismissedByKeyboard = false
            }
        }

        // Render overlay: if dismissed by keyboard back gesture, skip rendering
        // entirely (instant disappear). Otherwise use normal animated flow.
        if (activeInputOverlay != null && !isOverlayDismissedByKeyboard) {
            val overlayMode = activeInputOverlay!!
            val density = LocalDensity.current
            val keyboardBottom = WindowInsets.ime.getBottom(density)
            val keyboardBottomPadding = with(density) { keyboardBottom.toDp() }
            // Subtract the Scaffold bottom bar height from IME inset — our container's
            // bottom is already offset upward by the nav bar, so raw IME inset overshoots.
            val compensatedKeyboardPadding = if (keyboardBottom > 0) {
                (keyboardBottomPadding - FeedInputOverlayBottomBarCompensation).coerceAtLeast(0.dp)
            } else {
                0.dp
            }
            val focusRequester = remember { FocusRequester() }
            var isDimVisible by remember(overlayMode) { mutableStateOf(false) }
            val dimAlpha by animateFloatAsState(
                targetValue = if (isDimVisible) 0.45f else 0f,
                animationSpec = tween(durationMillis = 200),
                label = "feedInputOverlayDim"
            )

            LaunchedEffect(overlayMode) {
                isDimVisible = true
                focusRequester.requestFocus()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10f)
                    .background(Color.Black.copy(alpha = dimAlpha))
                    .pointerInput(Unit) {
                        detectTapGestures { dismissInputOverlay() }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(bottom = compensatedKeyboardPadding + FeedInputOverlayKeyboardGap)
                ) {
                    when (overlayMode) {
                        FeedInputOverlayMode.Reaction -> {
                            FeedReactionField(
                                value = reactionNote,
                                onValueChange = { reactionNote = it.take(20) },
                                onReact = ::sendReactionOptimistically,
                                onOpenEmojiPicker = { showEmojiPicker = true },
                                isEditable = true,
                                focusRequester = focusRequester,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        FeedInputOverlayMode.Message -> {
                            FeedMessageField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                onSend = ::sendMessageOptimistically,
                                isEditable = true,
                                focusRequester = focusRequester,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        if (showEmojiPicker) {
            EmojiPickerPopup(
                onClosed = { showEmojiPicker = false },
                selectedEmoji = null,
                recentEmojis = emptyList(),
                onReact = ::sendReactionOptimistically
            )
        }
    }
}

private suspend fun saveFeedPostMediaToPictures(
    context: Context,
    post: Post
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val mediaUrl = post.imageUrl.ifBlank { post.thumbnailUrl }
        require(mediaUrl.isNotBlank()) { "No media URL found." }

        val mediaUri = mediaUrl.toUri()
        val isVideo = post.isVideoMedia()
        val mimeType = context.resolveFeedPostMimeType(mediaUri, post.mediaType, isVideo)
        val extension = mimeType.toFileExtension(mediaUri, isVideo)
        val fileName = buildFeedDownloadFileName(post, extension)
        val mediaBytes = context.readFeedMediaBytes(mediaUri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = if (isVideo) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val outputUri = context.contentResolver.insert(
                collection,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/Solari"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            ) ?: throw IllegalStateException("Failed to create destination file.")

            try {
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    output.write(mediaBytes)
                } ?: throw IllegalStateException("Failed to write media.")

                context.contentResolver.update(
                    outputUri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null
                )
            } catch (error: Throwable) {
                context.contentResolver.delete(outputUri, null, null)
                throw error
            }
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Solari"
            ).apply { mkdirs() }
            val outputFile = File(directory, fileName)
            outputFile.outputStream().use { output ->
                output.write(mediaBytes)
            }
            MediaScannerConnection.scanFile(
                context,
                arrayOf(outputFile.absolutePath),
                arrayOf(mimeType),
                null
            )
        }

        Unit
    }
}

private fun Context.readFeedMediaBytes(uri: Uri): ByteArray {
    return when (uri.scheme?.lowercase()) {
        "content", "android.resource" -> {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Failed to read media.")
        }

        "file" -> File(requireNotNull(uri.path) { "Missing media path." }).readBytes()
        "http", "https" -> URL(uri.toString()).openConnection().run {
            connectTimeout = 15_000
            readTimeout = 30_000
            getInputStream().use { it.readBytes() }
        }

        else -> throw IllegalStateException("Unsupported media URL.")
    }
}

private fun Context.resolveFeedPostMimeType(
    uri: Uri,
    mediaType: String,
    isVideo: Boolean
): String {
    val normalizedMediaType = mediaType.trim().lowercase()
    if (normalizedMediaType.contains('/')) {
        return normalizedMediaType
    }

    val resolverMimeType = when (uri.scheme?.lowercase()) {
        "content", "android.resource" -> contentResolver.getType(uri)
        else -> null
    }
    if (!resolverMimeType.isNullOrBlank()) {
        return resolverMimeType
    }

    val extension = uri.lastPathSegment
        ?.substringBefore('?')
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.lowercase()
        .orEmpty()
    val extensionMimeType = extension
        .takeIf { it.isNotBlank() }
        ?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
    if (!extensionMimeType.isNullOrBlank()) {
        return extensionMimeType
    }

    return if (isVideo) "video/mp4" else "image/jpeg"
}

private fun String.toFileExtension(uri: Uri, isVideo: Boolean): String {
    val mimeExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(this)
    if (!mimeExtension.isNullOrBlank()) {
        return mimeExtension
    }

    val pathExtension = uri.lastPathSegment
        ?.substringBefore('?')
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.lowercase()
        .orEmpty()
    return pathExtension.ifBlank { if (isVideo) "mp4" else "jpg" }
}

private fun buildFeedDownloadFileName(post: Post, extension: String): String {
    val username = post.author.username
        .ifBlank { post.author.displayName }
        .toSafeFeedFileNamePart()
        .ifBlank { "solari_user" }
    val idPart = post.id
        .take(12)
        .toSafeFeedFileNamePart()
        .ifBlank { "post" }
    return "${username}_${post.timestamp}_${idPart}.$extension"
}

private fun String.toSafeFeedFileNamePart(): String {
    return trim()
        .lowercase()
        .replace(Regex("[^a-z0-9_-]+"), "_")
        .trim('_')
}

@Composable
private fun FeedFeedbackPill(message: String) {
    Surface(
        color = SolariTheme.colors.onSuccess,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SolariTheme.colors.success,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                color = SolariTheme.colors.onBackground,

                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FeedPostActionButton(
    text: String,
    textColor: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    androidx.compose.animation.ExperimentalSharedTransitionApi::class
)
@Composable
private fun FeedPost(
    post: Post,
    isActive: Boolean,
    enableMediaSharedTransition: Boolean,
    transitionPlaceholderUrl: String?,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onLongPress: () -> Unit,
    onMoreClick: () -> Unit,
    activityEntries: List<PostActivityEntry>,
    onShowActivity: () -> Unit,
    onNavigateToBrowse: (String?) -> Unit,
    currentUser: User?,
    onShowInputOverlay: (FeedInputOverlayMode) -> Unit,
    reactionNote: String,
    onReactionNoteChange: (String) -> Unit,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onSendReaction: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    val currentUserId = currentUser?.id
    val isCurrentUserPost = post.author.id == currentUserId
    val displayAuthor = currentUser?.takeIf { post.author.id == it.id } ?: post.author
    val activityUsers = remember(activityEntries, currentUserId) {
        activityEntries
            .map { it.user }
            .distinctBy { it.id }
            .filter { it.id != currentUserId }
    }

    var isZooming by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()
    val zoomScale = remember { Animatable(1f) }
    val zoomRotation = remember { Animatable(0f) }
    val zoomOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isPostChromeVisible by remember(post.id) { mutableStateOf(false) }

    LaunchedEffect(post.id) {
        isPostChromeVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .zIndex(if (isZooming) 10f else 0f)
                    .onGloballyPositioned { size = it.size }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            var didTransform = false

                            while (true) {
                                val event = awaitPointerEvent()
                                val pressedChanges = event.changes.filter { it.pressed }
                                if (pressedChanges.isEmpty()) break
                                if (pressedChanges.size < 2) continue

                                val zoom = event.calculateZoom()
                                val rotate = event.calculateRotation()
                                val centroid = event.calculateCentroid(useCurrent = true)
                                val oldScale = zoomScale.value
                                val newScale = (oldScale * zoom).coerceIn(1f, 5f)

                                if (newScale > 1f || oldScale > 1f) {
                                    val scaleFactor = newScale / oldScale
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val newOffset = zoomOffset.value * scaleFactor +
                                            (centroid - center) * (1 - scaleFactor)

                                    coroutineScope.launch {
                                        zoomScale.snapTo(newScale)
                                        zoomRotation.snapTo(zoomRotation.value + rotate)
                                        zoomOffset.snapTo(newOffset)
                                    }
                                    isZooming = true
                                    didTransform = true
                                    pressedChanges.forEach { it.consume() }
                                }
                            }

                            if (didTransform) {
                                coroutineScope.launch {
                                    isZooming = false
                                    launch { zoomScale.animateTo(1f, tween(300)) }
                                    launch { zoomRotation.animateTo(0f, tween(300)) }
                                    launch { zoomOffset.animateTo(Offset.Zero, tween(300)) }
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })

                            coroutineScope.launch {
                                isZooming = false
                                launch { zoomScale.animateTo(1f, tween(300)) }
                                launch { zoomRotation.animateTo(0f, tween(300)) }
                                launch { zoomOffset.animateTo(Offset.Zero, tween(300)) }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = zoomScale.value
                        scaleY = zoomScale.value
                        rotationZ = zoomRotation.value
                        translationX = zoomOffset.value.x
                        translationY = zoomOffset.value.y
                        clip = true
                        shape = RoundedCornerShape(FeedPostMediaCornerRadius)
                    }
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                        onLongClick = onLongPress
                    )
            ) {
                val mediaModifier = if (enableMediaSharedTransition) {
                    with(sharedTransitionScope) {
                        Modifier
                            .fillMaxSize()
                            .sharedBounds(
                                rememberSharedContentState(key = "post_media_${post.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ ->
                                    tween(
                                        durationMillis = FeedSharedMediaTransitionMillis,
                                        easing = FastOutSlowInEasing
                                    )
                                },
                                resizeMode = androidx.compose.animation.SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                clipInOverlayDuringTransition = OverlayClip(
                                    RoundedCornerShape(FeedSharedMediaCornerRadius)
                                )
                            )
                    }
                } else {
                    Modifier.fillMaxSize()
                }

                Box(
                    modifier = mediaModifier
                        .clip(RoundedCornerShape(FeedPostMediaCornerRadius))
                ) {
                    if (post.isVideoMedia()) {
                        FeedVideoPlayer(
                            url = post.imageUrl,
                            placeholderUrl = transitionPlaceholderUrl,
                            mediaType = post.mediaType,
                            isActive = isActive,
                            onLongPress = onLongPress,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        FeedImage(
                            url = post.imageUrl,
                            placeholderUrl = transitionPlaceholderUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                this@Column.AnimatedVisibility(
                    visible = isPostChromeVisible && !isZooming,
                    enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    with(animatedVisibilityScope) {
                        Box(
                            modifier = Modifier
                                .animateEnterExit(
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = fadeOut(animationSpec = tween(500))
                                )
                                .padding(10.dp)
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SolariTheme.colors.background.copy(alpha = 0.7f))
                                .clickable(onClick = onMoreClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = SolariTheme.colors.onBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                    }
                }

                if (post.hasDisplayableFeedCaption()) {
                    this@Column.AnimatedVisibility(
                        visible = isPostChromeVisible && !isZooming,
                        enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            FeedCaptionPill(post = post)
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isPostChromeVisible && !isZooming,
                enter = fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = fadeOut(),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    with(animatedVisibilityScope) {
                        // Author block
                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier
                                .animateEnterExit(
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = fadeOut(animationSpec = tween(500))
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigateToBrowse(displayAuthor.id) }
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SolariAvatar(
                                imageUrl = displayAuthor.profileImageUrl,
                                username = displayAuthor.username,
                                contentDescription = "Author Avatar",
                                modifier = Modifier
                                    .size(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isCurrentUserPost) "You" else displayAuthor.displayName,
                                    color = SolariTheme.colors.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = post.timestamp.toFeedRelativeTimeLabel(),
                                    color = SolariTheme.colors.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        // Content section: activity/inputs
                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = fadeOut(animationSpec = tween(500))
                            )
                        ) {
                            if (isCurrentUserPost) {
                                when (post.uploadStatus) {
                                    PostUploadStatus.None -> {
                                        FeedActivityPill(
                                            users = activityUsers.take(3),
                                            overflowCount = (activityUsers.size - 3).coerceAtLeast(0),
                                            onClick = onShowActivity
                                        )
                                    }

                                    PostUploadStatus.Uploading,
                                    PostUploadStatus.Processing -> {
                                        FeedUploadStatusPill(
                                            text = "Uploading post",
                                            isLoading = true,
                                            isError = false
                                        )
                                    }

                                    PostUploadStatus.Failed -> {
                                        FeedUploadStatusPill(
                                            text = post.uploadError ?: "Upload failed",
                                            isLoading = false,
                                            isError = true
                                        )
                                    }
                                }
                            } else {
                                Column {
                                    FeedReactionField(
                                        value = reactionNote,
                                        onValueChange = onReactionNoteChange,
                                        onReact = onSendReaction,
                                        onOpenEmojiPicker = onOpenEmojiPicker,
                                        isEditable = false,
                                        onActivate = { onShowInputOverlay(FeedInputOverlayMode.Reaction) }
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    FeedMessageField(
                                        value = messageText,
                                        onValueChange = onMessageTextChange,
                                        onSend = onSendMessage,
                                        isEditable = false,
                                        onActivate = { onShowInputOverlay(FeedInputOverlayMode.Message) }
                                    )
                                }
                            }
                        }

                        // Browse button
                        Spacer(modifier = Modifier.weight(1f))

                        FeedBrowseButton(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = fadeOut(animationSpec = tween(500))
                            ),
                            onClick = { onNavigateToBrowse(null) }
                        )

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedUploadStatusPill(
    text: String,
    isLoading: Boolean,
    isError: Boolean
) {
    Row(
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(if (isError) SolariTheme.colors.onSurfaceVariant.copy(alpha = 0.2f) else SolariTheme.colors.surfaceVariant)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = SolariTheme.colors.onBackground,
                trackColor = Color.Transparent,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
            )

            Spacer(modifier = Modifier.width(12.dp))
        }

        Text(
            text = text,
            color = if (isError) SolariTheme.colors.error else SolariTheme.colors.onBackground,
            fontSize = 18.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeedActivityPill(
    users: List<User>,
    overflowCount: Int,
    onClick: () -> Unit
) {
    val visibleUsers = users.take(3)
    val showOverflow = overflowCount > 0
    val avatarItemCount = visibleUsers.size + if (showOverflow) 1 else 0
    val avatarStackWidth = if (avatarItemCount == 0) {
        0.dp
    } else {
        36.dp + ((avatarItemCount - 1) * 23).dp
    }

    Row(
        modifier = Modifier
            .height(64.dp)
            .scaledClickable(pressedScale = 1.05f, onClick = onClick)
            .clip(RoundedCornerShape(36.dp))
            .background(SolariTheme.colors.surfaceVariant)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Activity",
            color = SolariTheme.colors.onBackground,
            fontSize = 18.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )

        if (avatarItemCount > 0) {
            Spacer(modifier = Modifier.width(16.dp))
        }

        Box(
            modifier = Modifier
                .width(avatarStackWidth)
                .height(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            visibleUsers.forEachIndexed { index, user ->
                SolariAvatar(
                    imageUrl = user.profileImageUrl,
                    username = user.username,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (index * 23).dp)
                        .size(36.dp),
                    shape = CircleShape,
                    fontSize = 14.sp,
                    backgroundColor = lerp(SolariTheme.colors.surfaceVariant, Color.White, 0.1f)
                )
            }

            if (showOverflow) {
                Box(
                    modifier = Modifier
                        .offset(x = (visibleUsers.size * 23).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SolariTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$overflowCount",
                        color = SolariTheme.colors.onBackground,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedCaptionPill(post: Post, modifier: Modifier = Modifier) {
    val metadata = post.resolvedCaptionMetadata()
    if (post.caption.isEmpty() && metadata == null) return

    if (metadata == null || metadata is CaptionMetadata.Text) {
        val text = if (metadata is CaptionMetadata.Text) metadata.data ?: post.caption else post.caption
        if (text.isEmpty()) return
        Text(
            text = text,
            color = SolariTheme.colors.onBackground,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontFamily = PlusJakartaSans,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .widthIn(max = 280.dp)
                .wrapContentWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SolariTheme.colors.background.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        return
    }

    when (metadata) {
        is CaptionMetadata.Ootd -> {
            Row(
                modifier = modifier
                    .widthIn(max = 280.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolariTheme.colors.background.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.solari.app.R.drawable.glasses),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "OOTD",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        is CaptionMetadata.Weather -> {
            val iconMap = mapOf("Sunny" to "☀️", "Cloudy" to "☁️", "Cool" to "❄️", "Cold" to "🥶", "Rainy" to "🌧️", "Snowy" to "🌨️", "Windy" to "💨", "Stormy" to "⛈️")
            Row(
                modifier = modifier
                    .widthIn(max = 280.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(androidx.compose.ui.graphics.Color(0xFF00ACC1).copy(alpha = 0.85f)) // Cyan for Weather
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = iconMap[metadata.condition] ?: "☀️", fontSize = 16.sp)
                if (metadata.temperatureC != null) {
                    Text(
                        text = "${metadata.temperatureC.formatCaptionTemperature()}°C",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        is CaptionMetadata.Location -> {
            Row(
                modifier = modifier
                    .widthIn(max = 280.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolariTheme.colors.background.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.solari.app.R.drawable.location),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = metadata.placeName,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        is CaptionMetadata.Clock -> {
            Row(
                modifier = modifier
                    .widthIn(max = 280.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolariTheme.colors.background.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(com.solari.app.R.drawable.clock),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = metadata.time,
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 14.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        is CaptionMetadata.Rating -> {
            Row(
                modifier = modifier
                    .widthIn(max = 280.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolariTheme.colors.background.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (i in 1..5) {
                        val fraction = (metadata.starRating - (i - 1)).coerceIn(0f, 1f)
                        Box(modifier = Modifier.size(16.dp)) {
                            Icon(
                                imageVector = Icons.Default.StarOutline,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFFFC107).copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxSize()
                            )
                            if (fraction > 0f) {
                                Icon(
                                    imageVector = if (fraction >= 1f) Icons.Default.Star else Icons.AutoMirrored.Filled.StarHalf,
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFFFFC107),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                if (!metadata.review.isNullOrBlank()) {
                    Text(
                        text = " - \"${metadata.review}\"",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp,
                        fontFamily = PlusJakartaSans,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        else -> {}
    }
}

private val FeedWeatherCaptionConditions = listOf(
    "Sunny",
    "Cloudy",
    "Cool",
    "Cold",
    "Rainy",
    "Snowy",
    "Windy",
    "Stormy"
)

private val FeedStyledCaptionTypes = setOf("ootd", "weather", "location", "clock", "rating")
private val FeedWeatherTemperaturePattern = Regex("""(-?\d+(?:\.\d+)?)\s*°?\s*C""")
private val FeedRatingPattern = Regex("""Rating:\s*([0-5](?:\.\d+)?)""", RegexOption.IGNORE_CASE)

private fun Post.hasDisplayableFeedCaption(): Boolean {
    return caption.isNotEmpty() || resolvedCaptionMetadata()?.let { it !is CaptionMetadata.Text } == true
}

private fun Post.withCaptionFieldsFromFallback(fallback: Post): Post {
    if (hasStyledCaptionFields() || !fallback.hasStyledCaptionFields()) return this

    return copy(
        caption = caption.ifBlank { fallback.caption },
        captionType = fallback.captionType,
        captionMetadata = fallback.captionMetadata
    )
}

private fun Post.hasStyledCaptionFields(): Boolean {
    return captionMetadata?.let { it !is CaptionMetadata.Text } == true ||
            captionType.normalizedFeedCaptionType() in FeedStyledCaptionTypes
}

private fun Post.resolvedCaptionMetadata(): CaptionMetadata? {
    captionMetadata?.let { return it }

    val normalizedType = captionType.normalizedFeedCaptionType()
    val trimmedCaption = caption.trim()
    return when (normalizedType) {
        "ootd" -> CaptionMetadata.Ootd
        "weather" -> {
            val condition = FeedWeatherCaptionConditions.firstOrNull { condition ->
                trimmedCaption.contains(condition, ignoreCase = true)
            } ?: trimmedCaption
                .replace(FeedWeatherTemperaturePattern, "")
                .trim()
                .takeIf { it.isNotEmpty() }
                ?: "Weather"
            val temperatureC = FeedWeatherTemperaturePattern
                .find(trimmedCaption)
                ?.groupValues
                ?.getOrNull(1)
                ?.toFloatOrNull()

            CaptionMetadata.Weather(condition, temperatureC)
        }
        "location" -> trimmedCaption
            .takeIf { it.isNotEmpty() }
            ?.let(CaptionMetadata::Location)
        "clock" -> trimmedCaption
            .removePrefix("⏱️")
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.let(CaptionMetadata::Clock)
        "rating" -> {
            val rating = FeedRatingPattern
                .find(trimmedCaption)
                ?.groupValues
                ?.getOrNull(1)
                ?.toFloatOrNull()
                ?: return null
            val review = trimmedCaption
                .substringAfter(" - ", missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }

            CaptionMetadata.Rating(rating, review)
        }
        else -> trimmedCaption.toGeneratedCaptionMetadataOrNull()
    }
}

private fun String.normalizedFeedCaptionType(): String = trim().lowercase()

private fun String.toGeneratedCaptionMetadataOrNull(): CaptionMetadata? {
    if (isEmpty()) return null

    val captionWithoutSunglasses = replace("🕶️", "")
        .replace("🕶", "")
        .trim()
    if (captionWithoutSunglasses.equals("OOTD", ignoreCase = true)) {
        return CaptionMetadata.Ootd
    }

    FeedRatingPattern.find(this)?.let { match ->
        val rating = match.groupValues.getOrNull(1)?.toFloatOrNull() ?: return@let
        val review = substringAfter(" - ", missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
        return CaptionMetadata.Rating(rating, review)
    }

    val weatherCondition = FeedWeatherCaptionConditions.firstOrNull { condition ->
        contains(condition, ignoreCase = true)
    }
    if (weatherCondition != null) {
        val temperatureC = FeedWeatherTemperaturePattern
            .find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?.toFloatOrNull()
        return CaptionMetadata.Weather(weatherCondition, temperatureC)
    }

    val clockCaption = removePrefix("⏱️")
        .removePrefix("⏱")
        .trim()
    if (clockCaption != this && clockCaption.isNotEmpty()) {
        return CaptionMetadata.Clock(clockCaption)
    }

    return null
}

private fun Float.formatCaptionTemperature(): String {
    val rounded = roundToInt()
    return if (this == rounded.toFloat()) rounded.toString() else toString()
}

@Composable
private fun FeedActivitySheet(
    visible: Boolean,
    activities: List<PostActivityEntry>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    var isSheetDragging by remember { mutableStateOf(false) }
    var sheetDragOffsetPx by remember { mutableFloatStateOf(0f) }
    var expandedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val groupedActivities = remember(activities) {
        activities
            .groupBy { it.user.id }
            .map { (_, userActivities) ->
                FeedActivityUserGroup(
                    user = userActivities.first().user,
                    activities = userActivities
                )
            }
    }
    val animatedSheetDragOffsetPx by animateFloatAsState(
        targetValue = sheetDragOffsetPx,
        animationSpec = tween(durationMillis = if (isSheetDragging) 0 else 180),
        label = "feedActivitySheetDragOffset"
    )

    LaunchedEffect(visible) {
        if (visible) {
            sheetDragOffsetPx = 0f
            expandedUserIds = emptySet()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SolariTheme.colors.background.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = dismissInteractionSource,
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 260),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 220),
                targetOffsetY = { it }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.68f)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = animatedSheetDragOffsetPx.roundToInt()
                        )
                    }
                    .clip(RoundedCornerShape(topStart = 29.dp, topEnd = 29.dp))
                    .background(SolariTheme.colors.surface)
                    .clickable(
                        interactionSource = sheetInteractionSource,
                        indication = null,
                        onClick = {}
                    )
                    .padding(top = 22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(58.dp)
                        .height(18.dp)
                        .pointerInput(onDismiss) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    sheetDragOffsetPx = (sheetDragOffsetPx + dragAmount)
                                        .coerceAtLeast(0f)
                                },
                                onDragEnd = {
                                    if (sheetDragOffsetPx > 80f) {
                                        onDismiss()
                                    } else {
                                        sheetDragOffsetPx = 0f
                                    }
                                },
                                onDragCancel = {
                                    sheetDragOffsetPx = 0f
                                }
                            )
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(35.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SolariTheme.colors.onSurface.copy(alpha = 0.28f))
                    )
                }

                Text(
                    text = "Activity",
                    color = SolariTheme.colors.onSurface,
                    fontSize = 19.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 22.dp, bottom = 16.dp)
                )

                when {
                    isLoading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = SolariTheme.colors.primary,
                            trackColor = SolariTheme.colors.surfaceVariant
                        )
                    }

                    activities.isEmpty() -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activity yet",
                            color = SolariTheme.colors.onSurface.copy(alpha = 0.68f),
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(start = 19.dp, end = 19.dp, bottom = 26.dp)
                    ) {
                        items(groupedActivities, key = { it.user.id }) { group ->
                            val isExpanded = group.user.id in expandedUserIds
                            FeedActivityGroupItem(
                                group = group,
                                isExpanded = isExpanded,
                                onToggleExpanded = {
                                    if (group.activities.size <= 1) return@FeedActivityGroupItem
                                    expandedUserIds = if (isExpanded) {
                                        expandedUserIds - group.user.id
                                    } else {
                                        expandedUserIds + group.user.id
                                    }
                                },
                                onCollapse = {
                                    expandedUserIds = expandedUserIds - group.user.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedActivityGroupItem(
    group: FeedActivityUserGroup,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onCollapse: () -> Unit
) {
    val canExpand = group.activities.size > 1

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        FeedActivityGroupRootRow(
            group = group,
            isExpanded = isExpanded,
            canExpand = canExpand,
            onClick = onToggleExpanded
        )

        AnimatedVisibility(visible = canExpand && isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                group.activities.forEach { activity ->
                    FeedActivityItem(activity = activity)
                }

                Surface(
                    color = SolariTheme.colors.surfaceVariant.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .scaledClickable(pressedScale = 1.08f, onClick = onCollapse)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Collapse",
                            color = SolariTheme.colors.onSurface,
                            fontSize = 12.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = null,
                            tint = SolariTheme.colors.onSurface.copy(alpha = 0.76f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedActivityGroupRootRow(
    group: FeedActivityUserGroup,
    isExpanded: Boolean,
    canExpand: Boolean,
    onClick: () -> Unit
) {
    val latestActivity = group.activities.first()
    val summary = if (canExpand) {
        "${group.activities.size} activities"
    } else {
        latestActivity.description
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .scaledClickable(
                pressedScale = 1.04f,
                enabled = canExpand,
                onClick = onClick
            )
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolariAvatar(
            imageUrl = group.user.profileImageUrl,
            username = group.user.username,
            contentDescription = "${group.user.displayName} avatar",
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.user.displayName,
                color = SolariTheme.colors.onSurface,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = summary,
                color = SolariTheme.colors.onSurface.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            )
        }

        if (latestActivity.caption != null || latestActivity.emoji != null) {
            FeedActivityTrailing(activity = latestActivity)
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (canExpand) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse activities" else "Expand activities",
                tint = SolariTheme.colors.onSurface.copy(alpha = 0.72f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FeedActivityItem(activity: PostActivityEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SolariAvatar(
            imageUrl = activity.user.profileImageUrl,
            username = activity.user.username,
            contentDescription = "${activity.user.displayName} avatar",
            modifier = Modifier
                .size(42.dp),
            shape = CircleShape,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(13.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.user.displayName,
                color = SolariTheme.colors.onSurface,
                fontSize = 14.sp,
                lineHeight = 16.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = activity.description,
                color = SolariTheme.colors.onSurface.copy(alpha = 0.68f),
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium
            )
        }

        FeedActivityTrailing(activity = activity)
    }
}

@Composable
private fun FeedActivityTrailing(activity: PostActivityEntry) {
    Row(
        modifier = Modifier.widthIn(max = 102.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (activity.caption != null) {
            Text(
                text = activity.caption,
                color = SolariTheme.colors.secondary,
                fontSize = 12.sp,
                lineHeight = 13.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        activity.emoji?.let { emoji ->
            Text(
                text = emoji,
                fontSize = 22.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun FeedImage(
    url: String,
    placeholderUrl: String?,
    modifier: Modifier = Modifier
) {
    var isLoading by remember(url) { mutableStateOf(true) }

    Box(
        modifier = modifier
    ) {
        if (placeholderUrl != null && isLoading) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(placeholderUrl)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .build(),
            contentDescription = "Post Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false }
        )

        if (isLoading && placeholderUrl == null) {
            FeedMediaLoadingIndicator()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeedVideoPlayer(
    url: String,
    placeholderUrl: String?,
    mediaType: String,
    isActive: Boolean,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cachedVideoUri = rememberFeedCachedMediaUri(url)
    val cachedPlaceholderUri = if (placeholderUrl != null) {
        rememberFeedCachedMediaUri(placeholderUrl)
    } else {
        null
    }
    var isLoading by remember(url) { mutableStateOf(true) }
    var isUserPaused by remember(url) { mutableStateOf(false) }
    val videoAlpha by animateFloatAsState(
        targetValue = if (isLoading && placeholderUrl != null) 0f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "feedVideoAlpha"
    )

    val player = remember(cachedVideoUri, mediaType) {
        if (cachedVideoUri == null) {
            return@remember null
        }
        ExoPlayer.Builder(context)
            .setVideoChangeFrameRateStrategy(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                setMediaItem(
                    MediaItem.Builder()
                        .setUri(cachedVideoUri)
                        .setMimeType(mediaType.toMedia3VideoMimeType())
                        .build()
                )
                prepare()
            }
    }

    LaunchedEffect(isActive) {
        if (!isActive) {
            isUserPaused = false
        }
    }

    LaunchedEffect(player, isActive, isUserPaused) {
        if (player == null) return@LaunchedEffect
        player.volume = 0f
        player.playWhenReady = isActive && !isUserPaused
    }

    DisposableEffect(player) {
        if (player == null) {
            onDispose {}
        } else {
            val listener = object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isLoading = playbackState == Player.STATE_BUFFERING ||
                            playbackState == Player.STATE_IDLE
                }

                override fun onRenderedFirstFrame() {
                    isLoading = false
                }

                override fun onPlayerError(error: PlaybackException) {
                    isLoading = false
                }
            }

            player.addListener(listener)
            isLoading = player.playbackState == Player.STATE_BUFFERING ||
                    player.playbackState == Player.STATE_IDLE

            onDispose {
                player.removeListener(listener)
            }
        }
    }

    DisposableEffect(player) {
        onDispose {
            player?.release()
        }
    }

    Box(
        modifier = modifier
    ) {
        if (placeholderUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(cachedPlaceholderUri)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .alpha(videoAlpha),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    useController = false
                    controllerAutoShow = false
                    hideController()
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    this.player = player
                }
            },
            update = { playerView ->
                playerView.useController = false
                playerView.controllerAutoShow = false
                playerView.hideController()
                if (player != null && playerView.player !== player) {
                    playerView.player = player
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isUserPaused = !isUserPaused },
                    onLongClick = onLongPress
                )
        )

        if ((isLoading || cachedVideoUri == null) && placeholderUrl == null) {
            FeedMediaLoadingIndicator()
        }
    }
}

@Composable
private fun FeedMediaLoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background.copy(alpha = 0.26f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = SolariTheme.colors.primary,
            trackColor = SolariTheme.colors.surface
        )
    }
}

private fun Post.isVideoMedia(): Boolean {
    val normalizedMediaType = mediaType.lowercase()
    val normalizedUrl = imageUrl.substringBefore("?").lowercase()
    return normalizedMediaType.startsWith("video/") ||
            normalizedMediaType == "video" ||
            normalizedUrl.endsWith(".mp4") ||
            normalizedUrl.endsWith(".mov") ||
            normalizedUrl.endsWith(".m4v") ||
            normalizedUrl.endsWith(".webm")
}

private fun Post.feedMediaPrefetchUrls(): List<String> {
    return buildList {
        imageUrl.takeIf(String::isNotBlank)?.let(::add)
        thumbnailUrl
            .takeIf { it.isNotBlank() && it != imageUrl }
            ?.let(::add)
    }
}

@Composable
private fun rememberFeedCachedMediaUri(url: String): Uri? {
    val context = LocalContext.current
    var mediaUri by remember(url) {
        mutableStateOf(
            PersistentMediaCache.peekMemory(
                url = url,
                kind = PersistentMediaCacheKind.FeedMedia
            )
        )
    }

    LaunchedEffect(url) {
        mediaUri = if (url.isBlank()) {
            null
        } else {
            PersistentMediaCache.resolve(
                context = context,
                url = url,
                kind = PersistentMediaCacheKind.FeedMedia
            ).getOrElse {
                url.toUri()
            }
        }
    }

    return mediaUri
}

private fun String.toMedia3VideoMimeType(): String {
    val normalized = lowercase()
    return when {
        normalized.startsWith("video/") -> this
        normalized.contains("webm") -> MimeTypes.VIDEO_WEBM
        normalized.contains("quicktime") || normalized.contains("mov") -> "video/quicktime"
        else -> MimeTypes.VIDEO_MP4
    }
}

private fun Long.toFeedRelativeTimeLabel(nowMillis: Long = System.currentTimeMillis()): String {
    val elapsedMillis = (nowMillis - this).coerceAtLeast(0L)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
    val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis)

    return when {
        minutes < 1 -> "JUST NOW"
        minutes < 60 -> "${minutes}M AGO"
        hours < 24 -> "${hours}H AGO"
        days < 7 -> "${days}D AGO"
        days < 30 -> "${days / 7}W AGO"
        days < 365 -> "${days / 30}MO AGO"
        else -> "${days / 365}Y AGO"
    }
}

@Composable
private fun FeedReactionField(
    value: String,
    onValueChange: (String) -> Unit,
    onReact: (String) -> Unit,
    onOpenEmojiPicker: () -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
    focusRequester: FocusRequester? = null,
    onActivate: () -> Unit = {}
) {
    val quickReactionEmojis = listOf("❤️", "😂", "😮")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(
                if (isEditable) {
                    Modifier
                } else {
                    Modifier.scaledClickable(pressedScale = 1.05f, onClick = onActivate)
                }
            )
            .clip(RoundedCornerShape(9.dp))
            .background(SolariTheme.colors.surface)
            .padding(start = 26.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textFieldModifier = Modifier
            .weight(1f)
            .padding(end = 8.dp)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEditable,
            modifier = textFieldModifier,
            textStyle = TextStyle(
                color = SolariTheme.colors.onBackground,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans
            ),
            cursorBrush = SolidColor(SolariTheme.colors.onBackground),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Send reaction...",
                            color = SolariTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans
                        )
                    }
                    innerTextField()
                }
            }
        )

        quickReactionEmojis.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .scaledClickable(pressedScale = 1.2f) { onReact(emoji) }
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .scaledClickable(pressedScale = 1.2f, onClick = onOpenEmojiPicker)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "More reactions",
                tint = SolariTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun FeedMessageField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
    focusRequester: FocusRequester? = null,
    onActivate: () -> Unit = {}
) {
    val barShape = RoundedCornerShape(9.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(
                if (isEditable) {
                    Modifier
                } else {
                    Modifier.scaledClickable(pressedScale = 1.05f, onClick = onActivate)
                }
            )
            .clip(barShape)
            .background(SolariTheme.colors.surface)
            .padding(start = 26.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textFieldModifier = Modifier
            .weight(1f)
            .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEditable,
            modifier = textFieldModifier,
            textStyle = TextStyle(
                color = SolariTheme.colors.onBackground,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans
            ),
            cursorBrush = SolidColor(SolariTheme.colors.onBackground),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Send message...",
                            color = SolariTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontFamily = PlusJakartaSans
                        )
                    }
                    innerTextField()
                }
            }
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send message",
            tint = SolariTheme.colors.primary,
            modifier = Modifier
                .size(28.dp)
                .then(
                    if (isEditable) {
                        Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onSend
                            )
                    } else {
                        Modifier
                    }
                )
        )
    }
}

@Composable
private fun FeedBrowseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .height(48.dp)
            .scaledClickable(pressedScale = 1.05f, onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(SolariTheme.colors.primary)
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "View all photos",
            color = SolariTheme.colors.onSurfaceVariant,
            fontSize = 16.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold
        )
    }
}
