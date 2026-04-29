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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.solari.app.R
import com.solari.app.ui.components.SolariConfirmationDialog
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostActivityEntry
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.models.User
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.FeedViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.request.ImageRequest
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private enum class FeedInputOverlayMode {
    Reaction,
    Message
}

private data class FeedActivityUserGroup(
    val user: User,
    val activities: List<PostActivityEntry>
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    initialPostId: String? = null,
    authorFilterIds: Set<String> = emptySet(),
    sortMode: String = "default",
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit,
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
    var feedbackPillEventId by remember { mutableStateOf(0) }
    var isInputOverlayVisible by remember { mutableStateOf(false) }
    var isUserRefreshing by remember { mutableStateOf(false) }
    val sourcePosts = viewModel.posts
    val posts = remember(sourcePosts, authorFilterIds, sortMode) {
        val filteredPosts = if (authorFilterIds.isEmpty()) {
            sourcePosts
        } else {
            sourcePosts.filter { it.author.id in authorFilterIds }
        }

        when (sortMode) {
            "newest" -> filteredPosts.sortedByDescending { it.timestamp }
            "oldest" -> filteredPosts.sortedBy { it.timestamp }
            else -> filteredPosts
        }
    }
    val currentUser = viewModel.currentUser
    val initialPostPage = remember(initialPostId, posts) {
        posts.indexOfFirst { it.id == initialPostId }.takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(initialPage = initialPostPage) { posts.size }

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

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pendingPost = postPendingLegacyDownload
        postPendingLegacyDownload = null
        if (granted && pendingPost != null) {
            downloadPost(pendingPost)
        }
    }

    LaunchedEffect(initialPostId, posts) {
        if (initialPostId == null || posts.isEmpty()) return@LaunchedEffect

        val requestedPostPage = posts.indexOfFirst { it.id == initialPostId }
        if (requestedPostPage >= 0 && pagerState.currentPage != requestedPostPage) {
            pagerState.scrollToPage(requestedPostPage)
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

    LaunchedEffect(isFeedVisible) {
        if (!isFeedVisible) {
            feedbackPillVisible = false
            feedbackPillMessage = ""
            viewModel.clearMessages()
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

    LaunchedEffect(pagerState.currentPage, posts, currentUser?.id) {
        val post = posts.getOrNull(pagerState.currentPage) ?: return@LaunchedEffect
        if (post.uploadStatus != PostUploadStatus.None) return@LaunchedEffect
        val currentUserId = currentUser?.id ?: return@LaunchedEffect
        if (post.author.id == currentUserId) {
            viewModel.loadPostActivity(post.id)
        } else {
            viewModel.registerPostView(post)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            feedbackPillVisible = false
            feedbackPillMessage = ""
            viewModel.clearMessages()
            onActivityPanelVisibilityChanged(false)
        }
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
                            when {
                                viewModel.isLoading -> CircularProgressIndicator(
                                    color = SolariTheme.colors.primary,
                                    trackColor = SolariTheme.colors.surface
                                )

                                else -> Text(
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
                userScrollEnabled = !isActivitySheetVisible && !isInputOverlayVisible
            ) { page ->
                FeedPost(
                    post = posts[page],
                    isActive = page == pagerState.currentPage,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onLongPress = { showMenuForPost = posts[page] },
                    onMoreClick = { showMenuForPost = posts[page] },
                    activityEntries = viewModel.postActivities[posts[page].id].orEmpty(),
                    onShowActivity = {
                        activitySheetPostId = posts[page].id
                        viewModel.loadPostActivity(posts[page].id, force = true)
                        isActivitySheetVisible = true
                    },
                    onSendPostReaction = { emoji, note, onSent ->
                        viewModel.sendPostReaction(posts[page], emoji, note, onSent)
                    },
                    onSendPostReply = { content, onSent ->
                        viewModel.sendPostReply(posts[page], content, onSent)
                    },
                    onNavigateToBrowse = onNavigateToBrowse,
                    onInputOverlayVisibilityChanged = { isVisible ->
                        isInputOverlayVisible = isVisible
                    },
                    currentUser = currentUser
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
    }
}

private suspend fun saveFeedPostMediaToPictures(
    context: Context,
    post: Post
): Result<Unit> = withContext(Dispatchers.IO) {
    runCatching {
        val mediaUrl = post.imageUrl.ifBlank { post.thumbnailUrl }
        require(mediaUrl.isNotBlank()) { "No media URL found." }

        val mediaUri = Uri.parse(mediaUrl)
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
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Solari")
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

@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
private fun FeedPost(
    post: Post,
    isActive: Boolean,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onLongPress: () -> Unit,
    onMoreClick: () -> Unit,
    activityEntries: List<PostActivityEntry>,
    onShowActivity: () -> Unit,
    onSendPostReaction: (String, String?, () -> Unit) -> Unit,
    onSendPostReply: (String, () -> Unit) -> Unit,
    onNavigateToBrowse: (String?) -> Unit,
    onInputOverlayVisibilityChanged: (Boolean) -> Unit,
    currentUser: User?
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var messageText by remember { mutableStateOf("") }
    var reactionNote by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var activeInputOverlay by remember { mutableStateOf<FeedInputOverlayMode?>(null) }
    val currentUserId = currentUser?.id
    val isCurrentUserPost = post.author.id == currentUserId
    val displayAuthor = currentUser?.takeIf { post.author.id == it.id } ?: post.author
    val activityUsers = remember(activityEntries, currentUserId) {
        activityEntries
            .map { it.user }
            .distinctBy { it.id }
            .filter { it.id != currentUserId }
    }

    fun dismissInputOverlay() {
        activeInputOverlay = null
        showEmojiPicker = false
        onInputOverlayVisibilityChanged(false)
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    fun showInputOverlay(mode: FeedInputOverlayMode) {
        activeInputOverlay = mode
        onInputOverlayVisibilityChanged(true)
    }

    fun sendReactionOptimistically(emoji: String) {
        val note = reactionNote.trim().takeIf { it.isNotEmpty() }
        reactionNote = ""
        dismissInputOverlay()
        onSendPostReaction(emoji, note) {}
    }

    fun sendMessageOptimistically() {
        val content = messageText.trim()
        if (content.isEmpty()) return

        messageText = ""
        dismissInputOverlay()
        onSendPostReply(content) {}
    }

    var isZooming by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.weight(0.55f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(if (isZooming) RoundedCornerShape(0.dp) else RoundedCornerShape(14.dp))
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongPress
                    )
                    .zIndex(if (isZooming) 10f else 0f)
            ) {
                if (post.isVideoMedia()) {
                    FeedVideoPlayer(
                        url = post.imageUrl,
                        mediaType = post.mediaType,
                        postId = post.id,
                        isActive = isActive,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onLongPress = onLongPress,
                        onZoomStateChanged = { isZooming = it },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FeedImage(
                        url = post.imageUrl,
                        postId = post.id,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onZoomStateChanged = { isZooming = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isZooming,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    with(animatedVisibilityScope) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .animateEnterExit(
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = fadeOut(animationSpec = tween(500))
                                )
                                .background(SolariTheme.colors.onSurface.copy(alpha = 0.18f))
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .animateEnterExit(
                                    enter = fadeIn(animationSpec = tween(500)),
                                    exit = fadeOut(animationSpec = tween(500))
                                )
                                .padding(10.dp)
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SolariTheme.colors.background.copy(alpha = 0.36f))
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

                        if (!post.caption.isEmpty()) {
                            Text(
                                text = post.caption,
                                color = SolariTheme.colors.onBackground,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontFamily = PlusJakartaSans,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .animateEnterExit(
                                        enter = fadeIn(animationSpec = tween(500)),
                                        exit = fadeOut(animationSpec = tween(500))
                                    )
                                    .padding(bottom = 14.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SolariTheme.colors.background.copy(alpha = 0.58f))
                                    .padding(horizontal = 28.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !isZooming,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    with(animatedVisibilityScope) {
                        Spacer(modifier = Modifier.height(36.dp))

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

                        Spacer(modifier = Modifier.height(32.dp))

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
                                        onValueChange = { reactionNote = it.take(20) },
                                        onReact = ::sendReactionOptimistically,
                                        onOpenEmojiPicker = { showEmojiPicker = true },
                                        isEditable = false,
                                        onActivate = { showInputOverlay(FeedInputOverlayMode.Reaction) }
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    FeedMessageField(
                                        value = messageText,
                                        onValueChange = { messageText = it },
                                        onSend = {},
                                        isEditable = false,
                                        onActivate = { showInputOverlay(FeedInputOverlayMode.Message) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        FeedBrowseButton(
                            modifier = Modifier.animateEnterExit(
                                enter = fadeIn(animationSpec = tween(500)),
                                exit = fadeOut(animationSpec = tween(500))
                            ),
                            onClick = { onNavigateToBrowse(null) }
                        )

                        Spacer(modifier = Modifier.weight(0.45f))
                    }
                }
            }
        }

        if (showEmojiPicker && activeInputOverlay == null) {
            EmojiPickerPopup(
                onClosed = { showEmojiPicker = false },
                selectedEmoji = null,
                recentEmojis = emptyList(),
                onReact = ::sendReactionOptimistically
            )
        }

        activeInputOverlay?.let { overlayMode ->
            Dialog(
                onDismissRequest = ::dismissInputOverlay,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                val view = LocalView.current
                LaunchedEffect(view) {
                    val window = (view.parent as? DialogWindowProvider)?.window
                    window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    FeedInputKeyboardOverlay(
                        mode = overlayMode,
                        reactionValue = reactionNote,
                        onReactionValueChange = { reactionNote = it.take(20) },
                        messageValue = messageText,
                        onMessageValueChange = { messageText = it },
                        isEmojiPickerOpen = showEmojiPicker,
                        onDismiss = ::dismissInputOverlay,
                        onReact = ::sendReactionOptimistically,
                        onOpenEmojiPicker = { showEmojiPicker = true },
                        onSendMessage = ::sendMessageOptimistically
                    )

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
                    fontSize = 14.sp
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
private fun FeedActivitySheet(
    visible: Boolean,
    activities: List<PostActivityEntry>,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val sheetInteractionSource = remember { MutableInteractionSource() }
    var isSheetDragging by remember { mutableStateOf(false) }
    var sheetDragOffsetPx by remember { mutableStateOf(0f) }
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
            isSheetDragging = false
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
                                    isSheetDragging = true
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    sheetDragOffsetPx = (sheetDragOffsetPx + dragAmount)
                                        .coerceAtLeast(0f)
                                },
                                onDragEnd = {
                                    isSheetDragging = false
                                    if (sheetDragOffsetPx > 80f) {
                                        onDismiss()
                                    } else {
                                        sheetDragOffsetPx = 0f
                                    }
                                },
                                onDragCancel = {
                                    isSheetDragging = false
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

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
private fun FeedImage(
    url: String,
    postId: String,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onZoomStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLoading by remember(url) { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotate ->
                    coroutineScope.launch {
                        val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                        scale.snapTo(newScale)
                        rotation.snapTo(rotation.value + rotate)
                        offset.snapTo(offset.value + pan)
                        onZoomStateChanged(newScale > 1f)
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
                        onZoomStateChanged(false)
                        launch { scale.animateTo(1f, tween(300)) }
                        launch { rotation.animateTo(0f, tween(300)) }
                        launch { offset.animateTo(Offset.Zero, tween(300)) }
                    }
                }
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
                translationX = offset.value.x
                translationY = offset.value.y
            }
    ) {
        with(sharedTransitionScope) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = "post_image_$postId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 500) }
                    ),
                contentScale = ContentScale.Crop,
                onLoading = { isLoading = true },
                onSuccess = { isLoading = false },
                onError = { isLoading = false }
            )
        }

        if (isLoading) {
            FeedMediaLoadingIndicator()
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
private fun FeedVideoPlayer(
    url: String,
    mediaType: String,
    postId: String,
    isActive: Boolean,
    sharedTransitionScope: androidx.compose.animation.SharedTransitionScope,
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    onLongPress: () -> Unit,
    onZoomStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember(url) { mutableStateOf(true) }
    var isUserPaused by remember(url) { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val player = remember(url, mediaType) {
        ExoPlayer.Builder(context)
            .setVideoChangeFrameRateStrategy(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
                setMediaItem(
                    MediaItem.Builder()
                        .setUri(Uri.parse(url))
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
        player.volume = 0f
        player.playWhenReady = isActive && !isUserPaused
    }

    DisposableEffect(player) {
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

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotate ->
                    coroutineScope.launch {
                        val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                        scale.snapTo(newScale)
                        rotation.snapTo(rotation.value + rotate)
                        offset.snapTo(offset.value + pan)
                        onZoomStateChanged(newScale > 1f)
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
                        onZoomStateChanged(false)
                        launch { scale.animateTo(1f, tween(300)) }
                        launch { rotation.animateTo(0f, tween(300)) }
                        launch { offset.animateTo(Offset.Zero, tween(300)) }
                    }
                }
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
                translationX = offset.value.x
                translationY = offset.value.y
            }
    ) {
        with(sharedTransitionScope) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = "post_image_$postId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 500) }
                    ),
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
                    if (playerView.player !== player) {
                        playerView.player = player
                    }
                }
            )
        }

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

        if (isLoading) {
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
private fun FeedInputKeyboardOverlay(
    mode: FeedInputOverlayMode,
    reactionValue: String,
    onReactionValueChange: (String) -> Unit,
    messageValue: String,
    onMessageValueChange: (String) -> Unit,
    isEmojiPickerOpen: Boolean,
    onDismiss: () -> Unit,
    onReact: (String) -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onSendMessage: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val bottomInset = with(density) {
        WindowInsets.ime.getBottom(this)
            .coerceAtLeast(WindowInsets.navigationBars.getBottom(this))
            .toDp()
    }
    var hasSeenKeyboard by remember(mode) { mutableStateOf(false) }
    var isBarVisible by remember(mode) { mutableStateOf(false) }

    LaunchedEffect(mode) {
        isBarVisible = true
        withFrameNanos { }
        withFrameNanos { }
        runCatching {
            focusRequester.requestFocus()
        }
        keyboardController?.show()
    }

    LaunchedEffect(isEmojiPickerOpen) {
        if (isEmojiPickerOpen) {
            hasSeenKeyboard = false 
        } else {
            runCatching { focusRequester.requestFocus() }
            keyboardController?.show()
        }
    }

    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            hasSeenKeyboard = true
        } else if (hasSeenKeyboard && !isEmojiPickerOpen) {
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(4f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background.copy(alpha = 0.56f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        AnimatedVisibility(
            visible = isBarVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter),
            enter = fadeIn(animationSpec = tween(durationMillis = 120)) +
                    slideInVertically(
                        animationSpec = tween(durationMillis = 180),
                        initialOffsetY = { it / 2 }
                    ),
            exit = fadeOut(animationSpec = tween(durationMillis = 0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 32.dp, 
                        end = 32.dp, 
                        top = 12.dp, 
                        bottom = bottomInset + 16.dp
                    )
            ) {
                when (mode) {
                    FeedInputOverlayMode.Reaction -> {
                        FeedReactionField(
                            value = reactionValue,
                            onValueChange = onReactionValueChange,
                            onReact = onReact,
                            onOpenEmojiPicker = onOpenEmojiPicker,
                            isEditable = true,
                            focusRequester = focusRequester,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    FeedInputOverlayMode.Message -> {
                        FeedMessageField(
                            value = messageValue,
                            onValueChange = onMessageValueChange,
                            onSend = onSendMessage,
                            isEditable = true,
                            focusRequester = focusRequester,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
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
            tint = SolariTheme.colors.onSurfaceVariant,
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
            .clip(RoundedCornerShape(16.dp))
            .background(SolariTheme.colors.background)
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
