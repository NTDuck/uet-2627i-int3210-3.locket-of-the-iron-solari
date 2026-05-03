package com.solari.app.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.solari.app.navigation.SolariRoute
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariBottomNavBar
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.CapturedMediaSource
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.HomepageAfterCapturingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.min
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint

private val CapturePreviewCornerRadius = 24.dp

private data class CaptureMediaTransform(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val containerSize: IntSize = IntSize.Zero
)

private enum class CaptureSendState {
    Idle,
    Sending,
    Sent
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomepageAfterCapturingScreen(
    viewModel: HomepageAfterCapturingViewModel,
    initialCapturedMedia: CapturedMedia? = null,
    onNavigateBack: () -> Unit,
    onSend: (OptimisticPostDraft) -> Unit,
    onCancel: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val media = viewModel.capturedMedia ?: initialCapturedMedia
    val friends = viewModel.friends
    var selectedFriends by remember { mutableStateOf(setOf<String>()) }
    var topPillVisible by remember { mutableStateOf(false) }
    var topPillMessage by remember { mutableStateOf("") }
    var topPillIsSuccess by remember { mutableStateOf(false) }
    var topPillEventId by remember { mutableStateOf(0) }
    var captionBounds by remember { mutableStateOf<Rect?>(null) }
    var isCaptionFocused by remember { mutableStateOf(false) }
    var pendingLegacyDownload by remember { mutableStateOf(false) }
    var sendState by remember { mutableStateOf(CaptureSendState.Idle) }
    var mediaTransform by remember(media?.uri) { mutableStateOf(CaptureMediaTransform()) }
    val isAllSelected = selectedFriends.isEmpty()
    val canTransformMedia = media != null && media.isVideo != true

    fun showTopFeedback(message: String, isSuccess: Boolean) {
        topPillMessage = message
        topPillIsSuccess = isSuccess
        topPillVisible = true
        topPillEventId += 1
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingLegacyDownload) {
            pendingLegacyDownload = false
            coroutineScope.launch {
                saveMediaToPictures(context, media)
                    .onSuccess { showTopFeedback("Saved to Pictures/Solari", true) }
                    .onFailure { showTopFeedback(it.message ?: "Failed to save media.", false) }
            }
        } else {
            pendingLegacyDownload = false
        }
    }

    LaunchedEffect(topPillEventId) {
        if (topPillEventId > 0) {
            delay(1_000)
            topPillVisible = false
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        val message = viewModel.errorMessage ?: return@LaunchedEffect
        showTopFeedback(message, false)
        viewModel.clearMessages()
    }

    LaunchedEffect(initialCapturedMedia) {
        if (viewModel.capturedMedia == null && initialCapturedMedia != null) {
            viewModel.updateCapturedMedia(initialCapturedMedia)
        }
    }

    fun clearCaptionFocus() {
        isCaptionFocused = false
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    fun isInsideCaption(position: Offset): Boolean {
        return captionBounds?.contains(position) == true
    }

    fun downloadPreviewMedia() {
        if (media == null) {
            showTopFeedback("No media selected.", false)
            return
        }

        if (
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingLegacyDownload = true
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        coroutineScope.launch {
            saveMediaToPictures(context, media)
                .onSuccess { showTopFeedback("Saved to Pictures/Solari", true) }
                .onFailure { showTopFeedback(it.message ?: "Failed to save media.", false) }
        }
    }

    var isZooming by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SolariTheme.colors.background,
        bottomBar = {
            AnimatedVisibility(
                visible = !isZooming,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SolariBottomNavBar(
                    selectedRoute = SolariRoute.Screen.CameraAfter.name,
                    onNavigate = { routeName ->
                        when (routeName) {
                            SolariRoute.Screen.CameraBefore.name -> Unit
                            SolariRoute.Screen.Feed.name -> onNavigateToFeed()
                            SolariRoute.Screen.Conversations.name -> onNavigateToChat()
                            SolariRoute.Screen.Profile.name -> onNavigateToProfile()
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background)
                .padding(innerPadding)
                .pointerInput(captionBounds) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (!isInsideCaption(down.position)) {
                            clearCaptionFocus()
                        }
                        waitForUpOrCancellation(pass = PointerEventPass.Final)
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                CapturePreviewCard(
                    mediaUri = media?.uri,
                    isVideo = media?.isVideo == true,
                    caption = viewModel.caption,
                    onCaptionChange = viewModel::updateCaption,
                    focusRequester = focusRequester,
                    onDownload = ::downloadPreviewMedia,
                    onCaptionBoundsChanged = { captionBounds = it },
                    onCaptionFocusChanged = { isFocused -> isCaptionFocused = isFocused },
                    onCaptionDone = ::clearCaptionFocus,
                    onZoomStateChanged = { isZooming = it },
                    canTransformMedia = canTransformMedia,
                    mediaTransform = mediaTransform,
                    onMediaTransformChange = { mediaTransform = it }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isZooming,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(30.dp))

                        Text(
                            text = "Choose who see this post",
                            color = SolariTheme.colors.onBackground.copy(alpha = 0.86f),
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(end = 6.dp)
                        ) {
                            item {
                                VisibilityAllItem(
                                    selected = isAllSelected,
                                    total = friends.size,
                                    onClick = {
                                        selectedFriends = emptySet()
                                    }
                                )
                            }

                            items(friends) { friend ->
                                val isSelected = friend.id in selectedFriends

                                VisibilityFriendItem(
                                    name = friend.displayName,
                                    username = friend.username,
                                    avatarUrl = friend.profileImageUrl,
                                    selected = isSelected,
                                    onClick = {
                                        selectedFriends = if (isSelected) {
                                            (selectedFriends - friend.id).takeIf { it.isNotEmpty() }
                                                ?: emptySet()
                                        } else {
                                            selectedFriends + friend.id
                                        }
                                    }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CaptureActionButtons(
                                sendState = sendState,
                                isVideo = media?.isVideo == true,
                                onCancel = onCancel,
                                onNavigateToEdit = onNavigateToEdit,
                                onSend = {
                                    if (sendState != CaptureSendState.Idle) {
                                        return@CaptureActionButtons
                                    }

                                    val capturedMedia = media
                                    if (capturedMedia == null) {
                                        showTopFeedback("No media selected.", false)
                                        return@CaptureActionButtons
                                    }

                                    clearCaptionFocus()
                                    coroutineScope.launch {
                                        sendState = CaptureSendState.Sending
                                        val uploadMedia = if (canTransformMedia) {
                                            renderTransformedGalleryImagePreview(
                                                context = context,
                                                media = capturedMedia,
                                                transform = mediaTransform
                                            ).getOrElse { error ->
                                                sendState = CaptureSendState.Idle
                                                showTopFeedback(
                                                    error.message ?: "Failed to prepare media.",
                                                    false
                                                )
                                                return@launch
                                            }
                                        } else {
                                            capturedMedia
                                        }
                                        val draft = viewModel.startOptimisticPostUpload(
                                            media = uploadMedia,
                                            isPublic = isAllSelected,
                                            selectedFriendIds = selectedFriends
                                        )
                                        sendState = CaptureSendState.Sent
                                        delay(260)
                                        onSend(draft)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = topPillVisible,
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
                SolariFeedbackPill(
                    message = topPillMessage,
                    isSuccess = topPillIsSuccess
                )
            }
        }
    }
}

@Composable
private fun CapturePreviewCard(
    mediaUri: Uri?,
    isVideo: Boolean,
    caption: String,
    onCaptionChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onDownload: () -> Unit,
    onCaptionBoundsChanged: (Rect?) -> Unit,
    onCaptionFocusChanged: (Boolean) -> Unit,
    onCaptionDone: () -> Unit,
    onZoomStateChanged: (Boolean) -> Unit,
    canTransformMedia: Boolean,
    mediaTransform: CaptureMediaTransform,
    onMediaTransformChange: (CaptureMediaTransform) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember(mediaUri) { mutableStateOf<IntSize?>(null) }
    val currentMediaTransform by rememberUpdatedState(mediaTransform)
    val currentImageSize by rememberUpdatedState(imageSize)

    LaunchedEffect(mediaUri, canTransformMedia) {
        imageSize = if (mediaUri != null && canTransformMedia) {
            readImageBounds(context, mediaUri)
        } else {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .zIndex(if (mediaTransform.scale > 1f || mediaTransform.offset != Offset.Zero) 10f else 0f)
            .onGloballyPositioned { size = it.size }
            .clip(RoundedCornerShape(CapturePreviewCornerRadius))
            .background(SolariTheme.colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(canTransformMedia, size) {
                    if (!canTransformMedia || size == IntSize.Zero) {
                        return@pointerInput
                    }

                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        var previousPosition = firstDown.position
                        var isTransforming = false

                        do {
                            val event = awaitPointerEvent()
                            val changes = event.changes

                            if (changes.size >= 2) {
                                // Two-finger pinch-to-zoom with centroid-based offset
                                val zoom = event.calculateZoom()
                                val centroid = event.calculateCentroid(useCurrent = true)
                                if (zoom.isFinite() && zoom > 0f) {
                                    val oldScale = currentMediaTransform.scale
                                    val newScale = (oldScale * zoom).coerceIn(1f, 5f)
                                    val scaleFactor = newScale / oldScale
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val nextOffset = currentMediaTransform.offset * scaleFactor +
                                            (centroid - center) * (1 - scaleFactor)

                                    onMediaTransformChange(
                                        CaptureMediaTransform(
                                            scale = newScale,
                                            offset = clampCaptureMediaOffset(
                                                imageSize = currentImageSize,
                                                containerSize = size,
                                                scale = newScale,
                                                offset = nextOffset
                                            ),
                                            containerSize = size
                                        )
                                    )
                                    isTransforming = true
                                    changes.forEach { it.consume() }
                                }
                                // Track centroid for next frame
                                previousPosition = centroid
                                onZoomStateChanged(true)
                            } else if (changes.size == 1) {
                                val change = changes.first()
                                val currentPosition = change.position
                                // Single-finger pan when zoomed in beyond 1x
                                if (currentMediaTransform.scale > 1f) {
                                    val pan = currentPosition - previousPosition
                                    val nextOffset = currentMediaTransform.offset + pan

                                    onMediaTransformChange(
                                        CaptureMediaTransform(
                                            scale = currentMediaTransform.scale,
                                            offset = clampCaptureMediaOffset(
                                                imageSize = currentImageSize,
                                                containerSize = size,
                                                scale = currentMediaTransform.scale,
                                                offset = nextOffset
                                            ),
                                            containerSize = size
                                        )
                                    )
                                    isTransforming = true
                                    change.consume()
                                }
                                previousPosition = currentPosition
                            }
                        } while (changes.any { it.pressed })

                        if (isTransforming) {
                            onZoomStateChanged(false)
                        }
                    }
                }
        ) {
            when {
                mediaUri == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No media selected",
                            color = SolariTheme.colors.onSurface.copy(alpha = 0.72f),
                            fontSize = 16.sp,
                            fontFamily = PlusJakartaSans,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                isVideo -> CaptureVideoPreview(uri = mediaUri)

                else -> {
                    if (canTransformMedia && size != IntSize.Zero) {
                        val sourceImageSize = currentImageSize ?: size
                        val baseScale = max(
                            size.width / sourceImageSize.width.toFloat(),
                            size.height / sourceImageSize.height.toFloat()
                        )
                        val displayedWidth =
                            sourceImageSize.width * baseScale * mediaTransform.scale
                        val displayedHeight =
                            sourceImageSize.height * baseScale * mediaTransform.scale
                        val widthDp = with(density) { displayedWidth.toDp() }
                        val heightDp = with(density) { displayedHeight.toDp() }

                        AsyncImage(
                            model = mediaUri,
                            contentDescription = "Captured image",
                            onSuccess = { state ->
                                val drawable = state.result.drawable
                                if (drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0) {
                                    imageSize = IntSize(
                                        drawable.intrinsicWidth,
                                        drawable.intrinsicHeight
                                    )
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(width = widthDp, height = heightDp)
                                .graphicsLayer {
                                    translationX = mediaTransform.offset.x
                                    translationY = mediaTransform.offset.y
                                },
                            contentScale = ContentScale.FillBounds
                        )
                    } else {
                        AsyncImage(
                            model = mediaUri,
                            contentDescription = "Captured image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SolariTheme.colors.background.copy(alpha = 0.12f))
        )

        PreviewTopActionButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp),
            onClick = onDownload
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Download",
                tint = SolariTheme.colors.onBackground,
                modifier = Modifier.size(22.dp)
            )
        }

        Surface(
            color = SolariTheme.colors.background.copy(alpha = 0.58f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 15.dp)
                .scaledClickable(pressedScale = 1.08f) {
                    focusRequester.requestFocus()
                }
                .onGloballyPositioned { coordinates ->
                    onCaptionBoundsChanged(coordinates.boundsInRoot())
                }
        ) {
            BasicTextField(
                value = caption,
                onValueChange = { onCaptionChange(it.take(48)) },
                modifier = Modifier
                    .width(236.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { onCaptionFocusChanged(it.isFocused) }
                    .onPreviewKeyEvent { event ->
                        if (
                            (event.type == KeyEventType.KeyDown || event.type == KeyEventType.KeyUp) &&
                            event.key == Key.Enter
                        ) {
                            onCaptionDone()
                            true
                        } else {
                            false
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                textStyle = TextStyle(
                    color = SolariTheme.colors.onBackground,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontFamily = PlusJakartaSans,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(SolariTheme.colors.onBackground),
                singleLine = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onCaptionDone() }
                ),
                decorationBox = { innerTextField ->
                    if (caption.isBlank()) {
                        Text(
                            text = "Enter your caption",
                            color = SolariTheme.colors.onBackground.copy(alpha = 0.72f),
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                            fontFamily = PlusJakartaSans,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

private fun clampCaptureMediaOffset(
    imageSize: IntSize?,
    containerSize: IntSize,
    scale: Float,
    offset: Offset
): Offset {
    if (containerSize.width <= 0 || containerSize.height <= 0) {
        return Offset.Zero
    }

    val sourceSize = imageSize?.takeIf { it.width > 0 && it.height > 0 }
        ?: containerSize

    val baseScale = max(
        containerSize.width / sourceSize.width.toFloat(),
        containerSize.height / sourceSize.height.toFloat()
    )
    val displayedWidth = sourceSize.width * baseScale * scale
    val displayedHeight = sourceSize.height * baseScale * scale
    val maxOffsetX = ((displayedWidth - containerSize.width) / 2f).coerceAtLeast(0f)
    val maxOffsetY = ((displayedHeight - containerSize.height) / 2f).coerceAtLeast(0f)

    return Offset(
        x = offset.x.coerceIn(-maxOffsetX, maxOffsetX),
        y = offset.y.coerceIn(-maxOffsetY, maxOffsetY)
    )
}

private suspend fun readImageBounds(context: android.content.Context, uri: Uri): IntSize? =
    withContext(Dispatchers.IO) {
        runCatching {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: throw IllegalStateException("Failed to read selected image.")

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                null
            } else {
                IntSize(options.outWidth, options.outHeight)
            }
        }.getOrNull()
    }

private suspend fun renderTransformedGalleryImagePreview(
    context: android.content.Context,
    media: CapturedMedia,
    transform: CaptureMediaTransform
): Result<CapturedMedia> = withContext(Dispatchers.IO) {
    runCatching {
        val sourceBitmap = decodeBitmapForPreviewTransform(context, media.uri)
        val outputSize = min(max(sourceBitmap.width, sourceBitmap.height), 2048).coerceAtLeast(1)
        val outputBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(outputBitmap)
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG or AndroidPaint.FILTER_BITMAP_FLAG)

        val baseScale = max(
            outputSize / sourceBitmap.width.toFloat(),
            outputSize / sourceBitmap.height.toFloat()
        )
        val previewSize = transform.containerSize
        val safeScale = transform.scale.coerceIn(1f, 5f)
        val offsetScale = if (previewSize.width > 0 && previewSize.height > 0) {
            outputSize / min(previewSize.width, previewSize.height).toFloat()
        } else {
            1f
        }
        val renderedWidth = sourceBitmap.width * baseScale * safeScale
        val renderedHeight = sourceBitmap.height * baseScale * safeScale
        val maxOffsetX = ((renderedWidth - outputSize) / 2f).coerceAtLeast(0f)
        val maxOffsetY = ((renderedHeight - outputSize) / 2f).coerceAtLeast(0f)
        val translatedOffset = Offset(
            x = (transform.offset.x * offsetScale).coerceIn(-maxOffsetX, maxOffsetX),
            y = (transform.offset.y * offsetScale).coerceIn(-maxOffsetY, maxOffsetY)
        )

        val matrix = AndroidMatrix().apply {
            val effectiveScale = baseScale * safeScale
            postScale(effectiveScale, effectiveScale)
            postTranslate(
                (outputSize - sourceBitmap.width * effectiveScale) / 2f + translatedOffset.x,
                (outputSize - sourceBitmap.height * effectiveScale) / 2f + translatedOffset.y
            )
        }

        canvas.drawBitmap(sourceBitmap, matrix, paint)

        val outputFile = File(context.cacheDir, "gallery_preview_${System.currentTimeMillis()}.jpg")
        outputFile.outputStream().use { output ->
            check(outputBitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)) {
                "Failed to encode preview media."
            }
        }

        if (sourceBitmap !== outputBitmap) {
            sourceBitmap.recycle()
        }
        outputBitmap.recycle()

        media.copy(
            uri = Uri.fromFile(outputFile),
            contentType = "image/jpeg",
            isVideo = false,
            durationMs = null,
            source = CapturedMediaSource.Gallery
        )
    }
}

private fun decodeBitmapForPreviewTransform(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val decodedBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        if (decodedBitmap.config == Bitmap.Config.ARGB_8888) {
            decodedBitmap
        } else {
            decodedBitmap.copy(Bitmap.Config.ARGB_8888, false).also { copiedBitmap ->
                if (copiedBitmap !== decodedBitmap) {
                    decodedBitmap.recycle()
                }
            }
        }
    } else {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)?.copy(Bitmap.Config.ARGB_8888, false)
        } ?: throw IllegalStateException("Failed to decode selected image.")
    } ?: throw IllegalStateException("Failed to decode selected image.")
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun CaptureVideoPreview(uri: Uri) {
    val context = LocalContext.current
    val player = remember(uri) {
        ExoPlayer.Builder(context)
            .setVideoChangeFrameRateStrategy(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaItem(MediaItem.fromUri(uri))
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.player = player
            }
        },
        update = { playerView ->
            if (playerView.player !== player) {
                playerView.player = player
            }
        }
    )
}

@Composable
private fun PreviewTopActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .scaledClickable(pressedScale = 1.12f, onClick = onClick)
            .clip(CircleShape)
            .background(SolariTheme.colors.background.copy(alpha = 0.34f)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun RoundActionButton(
    size: androidx.compose.ui.unit.Dp,
    backgroundColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .scaledClickable(
                pressedScale = 1.12f,
                enabled = enabled,
                onClick = onClick
            )
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun PrimaryRoundActionButton(
    size: androidx.compose.ui.unit.Dp,
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .scaledClickable(
                pressedScale = 1.12f,
                enabled = enabled,
                onClick = onClick
            ),
        shape = CircleShape,
        color = SolariTheme.colors.primary,
        border = BorderStroke(4.dp, SolariTheme.colors.onPrimary.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@Composable
private fun VisibilityAllItem(
    selected: Boolean,
    total: Int,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scaledClickable(pressedScale = 1.1f, onClick = onClick)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (selected) SolariTheme.colors.primary else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp)
                .background(SolariTheme.colors.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "All",
                tint = if (selected) SolariTheme.colors.primary else SolariTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = "All ($total)",
            color = if (selected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(56.dp)
                .padding(top = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VisibilityFriendItem(
    name: String,
    username: String,
    avatarUrl: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SolariAvatar(
            imageUrl = avatarUrl,
            username = username,
            contentDescription = "$name avatar",
            modifier = Modifier
                .size(56.dp)
                .scaledClickable(pressedScale = 1.1f, onClick = onClick)
                .border(
                    width = 2.dp,
                    color = if (selected) SolariTheme.colors.primary else Color.Transparent,
                    shape = CircleShape
                )
                .padding(4.dp)
                .clip(CircleShape),
            shape = CircleShape,
            fontSize = 21.sp
        )

        Text(
            text = name,
            color = if (selected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = PlusJakartaSans,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(56.dp)
                .padding(top = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CaptureActionButtons(
    sendState: CaptureSendState,
    isVideo: Boolean,
    onCancel: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onSend: () -> Unit
) {
    val isSending = sendState == CaptureSendState.Sending
    val isSent = sendState == CaptureSendState.Sent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(bottom = 36.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundActionButton(
            size = 64.dp,
            backgroundColor = SolariTheme.colors.surfaceVariant,
            onClick = onCancel
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel",
                tint = SolariTheme.colors.onSurface,
                modifier = Modifier.size(27.dp)
            )
        }

        Spacer(modifier = Modifier.width(28.dp))

        PrimaryRoundActionButton(
            size = 86.dp,
            enabled = sendState == CaptureSendState.Idle,
            onClick = onSend
        ) {
            when {
                isSending -> {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = SolariTheme.colors.onPrimary,
                        trackColor = SolariTheme.colors.primary,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp
                    )
                }

                isSent -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        tint = SolariTheme.colors.onPrimary,
                        modifier = Modifier.size(39.dp)
                    )
                }

                else -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = SolariTheme.colors.onPrimary,
                        modifier = Modifier.size(39.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(28.dp))

        RoundActionButton(
            size = 64.dp,
            backgroundColor = SolariTheme.colors.surfaceVariant.copy(alpha = if (isVideo) 0.5f else 1f),
            enabled = !isVideo,
            onClick = onNavigateToEdit
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = SolariTheme.colors.onSurface.copy(alpha = if (isVideo) 0.5f else 1f),
                modifier = Modifier.size(27.dp)
            )
        }
    }
}

private suspend fun saveMediaToPictures(
    context: android.content.Context,
    media: CapturedMedia?
): Result<Unit> {
    val mediaUri = media?.uri
    if (mediaUri == null) {
        return Result.failure(IllegalStateException("No media selected."))
    }

    return runCatching {
        val mimeType = media.contentType.ifBlank {
            context.contentResolver.getType(mediaUri)
                ?: if (mediaUri.toString().lowercase()
                        .endsWith(".mp4")
                ) "video/mp4" else "image/jpeg"
        }
        val isImage = mimeType.startsWith("image/")
        val outputMimeType = if (isImage) "image/jpeg" else mimeType
        val outputBytes = if (isImage) {
            context.encodePreviewCroppedImage(mediaUri)
        } else {
            context.readUriBytes(mediaUri)
        }
        val extension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(outputMimeType)
            ?: if (outputMimeType.startsWith("video/")) "mp4" else "jpg"
        val fileName = "solari_${System.currentTimeMillis()}.$extension"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = if (outputMimeType.startsWith("video/")) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, outputMimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/Solari"
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val outputUri = context.contentResolver.insert(collection, values)
                ?: throw IllegalStateException("Failed to create destination file.")

            try {
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    output.write(outputBytes)
                } ?: throw IllegalStateException("Failed to read preview media.")

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
                output.write(outputBytes)
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(outputFile.absolutePath),
                arrayOf(outputMimeType),
                null
            )
        }
    }
}

private fun android.content.Context.readUriBytes(uri: Uri): ByteArray {
    contentResolver.openInputStream(uri)?.use { input ->
        return input.readBytes()
    }

    if (uri.scheme == "file") {
        return File(requireNotNull(uri.path) { "Missing file path" }).readBytes()
    }

    throw IllegalStateException("Failed to read preview media.")
}

private fun android.content.Context.encodePreviewCroppedImage(uri: Uri): ByteArray {
    val source = ImageDecoder.createSource(contentResolver, uri)
    val decodedBitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }
    val croppedBitmap = decodedBitmap.centerCropSquare()

    try {
        val output = ByteArrayOutputStream()
        check(croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 94, output)) {
            "Failed to encode cropped image."
        }
        return output.toByteArray()
    } finally {
        if (croppedBitmap !== decodedBitmap) {
            croppedBitmap.recycle()
        }
        decodedBitmap.recycle()
    }
}

private fun Bitmap.centerCropSquare(): Bitmap {
    val side = minOf(width, height)
    val left = (width - side) / 2
    val top = (height - side) / 2
    return Bitmap.createBitmap(this, left, top, side, side)
}
