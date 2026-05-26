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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import com.solari.app.ui.components.SolariAvatar
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.CapturedMediaSource
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.findActivity
import com.solari.app.ui.util.openAppSettings
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.HomepageAfterCapturingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.roundToInt
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.min
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import kotlin.coroutines.resume

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    var topPillIsSuccess by remember { mutableStateOf(true) }

    val pagerState = rememberPagerState(pageCount = { 6 })
    var customCaptionText by remember { mutableStateOf(viewModel.caption) }
    var locationText by remember { mutableStateOf("") }
    var ratingValue by remember { mutableStateOf(0f) }
    var ratingReviewText by remember { mutableStateOf("") }
    var selectedWeatherCondition by remember { mutableStateOf<String?>(null) }
    var weatherTempCText by remember { mutableStateOf("") }
    var isWeatherSheetOpen by remember { mutableStateOf(false) }
    var isWeatherLoading by remember { mutableStateOf(false) }
    var weatherRequestedLocationAfterPermission by remember { mutableStateOf(false) }

    LaunchedEffect(
        pagerState.currentPage,
        customCaptionText,
        locationText,
        ratingValue,
        ratingReviewText,
        selectedWeatherCondition,
        weatherTempCText
    ) {
        when (pagerState.currentPage) {
            0 -> viewModel.updateCaption("text", com.solari.app.ui.models.CaptionMetadata.Text(customCaptionText), customCaptionText)
            1 -> viewModel.updateCaption("ootd", com.solari.app.ui.models.CaptionMetadata.Ootd, "🕶️ OOTD")
            2 -> {
                val condition = selectedWeatherCondition ?: "Sunny"
                val temp = weatherTempCText.toFloatOrNull()
                val fallback = if (temp != null) "$condition $temp°C" else condition
                viewModel.updateCaption("weather", com.solari.app.ui.models.CaptionMetadata.Weather(condition, temp), fallback)
            }
            3 -> {
                if (locationText.isBlank()) {
                    viewModel.updateCaption("text", null, "")
                } else {
                    viewModel.updateCaption("location", com.solari.app.ui.models.CaptionMetadata.Location(locationText), "📍 $locationText")
                }
            }
            4 -> {
                val fallback = "Rating: $ratingValue" + if (ratingReviewText.isNotBlank()) " - $ratingReviewText" else ""
                viewModel.updateCaption("rating", com.solari.app.ui.models.CaptionMetadata.Rating(ratingValue, ratingReviewText.takeIf { it.isNotBlank() }), fallback)
            }
            5 -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val timeString = timeFormat.format(Date())
                viewModel.updateCaption("clock", com.solari.app.ui.models.CaptionMetadata.Clock(timeString), "⏱️ $timeString")
            }
            else -> viewModel.updateCaption("text", com.solari.app.ui.models.CaptionMetadata.Text(customCaptionText), customCaptionText)
        }
    }
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

    suspend fun fetchAndSetWeather(context: android.content.Context) {
        isWeatherLoading = true
        try {
            val loc = getCurrentLocation(context)
            if (loc != null) {
                val weatherResult = fetchWeatherForLocation(loc.latitude, loc.longitude)
                if (weatherResult != null) {
                    selectedWeatherCondition = weatherResult.first
                    weatherTempCText = String.format(Locale.US, "%.1f", weatherResult.second)
                } else {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Failed to fetch weather. Select manually.", android.widget.Toast.LENGTH_SHORT).show()
                        isWeatherSheetOpen = true
                    }
                }
            } else {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Unable to get current location. Select manually.", android.widget.Toast.LENGTH_SHORT).show()
                    isWeatherSheetOpen = true
                }
            }
        } catch (e: Exception) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Error: ${e.localizedMessage}. Select manually.", android.widget.Toast.LENGTH_SHORT).show()
                isWeatherSheetOpen = true
            }
        } finally {
            isWeatherLoading = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            coroutineScope.launch {
                if (weatherRequestedLocationAfterPermission) {
                    weatherRequestedLocationAfterPermission = false
                    fetchAndSetWeather(context)
                } else {
                    val city = getCityFromLocation(context)
                    if (!city.isNullOrBlank()) {
                        locationText = city
                    }
                }
            }
        } else {
            val activity = context.findActivity()
            if (activity != null &&
                (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                 activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
            ) {
                viewModel.markLocationPermissionRequested()
            }
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineLocationCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocationCheck || coarseLocationCheck
    }

    fun requestLocationPermission(afterGrantFetchesWeather: Boolean) {
        if (hasLocationPermission()) return

        val activity = context.findActivity()
        val shouldOpenSettings = viewModel.hasRequestedLocationPermission &&
                activity != null &&
                !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                !activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (shouldOpenSettings) {
            context.openAppSettings()
            return
        }

        weatherRequestedLocationAfterPermission = afterGrantFetchesWeather
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val requestWeather = {
        if (hasLocationPermission()) {
            coroutineScope.launch {
                fetchAndSetWeather(context)
            }
        } else {
            requestLocationPermission(afterGrantFetchesWeather = true)
        }
        Unit
    }

    val requestLocation = {
        if (hasLocationPermission()) {
            coroutineScope.launch {
                val city = getCityFromLocation(context)
                if (!city.isNullOrBlank()) {
                    locationText = city
                }
            }
            Unit
        } else {
            requestLocationPermission(afterGrantFetchesWeather = false)
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

        android.util.Log.d("SolariDownload", "downloadPreviewMedia: media uri = ${media?.uri}, viewModel capturedMedia uri = ${viewModel.capturedMedia?.uri}, initialCapturedMedia uri = ${initialCapturedMedia?.uri}")
        coroutineScope.launch {
            saveMediaToPictures(context, media)
                .onSuccess { showTopFeedback("Saved to Pictures/Solari", true) }
                .onFailure { showTopFeedback(it.message ?: "Failed to save media.", false) }
        }
    }

    var isZooming by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
            .navigationBarsPadding()
            .padding(bottom = 59.dp)
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
                    .padding(top = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    CapturePreviewCard(
                        mediaUri = media?.uri,
                        isVideo = media?.isVideo == true,
                        customCaptionText = customCaptionText,
                        onCustomCaptionChange = { customCaptionText = it },
                        locationText = locationText,
                        onLocationTextChange = { locationText = it },
                        onRequestLocation = requestLocation,
                        ratingValue = ratingValue,
                        onRatingValueChange = { ratingValue = it },
                        ratingReviewText = ratingReviewText,
                        onRatingReviewTextChange = { ratingReviewText = it },
                        selectedWeatherCondition = selectedWeatherCondition,
                        weatherTempCText = weatherTempCText,
                        onWeatherTempCTextChange = { weatherTempCText = it },
                        onOpenWeatherSheet = { isWeatherSheetOpen = true },
                        isWeatherLoading = isWeatherLoading,
                        onAutoFetchWeather = requestWeather,
                        pagerState = pagerState,
                        focusRequester = focusRequester,
                        onDownload = { downloadPreviewMedia() },
                        onCaptionBoundsChanged = { captionBounds = it },
                        onCaptionFocusChanged = { isFocused -> isCaptionFocused = isFocused },
                        onCaptionDone = ::clearCaptionFocus,
                        onZoomStateChanged = { isZooming = it },
                        canTransformMedia = canTransformMedia,
                        mediaTransform = mediaTransform,
                        onMediaTransformChange = { mediaTransform = it }
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { page ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (pagerState.currentPage == page) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

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
                            modifier = Modifier.align(Alignment.Start).padding(start = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth().graphicsLayer { clip = false },
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

        // Feedback pill overlay for download and error messages
        AnimatedVisibility(
            visible = topPillVisible,
            enter = slideInVertically(
                initialOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 260)
            ) + fadeIn(animationSpec = tween(260)),
            exit = slideOutVertically(
                targetOffsetY = { -it * 2 },
                animationSpec = tween(durationMillis = 260)
            ) + fadeOut(animationSpec = tween(260)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
                .zIndex(20f)
        ) {
            SolariFeedbackPill(
                message = topPillMessage,
                isSuccess = topPillIsSuccess
            )
        }
    }

    if (isWeatherSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isWeatherSheetOpen = false },
            containerColor = SolariTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                Text("Select Condition", color = SolariTheme.colors.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(listOf("Sunny", "Cloudy", "Cool", "Cold", "Rainy", "Snowy", "Windy", "Stormy")) { condition ->
                        Text(
                            text = condition,
                            color = SolariTheme.colors.onSurface,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedWeatherCondition = condition
                                    isWeatherSheetOpen = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CapturePreviewCard(
    mediaUri: Uri?,
    isVideo: Boolean,
    customCaptionText: String,
    onCustomCaptionChange: (String) -> Unit,
    locationText: String,
    onLocationTextChange: (String) -> Unit,
    onRequestLocation: () -> Unit,
    ratingValue: Float,
    onRatingValueChange: (Float) -> Unit,
    ratingReviewText: String,
    onRatingReviewTextChange: (String) -> Unit,
    selectedWeatherCondition: String?,
    weatherTempCText: String,
    onWeatherTempCTextChange: (String) -> Unit,
    onOpenWeatherSheet: () -> Unit,
    isWeatherLoading: Boolean,
    onAutoFetchWeather: () -> Unit,
    pagerState: PagerState,
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
                            contentScale = ContentScale.Crop
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
                .padding(14.dp)
                .zIndex(1f),
            onClick = onDownload
        ) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Download",
                tint = SolariTheme.colors.onBackground,
                modifier = Modifier.size(22.dp)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = mediaTransform.scale <= 1f
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                when (page) {
                    0 -> {
                        Surface(
                            color = SolariTheme.colors.background.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                                .scaledClickable(pressedScale = 1.08f) {
                                    focusRequester.requestFocus()
                                }
                                .onGloballyPositioned { coordinates ->
                                    onCaptionBoundsChanged(coordinates.boundsInRoot())
                                }
                        ) {
                            BasicTextField(
                                value = customCaptionText,
                                onValueChange = { onCustomCaptionChange(it.take(48)) },
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .wrapContentWidth()
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
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                textStyle = TextStyle(
                                    color = SolariTheme.colors.onBackground,
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = PlusJakartaSans,
                                    textAlign = TextAlign.Center
                                ),
                                cursorBrush = SolidColor(SolariTheme.colors.onBackground),
                                singleLine = false,
                                maxLines = 2,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { onCaptionDone() }
                                ),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center) {
                                        if (customCaptionText.isBlank()) {
                                            Text(
                                                text = "Add a message",
                                                color = SolariTheme.colors.onBackground.copy(alpha = 0.72f),
                                                fontSize = 14.sp,
                                                lineHeight = 16.sp,
                                                fontFamily = PlusJakartaSans,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                    1 -> {
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFFE4E4E4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 15.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🕶️",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "OOTD",
                                    color = androidx.compose.ui.graphics.Color(0xFF252525),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = PlusJakartaSans,
                                    textAlign = TextAlign.Center,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                    2 -> {
                        val condition = selectedWeatherCondition
                        val surfaceColor = if (isWeatherLoading || condition == null) {
                            SolariTheme.colors.background.copy(alpha = 0.7f)
                        } else {
                            getWeatherConditionColor(condition)
                        }
                        Surface(
                            color = surfaceColor,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(bottom = 15.dp)
                                .then(
                                    if (isWeatherLoading) {
                                        Modifier
                                    } else {
                                        Modifier.combinedClickable(
                                            onClick = onAutoFetchWeather,
                                            onLongClick = onOpenWeatherSheet
                                        )
                                    }
                                )
                        ) {
                            if (isWeatherLoading) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = SolariTheme.colors.onBackground,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Fetching weather...",
                                        color = SolariTheme.colors.onBackground,
                                        fontSize = 14.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                            } else if (condition == null) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "☁️", fontSize = 18.sp)
                                    Text(
                                        text = "Get Weather",
                                        color = SolariTheme.colors.onBackground,
                                        fontSize = 14.sp,
                                        lineHeight = 16.sp,
                                        fontFamily = PlusJakartaSans,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                            } else {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val iconMap = mapOf("Sunny" to "☀️", "Cloudy" to "☁️", "Cool" to "❄️", "Cold" to "🥶", "Rainy" to "🌧️", "Snowy" to "🌨️", "Windy" to "💨", "Stormy" to "⛈️")
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = iconMap[condition] ?: "☀️",
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            text = condition,
                                            color = SolariTheme.colors.onBackground,
                                            fontSize = 14.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                    val tempVal = weatherTempCText.toFloatOrNull()
                                    if (tempVal != null) {
                                        Text(
                                            text = "${tempVal.formatCaptionTemperature()}°C",
                                            color = SolariTheme.colors.onBackground.copy(alpha = 0.7f),
                                            fontSize = 14.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = PlusJakartaSans,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Surface(
                            color = SolariTheme.colors.background.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 15.dp)
                                .scaledClickable(pressedScale = 1.08f) {
                                    onRequestLocation()
                                }
                                .onGloballyPositioned { onCaptionBoundsChanged(it.boundsInRoot()) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.solari.app.R.drawable.location),
                                    contentDescription = null,
                                    tint = if (locationText.isBlank()) {
                                        SolariTheme.colors.onBackground.copy(alpha = 0.7f)
                                    } else {
                                        SolariTheme.colors.onBackground
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = locationText.ifBlank { "Location name" },
                                    color = if (locationText.isBlank()) {
                                        SolariTheme.colors.onBackground.copy(alpha = 0.7f)
                                    } else {
                                        SolariTheme.colors.onBackground
                                    },
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                )
                            }
                        }
                    }
                    4 -> {
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFF000000).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 15.dp)
                                .onGloballyPositioned { onCaptionBoundsChanged(it.boundsInRoot()) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StarRatingInput(
                                    rating = ratingValue,
                                    onRatingChange = onRatingValueChange
                                )
                                if (ratingValue > 0f) {
                                    BasicTextField(
                                        value = ratingReviewText,
                                        onValueChange = { onRatingReviewTextChange(it.take(48)) },
                                        modifier = Modifier.widthIn(max = 240.dp).wrapContentWidth().focusRequester(focusRequester).onFocusChanged { onCaptionFocusChanged(it.isFocused) },
                                        textStyle = TextStyle(
                                            color = SolariTheme.colors.onBackground,
                                            fontSize = 14.sp,
                                            lineHeight = 16.sp,
                                            fontFamily = PlusJakartaSans,
                                            textAlign = TextAlign.Center
                                        ),
                                        cursorBrush = SolidColor(SolariTheme.colors.onBackground),
                                        singleLine = false,
                                        maxLines = 2,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { onCaptionDone() }),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.Center) {
                                                if (ratingReviewText.isBlank()) {
                                                    Text("Write a review...", color = SolariTheme.colors.onBackground.copy(alpha = 0.7f), fontSize = 14.sp, lineHeight = 16.sp, fontFamily = PlusJakartaSans)
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    5 -> {
                        Surface(
                            color = androidx.compose.ui.graphics.Color(0xFF000000).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(bottom = 15.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(com.solari.app.R.drawable.clock),
                                    contentDescription = null,
                                    tint = androidx.compose.ui.graphics.Color(0xFFD9D9D9),
                                    modifier = Modifier.size(18.dp)
                                )
                                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                Text(
                                    text = timeFormat.format(Date()),
                                    color = androidx.compose.ui.graphics.Color(0xFFD9D9D9),
                                    fontSize = 14.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 24.dp)) {
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

private fun saveMediaToPictures(
    context: android.content.Context,
    media: CapturedMedia?
): Result<Unit> {
    val mediaUri = media?.uri ?: return Result.failure(IllegalStateException("No media selected."))

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

@Composable
private fun StarRatingInput(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var width by remember { mutableStateOf(0f) }
    Row(
        modifier = modifier
            .onGloballyPositioned { width = it.size.width.toFloat() }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    var x = down.position.x
                    if (width > 0) {
                        onRatingChange((Math.round((x / width * 5f).coerceIn(0f, 5f) * 2) / 2.0).toFloat())
                    }
                    do {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        changes.forEach { it.consume() }
                        if (changes.isNotEmpty()) {
                            x = changes.first().position.x
                            if (width > 0) {
                                onRatingChange((Math.round((x / width * 5f).coerceIn(0f, 5f) * 2) / 2.0).toFloat())
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..5) {
            val fraction = (rating - (i - 1)).coerceIn(0f, 1f)
            Box(modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.StarOutline,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.5f),
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
}

private fun Bitmap.centerCropSquare(): Bitmap {
    val side = minOf(width, height)
    val left = (width - side) / 2
    val top = (height - side) / 2
    return Bitmap.createBitmap(this, left, top, side, side)
}

private suspend fun getCurrentLocation(context: android.content.Context): android.location.Location? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return@withContext null
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return@withContext null
        }
        
        val providers = locationManager.getProviders(true)
        var bestLocation: android.location.Location? = null
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }
        
        if (bestLocation == null) {
            val provider = if (locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                android.location.LocationManager.NETWORK_PROVIDER
            } else if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                android.location.LocationManager.GPS_PROVIDER
            } else {
                null
            }
            
            if (provider != null) {
                val locationDeferred = kotlinx.coroutines.CompletableDeferred<android.location.Location?>()
                var activeListener: android.location.LocationListener? = null
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        val listener = object : android.location.LocationListener {
                            override fun onLocationChanged(location: android.location.Location) {
                                locationDeferred.complete(location)
                                locationManager.removeUpdates(this)
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(p0: String?, p1: Int, p2: android.os.Bundle?) {}
                            override fun onProviderEnabled(p0: String) {}
                            override fun onProviderDisabled(p0: String) {}
                        }
                        activeListener = listener
                        locationManager.requestLocationUpdates(provider, 0L, 0f, listener, android.os.Looper.getMainLooper())
                    } catch (e: Exception) {
                        locationDeferred.complete(null)
                    }
                }
                
                try {
                    kotlinx.coroutines.withTimeoutOrNull(4000) {
                        val newLoc = locationDeferred.await()
                        if (newLoc != null) {
                            bestLocation = newLoc
                        }
                    }
                } finally {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        activeListener?.let { locationManager.removeUpdates(it) }
                    }
                }
            }
        }
        bestLocation
    } catch (e: Exception) {
        null
    }
}

private suspend fun getCityFromLocation(context: android.content.Context): String? {
    val loc = getCurrentLocation(context) ?: return null
    return try {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        val addresses = geocoder.getFromLocationCompat(loc.latitude, loc.longitude, 1)
        val address = addresses?.firstOrNull()
        address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: address?.featureName
    } catch (e: Exception) {
        android.util.Log.e("SolariLocation", "Failed to fetch city name", e)
        null
    }
}

private suspend fun android.location.Geocoder.getFromLocationCompat(
    latitude: Double,
    longitude: Double,
    maxResults: Int
): List<android.location.Address>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        suspendCancellableCoroutine { continuation ->
            getFromLocation(
                latitude,
                longitude,
                maxResults,
                object : android.location.Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<android.location.Address>) {
                        if (continuation.isActive) {
                            continuation.resume(addresses)
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                }
            )
        }
    } else {
        @Suppress("DEPRECATION")
        getFromLocation(latitude, longitude, maxResults)
    }
}

private suspend fun fetchWeatherForLocation(lat: Double, lon: Double): Pair<String, Float>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val client = okhttp3.OkHttpClient()
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
        val request = okhttp3.Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext null
        
        val tempPattern = Regex("\"temperature_2m\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
        val codePattern = Regex("\"weather_code\"\\s*:\\s*(\\d+)")
        
        val tempMatch = tempPattern.find(body)
        val codeMatch = codePattern.find(body)
        
        val temp = tempMatch?.groupValues?.get(1)?.toFloatOrNull() ?: return@withContext null
        val code = codeMatch?.groupValues?.get(1)?.toIntOrNull() ?: return@withContext null
        
        val baseCondition = when (code) {
            0 -> "Sunny"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Cloudy"
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "Rainy"
            71, 73, 75, 77, 85, 86 -> "Snowy"
            95, 96, 99 -> "Stormy"
            else -> "Sunny"
        }
        
        val condition = if (baseCondition == "Sunny" || baseCondition == "Cloudy") {
            if (temp < 10f) {
                "Cold"
            } else if (temp < 20f) {
                "Cool"
            } else {
                baseCondition
            }
        } else {
            baseCondition
        }
        
        Pair(condition, temp)
    } catch (e: Exception) {
        android.util.Log.e("SolariWeather", "Failed to fetch weather", e)
        null
    }
}

private fun getWeatherConditionColor(condition: String): androidx.compose.ui.graphics.Color {
    val colorHex = when (condition) {
        "Sunny" -> 0xFFFFA726    // Vibrant Orange
        "Cloudy" -> 0xFF78909C   // Muted Slate Gray
        "Cool" -> 0xFF26C6DA     // Cool Teal/Cyan
        "Cold" -> 0xFF42A5F5     // Freezing Sky Blue
        "Rainy" -> 0xFF5C6BC0    // Rainy Indigo
        "Snowy" -> 0xFF90A4AE    // Soft Lavender Gray
        "Windy" -> 0xFF26A69A    // Mint Green
        "Stormy" -> 0xFF5E35B1   // Deep Violet/Purple
        else -> 0xFF00ACC1       // Default Cyan
    }
    return androidx.compose.ui.graphics.Color(colorHex).copy(alpha = 0.85f)
}

private fun Float.formatCaptionTemperature(): String {
    val rounded = roundToInt()
    return if (this == rounded.toFloat()) rounded.toString() else toString()
}
