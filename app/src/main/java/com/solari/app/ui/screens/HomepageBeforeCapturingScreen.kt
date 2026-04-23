package com.solari.app.ui.screens

import android.Manifest
import android.graphics.PathMeasure
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.util.Rational
import android.view.Surface
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.ViewPort
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.createCaptureCacheFile
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.HomepageBeforeCapturingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MaxVideoDurationMs = 3_000
private val CapturePreviewCornerRadius = 24.dp

private enum class CaptureMode {
    Photo,
    Video
}

@Composable
fun HomepageBeforeCapturingScreen(
    viewModel: HomepageBeforeCapturingViewModel,
    onNavigateBack: () -> Unit,
    onCapture: (CapturedMedia) -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val recorder = remember {
        Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
    }
    val videoCapture = remember(recorder) { VideoCapture.withOutput(recorder) }
    val recordingProgress = remember { Animatable(0f) }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var captureMode by remember { mutableStateOf(CaptureMode.Photo) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var timerValue by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableIntStateOf(0) }
    var isCameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var activeRecordingFile by remember { mutableStateOf<File?>(null) }
    var isStoppingRecording by remember { mutableStateOf(false) }
    var recordingProgressJob by remember { mutableStateOf<Job?>(null) }
    var isCaptureInFlight by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isCameraPermissionGranted = granted
    }
    val galleryPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        scope.launch {
            val cachedUri = withContext(Dispatchers.IO) {
                runCatching {
                    val extension = MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(mimeType)
                        ?: "jpg"
                    val outputFile = createCaptureCacheFile(context, extension)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    } ?: throw IllegalStateException("Failed to read selected image.")
                    Uri.fromFile(outputFile)
                }.getOrDefault(uri)
            }

            onCapture(
                CapturedMedia(
                    uri = cachedUri,
                    contentType = mimeType,
                    isVideo = false
                )
            )
        }
    }

    fun stopVideoRecording() {
        val recording = activeRecording ?: return
        if (isStoppingRecording) return
        isStoppingRecording = true
        recording.stop()
    }

    fun bindCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageCapture)
                .addUseCase(videoCapture)
                .setViewPort(
                    ViewPort.Builder(Rational(1, 1), rotation)
                        .build()
                )
                .build()

            try {
                cameraProvider.unbindAll()
                boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                    useCaseGroup
                )
            } catch (error: Exception) {
                Log.e("HomepageBeforeCapture", "Failed to bind camera use cases", error)
            }
        }, mainExecutor)
    }

    fun launchTimedCapture(captureAction: () -> Unit) {
        if (timerValue <= 0) {
            captureAction()
            return
        }

        scope.launch {
            isTimerRunning = true
            countdownValue = timerValue
            while (countdownValue > 0) {
                delay(1_000)
                countdownValue -= 1
            }
            isTimerRunning = false
            captureAction()
        }
    }

    fun capturePhoto() {
        if (isCaptureInFlight) return
        isCaptureInFlight = true

        val outputFile = createCaptureCacheFile(context, "jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile)
            .setMetadata(
                ImageCapture.Metadata().apply {
                    // Front camera captures may be mirrored by device pipelines; normalize output.
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            mainExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCaptureInFlight = false
                    onCapture(
                        CapturedMedia(
                            uri = outputFileResults.savedUri ?: Uri.fromFile(outputFile),
                            contentType = "image/jpeg",
                            isVideo = false
                        )
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    isCaptureInFlight = false
                    Log.e("HomepageBeforeCapture", "Photo capture failed", exception)
                }
            }
        )
    }

    fun startVideoRecording() {
        if (activeRecording != null || isCaptureInFlight) return

        val outputFile = createCaptureCacheFile(context, "mp4")
        activeRecordingFile = outputFile
        isStoppingRecording = false
        recordingProgressJob?.cancel()
        isCaptureInFlight = true

        activeRecording = videoCapture.output
            .prepareRecording(context, FileOutputOptions.Builder(outputFile).build())
            .start(mainExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        isCaptureInFlight = false
                        recordingProgressJob = scope.launch {
                            recordingProgress.snapTo(0f)
                            recordingProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = MaxVideoDurationMs,
                                    easing = LinearEasing
                                )
                            )
                            stopVideoRecording()
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        recordingProgressJob?.cancel()
                        scope.launch { recordingProgress.snapTo(0f) }
                        val finishedFile = activeRecordingFile
                        activeRecording = null
                        activeRecordingFile = null
                        isStoppingRecording = false
                        isCaptureInFlight = false

                        if (event.hasError() || finishedFile == null) {
                            Log.e(
                                "HomepageBeforeCapture",
                                "Video recording failed with code ${event.error}"
                            )
                            return@start
                        }

                        onCapture(
                            CapturedMedia(
                                uri = Uri.fromFile(finishedFile),
                                contentType = "video/mp4",
                                isVideo = true
                            )
                        )
                    }

                    else -> Unit
                }
            }
    }

    fun handleShutter() {
        if (captureMode == CaptureMode.Video && activeRecording != null) {
            stopVideoRecording()
            return
        }

        launchTimedCapture {
            when (captureMode) {
                CaptureMode.Photo -> capturePhoto()
                CaptureMode.Video -> startVideoRecording()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isCameraPermissionGranted) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(isCameraPermissionGranted, lensFacing) {
        if (isCameraPermissionGranted) {
            bindCamera()
        }
    }

    LaunchedEffect(boundCamera, isFlashEnabled, captureMode) {
        imageCapture.flashMode = if (isFlashEnabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
        boundCamera?.cameraControl?.enableTorch(captureMode == CaptureMode.Video && isFlashEnabled)
    }

    DisposableEffect(Unit) {
        onDispose {
            recordingProgressJob?.cancel()
            activeRecording?.stop()
            activeRecording = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (viewModel.currentStreak >= 2) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = SolariTheme.colors.surface,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "\uD83D\uDD25 Current streak: ${viewModel.currentStreak}",
                        color = SolariTheme.colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(CapturePreviewCornerRadius))
                .background(Color.Black)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(CapturePreviewCornerRadius)),
            contentAlignment = Alignment.Center
        ) {
            if (isCameraPermissionGranted) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Camera permission is required.",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            GridOverlay()

            if (activeRecording != null) {
                RecordingProgressBorder(progress = recordingProgress.value)
            }

            if (isTimerRunning) {
                Text(
                    text = countdownValue.toString(),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isCaptureInFlight && activeRecording == null) {
                CircularProgressIndicator(
                    color = SolariTheme.colors.primary,
                    trackColor = SolariTheme.colors.surface
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundControlButton(onClick = { isFlashEnabled = !isFlashEnabled }) {
                    Icon(
                        imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash",
                        tint = SolariTheme.colors.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                CaptureModePill(
                    selectedMode = captureMode,
                    onSelect = { selectedMode ->
                        captureMode = selectedMode
                        if (selectedMode == CaptureMode.Photo && activeRecording != null) {
                            stopVideoRecording()
                        }
                    }
                )

                RoundControlButton(
                    onClick = {
                        timerValue = when (timerValue) {
                            0 -> 3
                            3 -> 10
                            else -> 0
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = SolariTheme.colors.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        if (timerValue > 0) {
                            Text(
                                text = timerValue.toString(),
                                color = SolariTheme.colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.offset(y = 12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundControlButton(
                    size = 68.dp,
                    onClick = {
                        galleryPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = SolariTheme.colors.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(86.dp)
                        .scaledClickable(pressedScale = 1.12f, onClick = ::handleShutter),
                    shape = CircleShape,
                    color = if (captureMode == CaptureMode.Video && activeRecording != null) {
                        SolariTheme.colors.secondary
                    } else {
                        SolariTheme.colors.primary
                    },
                    border = BorderStroke(4.dp, SolariTheme.colors.onPrimary.copy(alpha = 0.5f))
                ) {}

                RoundControlButton(
                    size = 68.dp,
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = SolariTheme.colors.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CaptureModePill(
    selectedMode: CaptureMode,
    onSelect: (CaptureMode) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .width(182.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(SolariTheme.colors.surface)
            .padding(6.dp)
    ) {
        val highlightOffsetX by animateDpAsState(
            targetValue = if (selectedMode == CaptureMode.Photo) 0.dp else maxWidth / 2f,
            animationSpec = tween(durationMillis = 100),
            label = "CaptureModePillOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = highlightOffsetX)
                .width(maxWidth / 2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(18.dp))
                .background(SolariTheme.colors.primary)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaptureModeChip(
                text = "PHOTO",
                selected = selectedMode == CaptureMode.Photo,
                onClick = { onSelect(CaptureMode.Photo) },
                modifier = Modifier.weight(1f)
            )
            CaptureModeChip(
                text = "VIDEO",
                selected = selectedMode == CaptureMode.Video,
                onClick = { onSelect(CaptureMode.Video) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CaptureModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .scaledClickable(pressedScale = 1.06f, onClick = onClick)
            .clip(RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) SolariTheme.colors.onPrimary else SolariTheme.colors.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun RoundControlButton(
    size: androidx.compose.ui.unit.Dp = 48.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .scaledClickable(pressedScale = 1.12f, onClick = onClick)
            .clip(CircleShape)
            .background(SolariTheme.colors.surface),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun GridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color.White.copy(alpha = 0.18f)
        val strokeWidth = 1.dp.toPx()
        val firstX = size.width / 3f
        val secondX = size.width * 2f / 3f
        val firstY = size.height / 3f
        val secondY = size.height * 2f / 3f

        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(firstX, 0f),
            end = androidx.compose.ui.geometry.Offset(firstX, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(secondX, 0f),
            end = androidx.compose.ui.geometry.Offset(secondX, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(0f, firstY),
            end = androidx.compose.ui.geometry.Offset(size.width, firstY),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = lineColor,
            start = androidx.compose.ui.geometry.Offset(0f, secondY),
            end = androidx.compose.ui.geometry.Offset(size.width, secondY),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun RecordingProgressBorder(progress: Float) {
    val progressColor = SolariTheme.colors.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 6.dp.toPx()
        val clampedProgress = progress.coerceIn(0f, 1f)
        val radiusPx = CapturePreviewCornerRadius.toPx()
        val inset = strokeWidth / 2f
        val roundRectPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = inset,
                    top = inset,
                    right = size.width - inset,
                    bottom = size.height - inset,
                    radiusX = radiusPx,
                    radiusY = radiusPx
                )
            )
        }

        drawIntoCanvas {
            val sourcePath = roundRectPath.asAndroidPath()
            val pathMeasure = PathMeasure(sourcePath, true)
            val segmentPath = android.graphics.Path()
            pathMeasure.getSegment(
                0f,
                pathMeasure.length * clampedProgress,
                segmentPath,
                true
            )

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    asAndroidPath().set(segmentPath)
                },
                color = progressColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
