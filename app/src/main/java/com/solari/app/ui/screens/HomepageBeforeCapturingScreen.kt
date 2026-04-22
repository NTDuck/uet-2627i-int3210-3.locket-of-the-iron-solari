package com.solari.app.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.HomepageBeforeCapturingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomepageBeforeCapturingScreen(
    viewModel: HomepageBeforeCapturingViewModel,
    onNavigateBack: () -> Unit,
    onCapture: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var timerValue by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableStateOf(0) }

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().setFlashMode(flashMode).build() }
    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(lensFacing, flashMode) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            imageCapture.flashMode = flashMode

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("Camera", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    val buttonScale = 1.2f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SolariTheme.colors.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        if (viewModel.currentStreak >= 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = SolariTheme.colors.surface,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = "🔥 Current streak: ${viewModel.currentStreak}",
                        color = SolariTheme.colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
            }
        }

        // Viewfinder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .border(1.dp, Color.DarkGray, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            if (isTimerRunning) {
                Text(
                    text = countdownValue.toString(),
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid lines overlay
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(2) {
                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxSize()) {
                repeat(2) {
                    Spacer(modifier = Modifier.weight(1f))
                    VerticalDivider(
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxHeight().width(1.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp * buttonScale)
                    .scaledClickable(pressedScale = 1.2f) {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                    }
                    .clip(CircleShape)
                    .background(SolariTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                    else -> Icons.Default.FlashOff
                }
                Icon(icon, contentDescription = "Flash", tint = SolariTheme.colors.onSurface, modifier = Modifier.size(24.dp * buttonScale))
            }

            Row(
                modifier = Modifier
                    .background(SolariTheme.colors.surface, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp * buttonScale, vertical = 8.dp * buttonScale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHOTO",
                    color = SolariTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp * buttonScale
                )
                Spacer(modifier = Modifier.width(16.dp * buttonScale))
                Text(
                    text = "VIDEO",
                    color = SolariTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp * buttonScale
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp * buttonScale)
                    .scaledClickable(pressedScale = 1.2f) {
                        timerValue = when (timerValue) {
                            0 -> 3
                            3 -> 10
                            else -> 0
                        }
                    }
                    .clip(CircleShape)
                    .background(SolariTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = SolariTheme.colors.onSurface, modifier = Modifier.size(24.dp * buttonScale))
                    if (timerValue > 0) {
                        Text(
                            timerValue.toString(),
                            color = SolariTheme.colors.primary,
                            fontSize = 12.sp * buttonScale,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(y = 12.dp * buttonScale)
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
            Box(
                modifier = Modifier
                    .size(56.dp * buttonScale)
                    .scaledClickable(pressedScale = 1.2f, onClick = onCapture)
                    .clip(CircleShape)
                    .background(SolariTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = "Gallery", tint = SolariTheme.colors.onSurface, modifier = Modifier.size(28.dp * buttonScale))
            }

            // Capture Button
            Surface(
                modifier = Modifier
                    .size(72.dp * buttonScale)
                    .scaledClickable(pressedScale = 1.2f) {
                        if (timerValue > 0) {
                            scope.launch {
                                isTimerRunning = true
                                countdownValue = timerValue
                                while (countdownValue > 0) {
                                    delay(1000)
                                    countdownValue--
                                }
                                onCapture()
                                isTimerRunning = false
                            }
                        } else {
                            onCapture()
                        }
                    },
                shape = CircleShape,
                color = SolariTheme.colors.primary,
                border = BorderStroke(4.dp, SolariTheme.colors.onPrimary.copy(alpha = 0.5f))
            ) {}

            Box(
                modifier = Modifier
                    .size(56.dp * buttonScale)
                    .scaledClickable(pressedScale = 1.2f) {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                    .clip(CircleShape)
                    .background(SolariTheme.colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip",
                    tint = SolariTheme.colors.onSurface,
                    modifier = Modifier.size(28.dp * buttonScale)
                )
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}
