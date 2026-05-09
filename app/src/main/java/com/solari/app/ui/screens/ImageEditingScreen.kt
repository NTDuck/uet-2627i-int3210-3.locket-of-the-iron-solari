package com.solari.app.ui.screens

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.graphics.withRotation
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.ImageEditingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.graphics.Path as NativePath

private enum class EditMode {
    Main, Crop, Draw, Text, Adjust
}

private enum class DrawTool {
    Brush, Eraser
}

private enum class AdjustType {
    Exposure, Contrast, Brightness, Saturation, Temperature
}

private data class DrawPath(
    val path: NativePath,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean
)

private class TextOverlay(
    val id: String = UUID.randomUUID().toString(),
    text: String = "",
    position: Offset = Offset.Zero,
    rotation: Float = 0f,
    scale: Float = 1f,
    color: Color = Color.Red
) {
    var text by mutableStateOf(text)
    var position by mutableStateOf(position)
    var rotation by mutableFloatStateOf(rotation)
    var scale by mutableFloatStateOf(scale)
    var color by mutableStateOf(color)
}

private val PresetColors = listOf(
    Color.White,
    Color.Gray,
    Color.Black,
    Color(0xFF800080),
    Color.Red,
    Color(0xFFFFA500),
    Color.Yellow,
    Color.Green,
    Color.Blue
)

@Composable
fun ImageEditingScreen(
    viewModel: ImageEditingViewModel,
    initialMedia: CapturedMedia?,
    onNavigateBack: (CapturedMedia?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentMode by remember { mutableStateOf(EditMode.Main) }
    var baseBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var applyTrigger by remember { mutableStateOf(false) }

    var cropRotation by remember { mutableFloatStateOf(0f) }
    var cropFlipH by remember { mutableFloatStateOf(1f) }
    var cropFlipV by remember { mutableFloatStateOf(1f) }

    var drawColor by remember { mutableStateOf(Color.Red) }
    var drawTool by remember { mutableStateOf(DrawTool.Brush) }
    var selectedEraserSize by remember { mutableFloatStateOf(30f) }

    var textColor by remember { mutableStateOf(Color.Red) }

    var adjustType by remember { mutableStateOf<AdjustType?>(AdjustType.Exposure) }
    var adjustValues by remember { mutableStateOf(AdjustType.entries.associateWith { 0f }) }

    LaunchedEffect(initialMedia) {
        if (initialMedia != null && baseBitmap == null) {
            withContext(Dispatchers.IO) {
                try {
                    val source = android.graphics.ImageDecoder.createSource(
                        context.contentResolver,
                        initialMedia.uri
                    )
                    val bitmap =
                        android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.isMutableRequired = true
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                            }
                        }
                    val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    baseBitmap = softwareBitmap
                    currentBitmap = softwareBitmap
                    softwareBitmap?.let { viewModel.setInitialBitmap(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            isLoading = false
        }
    }

    BackHandler {
        if (currentMode != EditMode.Main) {
            currentMode = EditMode.Main
        } else {
            onNavigateBack(null)
        }
    }

    Scaffold(
        containerColor = SolariTheme.colors.background,
        topBar = {
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "topBar"
            ) { mode ->
                if (mode == EditMode.Main) {
                    EditingTopBar(
                        onCancel = { onNavigateBack(null) },
                        onApply = {
                            coroutineScope.launch {
                                val finalBitmap = currentBitmap ?: return@launch
                                val file = File(
                                    context.cacheDir,
                                    "edited_${System.currentTimeMillis()}.jpg"
                                )
                                withContext(Dispatchers.IO) {
                                    try {
                                        FileOutputStream(file).use { out ->
                                            finalBitmap.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                95,
                                                out
                                            )
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                onNavigateBack(
                                    initialMedia?.copy(uri = android.net.Uri.fromFile(file))
                                )
                            }
                        },
                        canApply = viewModel.hasChanges
                    )
                } else {
                    SubScreenTopBar(
                        onCancel = { currentMode = EditMode.Main },
                        onApply = { applyTrigger = true }
                    )
                }
            }
        },
        bottomBar = {
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                },
                label = "bottomBar"
            ) { mode ->
                when (mode) {
                    EditMode.Main -> EditingBottomBar(onModeSelected = { currentMode = it })
                    EditMode.Crop -> CropBottomRow(
                        onRotate = { cropRotation = (cropRotation + 90f) % 360f },
                        onHFlip = { cropFlipH *= -1f },
                        onVFlip = { cropFlipV *= -1f }
                    )

                    EditMode.Draw -> DrawBottomRow(
                        selectedColor = drawColor,
                        onColorSelected = { drawColor = it },
                        selectedTool = drawTool,
                        onToolSelected = { drawTool = it },
                        selectedEraserSize = selectedEraserSize,
                        onEraserSizeSelected = { selectedEraserSize = it }
                    )

                    EditMode.Text -> TextBottomRow(
                        selectedColor = textColor,
                        onColorSelected = { textColor = it }
                    )

                    EditMode.Adjust -> AdjustBottomRow(
                        activeType = adjustType,
                        onTypeSelected = { adjustType = it },
                        values = adjustValues,
                        onValuesChange = { adjustValues = it }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SolariTheme.colors.primary)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                currentBitmap?.let { bitmap ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (currentMode) {
                            EditMode.Main -> {
                                androidx.compose.foundation.Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            EditMode.Crop -> {
                                CropWorkspace(
                                    bitmap = bitmap,
                                    rotation = cropRotation,
                                    flipH = cropFlipH,
                                    flipV = cropFlipV,
                                    applyTrigger = applyTrigger,
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                        applyTrigger = false
                                    }
                                )
                            }

                            EditMode.Draw -> {
                                DrawWorkspace(
                                    bitmap = bitmap,
                                    selectedColor = drawColor,
                                    selectedTool = drawTool,
                                    selectedEraserSize = selectedEraserSize,
                                    applyTrigger = applyTrigger,
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                        applyTrigger = false
                                    }
                                )
                            }

                            EditMode.Text -> {
                                TextWorkspace(
                                    bitmap = bitmap,
                                    selectedColor = textColor,
                                    applyTrigger = applyTrigger,
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                        applyTrigger = false
                                    }
                                )
                            }

                            EditMode.Adjust -> {
                                AdjustWorkspace(
                                    bitmap = bitmap,
                                    values = adjustValues,
                                    applyTrigger = applyTrigger,
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                        applyTrigger = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditingTopBar(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    canApply: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancel) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cancel",
                tint = SolariTheme.colors.secondary
            )
        }
        IconButton(
            onClick = onApply,
            enabled = canApply
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Apply",
                tint = if (canApply) SolariTheme.colors.primary else SolariTheme.colors.primary.copy(
                    alpha = 0.38f
                )
            )
        }
    }
}

@Composable
private fun EditingBottomBar(
    onModeSelected: (EditMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditModeButton("Crop", Icons.Default.Crop, onClick = { onModeSelected(EditMode.Crop) })
        Spacer(modifier = Modifier.width(32.dp))
        EditModeButton("Draw", Icons.Default.Brush, onClick = { onModeSelected(EditMode.Draw) })
        Spacer(modifier = Modifier.width(32.dp))
        EditModeButton(
            "Text",
            Icons.Default.TextFields,
            onClick = { onModeSelected(EditMode.Text) })
        Spacer(modifier = Modifier.width(32.dp))
        EditModeButton("Adjust", Icons.Default.Tune, onClick = { onModeSelected(EditMode.Adjust) })
    }
}

@Composable
private fun EditModeButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .scaledClickable(pressedScale = 1.1f, onClick = onClick)
                .clip(CircleShape)
                .background(SolariTheme.colors.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = SolariTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = SolariTheme.colors.onBackground,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 7.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CropWorkspace(
    bitmap: Bitmap,
    rotation: Float,
    flipH: Float,
    flipV: Float,
    applyTrigger: Boolean,
    onApply: (Bitmap) -> Unit
) {
    val animRotation by animateFloatAsState(targetValue = rotation, label = "rotation")
    val animFlipH by animateFloatAsState(targetValue = flipH, label = "flipH")
    val animFlipV by animateFloatAsState(targetValue = flipV, label = "flipV")

    var containerSize by remember { mutableStateOf(Size.Zero) }
    var imageRect by remember { mutableStateOf(RectF()) }
    var cropRect by remember { mutableStateOf(RectF()) }
    var draggingEdge by remember { mutableStateOf(CropEdge.None) }

    val transformedBitmap = remember(bitmap, rotation, flipH, flipV) {
        val matrix = AndroidMatrix()
        matrix.postRotate(rotation)
        matrix.postScale(flipH, flipV)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    LaunchedEffect(applyTrigger) {
        if (applyTrigger) {
            val scaleX = transformedBitmap.width / imageRect.width()
            val scaleY = transformedBitmap.height / imageRect.height()

            val left = ((cropRect.left - imageRect.left) * scaleX).toInt()
                .coerceIn(0, transformedBitmap.width - 1)
            val top = ((cropRect.top - imageRect.top) * scaleY).toInt()
                .coerceIn(0, transformedBitmap.height - 1)
            val right = ((cropRect.right - imageRect.left) * scaleX).toInt()
                .coerceIn(left + 1, transformedBitmap.width)
            val bottom = ((cropRect.bottom - imageRect.top) * scaleY).toInt()
                .coerceIn(top + 1, transformedBitmap.height)

            val width = (right - left).coerceAtLeast(1)
            val height = (bottom - top).coerceAtLeast(1)
            onApply(Bitmap.createBitmap(transformedBitmap, left, top, width, height))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { containerSize = it.size.toSize() },
        contentAlignment = Alignment.Center
    ) {
        val imageWidth = transformedBitmap.width.toFloat()
        val imageHeight = transformedBitmap.height.toFloat()
        val containerWidth = containerSize.width
        val containerHeight = containerSize.height

        if (containerWidth > 0 && containerHeight > 0) {
            val scale = min(containerWidth / imageWidth, containerHeight / imageHeight)
            val drawWidth = imageWidth * scale
            val drawHeight = imageHeight * scale
            val left = (containerWidth - drawWidth) / 2
            val top = (containerHeight - drawHeight) / 2

            imageRect = RectF(left, top, left + drawWidth, top + drawHeight)
            if (cropRect.isEmpty) {
                val size = min(drawWidth, drawHeight)
                val cLeft = left + (drawWidth - size) / 2
                val cTop = top + (drawHeight - size) / 2
                cropRect = RectF(cLeft, cTop, cLeft + size, cTop + size)
            } else {
                val newLeft = cropRect.left.coerceIn(imageRect.left, imageRect.right - 100f)
                val newTop = cropRect.top.coerceIn(imageRect.top, imageRect.bottom - 100f)
                val newRight = cropRect.right.coerceIn(newLeft + 100f, imageRect.right)
                val newBottom = cropRect.bottom.coerceIn(newTop + 100f, imageRect.bottom)

                val size = min(newRight - newLeft, newBottom - newTop)
                cropRect = if (size != cropRect.width() || size != cropRect.height()) {
                    RectF(newLeft, newTop, newLeft + size, newTop + size)
                } else {
                    RectF(newLeft, newTop, newRight, newBottom)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.Image(
                    transformedBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = animRotation - rotation
                            scaleX = if (flipH != 0f) animFlipH / flipH else 1f
                            scaleY = if (flipV != 0f) animFlipV / flipV else 1f
                        },
                    contentScale = ContentScale.Fit
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val hitSlop = 40.dp.toPx()
                                    draggingEdge = when {
                                        abs(offset.x - cropRect.left) < hitSlop && abs(
                                            offset.y - cropRect.top
                                        ) < hitSlop -> CropEdge.TopLeft

                                        abs(offset.x - cropRect.right) < hitSlop && abs(
                                            offset.y - cropRect.top
                                        ) < hitSlop -> CropEdge.TopRight

                                        abs(offset.x - cropRect.left) < hitSlop && abs(
                                            offset.y - cropRect.bottom
                                        ) < hitSlop -> CropEdge.BottomLeft

                                        abs(offset.x - cropRect.right) < hitSlop && abs(
                                            offset.y - cropRect.bottom
                                        ) < hitSlop -> CropEdge.BottomRight

                                        offset.x > cropRect.left && offset.x < cropRect.right &&
                                            offset.y > cropRect.top && offset.y < cropRect.bottom -> CropEdge.Inside

                                        abs(offset.x - cropRect.left) < hitSlop -> CropEdge.Left
                                        abs(offset.x - cropRect.right) < hitSlop -> CropEdge.Right
                                        abs(offset.y - cropRect.top) < hitSlop -> CropEdge.Top
                                        abs(offset.y - cropRect.bottom) < hitSlop -> CropEdge.Bottom
                                        else -> CropEdge.None
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (draggingEdge == CropEdge.None) return@detectDragGestures
                                    change.consume()
                                    val newRect = RectF(cropRect)

                                    when (draggingEdge) {
                                        CropEdge.Left -> {
                                            val delta = dragAmount.x
                                            newRect.left = (newRect.left + delta).coerceIn(
                                                imageRect.left,
                                                newRect.right - 100f
                                            )
                                            val newWidth = newRect.width()
                                            newRect.bottom =
                                                (newRect.top + newWidth).coerceAtMost(imageRect.bottom)
                                            if (newRect.bottom - newRect.top < newWidth) {
                                                newRect.left = newRect.right - newRect.height()
                                            }
                                        }

                                        CropEdge.Right -> {
                                            val delta = dragAmount.x
                                            newRect.right = (newRect.right + delta).coerceIn(
                                                newRect.left + 100f,
                                                imageRect.right
                                            )
                                            val newWidth = newRect.width()
                                            newRect.bottom =
                                                (newRect.top + newWidth).coerceAtMost(imageRect.bottom)
                                            if (newRect.bottom - newRect.top < newWidth) {
                                                newRect.right = newRect.left + newRect.height()
                                            }
                                        }

                                        CropEdge.Top -> {
                                            val delta = dragAmount.y
                                            newRect.top = (newRect.top + delta).coerceIn(
                                                imageRect.top,
                                                newRect.bottom - 100f
                                            )
                                            val newHeight = newRect.height()
                                            newRect.right =
                                                (newRect.left + newHeight).coerceAtMost(imageRect.right)
                                            if (newRect.right - newRect.left < newHeight) {
                                                newRect.top = newRect.bottom - newRect.width()
                                            }
                                        }

                                        CropEdge.Bottom -> {
                                            val delta = dragAmount.y
                                            newRect.bottom = (newRect.bottom + delta).coerceIn(
                                                newRect.top + 100f,
                                                imageRect.bottom
                                            )
                                            val newHeight = newRect.height()
                                            newRect.right =
                                                (newRect.left + newHeight).coerceAtMost(imageRect.right)
                                            if (newRect.right - newRect.left < newHeight) {
                                                newRect.bottom = newRect.top + newRect.width()
                                            }
                                        }

                                        CropEdge.TopLeft -> {
                                            val delta =
                                                if (abs(dragAmount.x) > abs(dragAmount.y)) dragAmount.x else dragAmount.y
                                            val size = (newRect.width() - delta).coerceIn(
                                                100f,
                                                min(
                                                    newRect.right - imageRect.left,
                                                    newRect.bottom - imageRect.top
                                                )
                                            )
                                            newRect.left = newRect.right - size
                                            newRect.top = newRect.bottom - size
                                        }

                                        CropEdge.TopRight -> {
                                            val delta =
                                                if (abs(dragAmount.x) > abs(dragAmount.y)) dragAmount.x else -dragAmount.y
                                            val size = (newRect.width() + delta).coerceIn(
                                                100f,
                                                min(
                                                    imageRect.right - newRect.left,
                                                    newRect.bottom - imageRect.top
                                                )
                                            )
                                            newRect.right = newRect.left + size
                                            newRect.top = newRect.bottom - size
                                        }

                                        CropEdge.BottomLeft -> {
                                            val delta =
                                                if (abs(dragAmount.x) > abs(dragAmount.y)) dragAmount.x else -dragAmount.y
                                            val size = (newRect.width() - delta).coerceIn(
                                                100f,
                                                min(
                                                    newRect.right - imageRect.left,
                                                    imageRect.bottom - newRect.top
                                                )
                                            )
                                            newRect.left = newRect.right - size
                                            newRect.bottom = newRect.top + size
                                        }

                                        CropEdge.BottomRight -> {
                                            val delta =
                                                if (abs(dragAmount.x) > abs(dragAmount.y)) dragAmount.x else dragAmount.y
                                            val size = (newRect.width() + delta).coerceIn(
                                                100f,
                                                min(
                                                    imageRect.right - newRect.left,
                                                    imageRect.bottom - newRect.top
                                                )
                                            )
                                            newRect.right = newRect.left + size
                                            newRect.bottom = newRect.top + size
                                        }

                                        CropEdge.Inside -> {
                                            val dx = dragAmount.x
                                            val dy = dragAmount.y
                                            val w = newRect.width()
                                            val h = newRect.height()
                                            newRect.left = (newRect.left + dx).coerceIn(imageRect.left, imageRect.right - w)
                                            newRect.top = (newRect.top + dy).coerceIn(imageRect.top, imageRect.bottom - h)
                                            newRect.right = newRect.left + w
                                            newRect.bottom = newRect.top + h
                                        }

                                        else -> {}
                                    }
                                    cropRect = newRect
                                },
                                onDragEnd = { draggingEdge = CropEdge.None }
                            )
                        }
                ) {
                    drawRect(Color.Black.copy(alpha = 0.5f), size = Size(size.width, cropRect.top))
                    drawRect(
                        Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(0f, cropRect.bottom),
                        size = Size(size.width, size.height - cropRect.bottom)
                    )
                    drawRect(
                        Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(0f, cropRect.top),
                        size = Size(cropRect.left, cropRect.height())
                    )
                    drawRect(
                        Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(cropRect.right, cropRect.top),
                        size = Size(size.width - cropRect.right, cropRect.height())
                    )

                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cropRect.left, cropRect.top),
                        size = Size(cropRect.width(), cropRect.height()),
                        style = Stroke(2.dp.toPx())
                    )

                    val handleSize = 10.dp.toPx()
                    val handleStroke = 3.dp.toPx()

                    drawLine(
                        Color.White,
                        Offset(cropRect.left, cropRect.top),
                        Offset(cropRect.left + handleSize, cropRect.top),
                        handleStroke
                    )
                    drawLine(
                        Color.White,
                        Offset(cropRect.left, cropRect.top),
                        Offset(cropRect.left, cropRect.top + handleSize),
                        handleStroke
                    )

                    drawLine(
                        Color.White,
                        Offset(cropRect.right, cropRect.top),
                        Offset(cropRect.right - handleSize, cropRect.top),
                        handleStroke
                    )
                    drawLine(
                        Color.White,
                        Offset(cropRect.right, cropRect.top),
                        Offset(cropRect.right, cropRect.top + handleSize),
                        handleStroke
                    )

                    drawLine(
                        Color.White,
                        Offset(cropRect.left, cropRect.bottom),
                        Offset(cropRect.left + handleSize, cropRect.bottom),
                        handleStroke
                    )
                    drawLine(
                        Color.White,
                        Offset(cropRect.left, cropRect.bottom),
                        Offset(cropRect.left, cropRect.bottom - handleSize),
                        handleStroke
                    )

                    drawLine(
                        Color.White,
                        Offset(cropRect.right, cropRect.bottom),
                        Offset(cropRect.right - handleSize, cropRect.bottom),
                        handleStroke
                    )
                    drawLine(
                        Color.White,
                        Offset(cropRect.right, cropRect.bottom),
                        Offset(cropRect.right, cropRect.bottom - handleSize),
                        handleStroke
                    )
                }
            }
        }
    }
}

@Composable
private fun CropBottomRow(
    onRotate: () -> Unit,
    onHFlip: () -> Unit,
    onVFlip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubScreenActionButton(
                "Rotate",
                Icons.AutoMirrored.Filled.RotateRight,
                onClick = onRotate
            )
            SubScreenActionButton("H-Flip", Icons.Default.Flip, onClick = onHFlip)
            SubScreenActionButton("V-Flip", Icons.Default.Flip, onClick = onVFlip)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private enum class CropEdge { None, Left, Top, Right, Bottom, TopLeft, TopRight, BottomLeft, BottomRight, Inside }

@Composable
private fun DrawWorkspace(
    bitmap: Bitmap,
    selectedColor: Color,
    selectedTool: DrawTool,
    selectedEraserSize: Float,
    applyTrigger: Boolean,
    onApply: (Bitmap) -> Unit
) {
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<NativePath?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var eraserCursorPosition by remember { mutableStateOf<Offset?>(null) }

    var drawCounter by remember { mutableIntStateOf(0) }

    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()
    val screenScale = if (canvasSize.width > 0) min(
        canvasSize.width / imageWidth,
        canvasSize.height / imageHeight
    ) else 1f
    val drawWidth = imageWidth * screenScale
    val drawHeight = imageHeight * screenScale
    val imgLeft = (canvasSize.width - drawWidth) / 2
    val imgTop = (canvasSize.height - drawHeight) / 2

    LaunchedEffect(applyTrigger) {
        if (applyTrigger) {
            val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val scale = bitmap.width / drawWidth

            val paint = AndroidPaint().apply {
                isAntiAlias = true
                style = AndroidPaint.Style.STROKE
                strokeJoin = AndroidPaint.Join.ROUND
                strokeCap = AndroidPaint.Cap.ROUND
            }

            val layerBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val layerCanvas = AndroidCanvas(layerBitmap)

            paths.forEach { drawPath ->
                paint.strokeWidth = drawPath.strokeWidth * scale
                if (drawPath.isEraser) {
                    paint.xfermode =
                        android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                } else {
                    paint.xfermode = null
                    paint.color = drawPath.color.toArgb()
                }
                val matrix = AndroidMatrix()
                matrix.postScale(scale, scale)
                val scaledPath = NativePath(drawPath.path)
                scaledPath.transform(matrix)
                layerCanvas.drawPath(scaledPath, paint)
            }

            val resultCanvas = AndroidCanvas(result)
            resultCanvas.drawBitmap(layerBitmap, 0f, 0f, null)
            layerBitmap.recycle()

            onApply(result)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { canvasSize = it.size.toSize() }
            .pointerInput(selectedColor, selectedTool, selectedEraserSize, imgLeft, imgTop) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val path = NativePath()
                        path.moveTo(offset.x - imgLeft, offset.y - imgTop)
                        currentPath = path
                        eraserCursorPosition = if (selectedTool == DrawTool.Eraser) offset else null
                        drawCounter++
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPath?.lineTo(change.position.x - imgLeft, change.position.y - imgTop)
                        eraserCursorPosition =
                            if (selectedTool == DrawTool.Eraser) change.position else null
                        drawCounter++
                    },
                    onDragEnd = {
                        currentPath?.let {
                            paths.add(
                                DrawPath(
                                    it,
                                    selectedColor,
                                    if (selectedTool == DrawTool.Eraser) selectedEraserSize else 10f,
                                    selectedTool == DrawTool.Eraser
                                )
                            )
                        }
                        currentPath = null
                        eraserCursorPosition = null
                        drawCounter++
                    },
                    onDragCancel = {
                        currentPath = null
                        eraserCursorPosition = null
                        drawCounter++
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCounter

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                nativeCanvas.drawBitmap(
                    bitmap,
                    null,
                    RectF(imgLeft, imgTop, imgLeft + drawWidth, imgTop + drawHeight),
                    null
                )

                val layerW = drawWidth.toInt().coerceAtLeast(1)
                val layerH = drawHeight.toInt().coerceAtLeast(1)
                val layerBitmap = Bitmap.createBitmap(layerW, layerH, Bitmap.Config.ARGB_8888)
                val layerCanvas = AndroidCanvas(layerBitmap)

                val paint = AndroidPaint().apply {
                    isAntiAlias = true
                    style = AndroidPaint.Style.STROKE
                    strokeJoin = AndroidPaint.Join.ROUND
                    strokeCap = AndroidPaint.Cap.ROUND
                }

                val scale = drawWidth / bitmap.width.toFloat()

                paths.forEach { drawPath ->
                    paint.strokeWidth = drawPath.strokeWidth * scale
                    if (drawPath.isEraser) {
                        paint.xfermode =
                            android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                    } else {
                        paint.xfermode = null
                        paint.color = drawPath.color.toArgb()
                    }
                    val matrix = AndroidMatrix()
                    matrix.postScale(scale, scale)
                    val scaledPath = NativePath(drawPath.path)
                    scaledPath.transform(matrix)
                    layerCanvas.drawPath(scaledPath, paint)
                }

                currentPath?.let { path ->
                    paint.strokeWidth =
                        (if (selectedTool == DrawTool.Eraser) selectedEraserSize else 10f) * scale
                    if (selectedTool == DrawTool.Eraser) {
                        paint.xfermode =
                            android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                    } else {
                        paint.xfermode = null
                        paint.color = selectedColor.toArgb()
                    }
                    val matrix = AndroidMatrix()
                    matrix.postScale(scale, scale)
                    val scaledPath = NativePath(path)
                    scaledPath.transform(matrix)
                    layerCanvas.drawPath(scaledPath, paint)
                }

                nativeCanvas.drawBitmap(
                    layerBitmap,
                    null,
                    RectF(imgLeft, imgTop, imgLeft + drawWidth, imgTop + drawHeight),
                    null
                )
                layerBitmap.recycle()
            }

            eraserCursorPosition
                ?.takeIf { selectedTool == DrawTool.Eraser }
                ?.let { cursorPosition ->
                    val radius = selectedEraserSize / 2f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.18f),
                        radius = radius,
                        center = cursorPosition
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.72f),
                        radius = radius,
                        center = cursorPosition,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
        }
    }
}

@Composable
private fun DrawBottomRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    selectedTool: DrawTool,
    onToolSelected: (DrawTool) -> Unit,
    selectedEraserSize: Float,
    onEraserSizeSelected: (Float) -> Unit
) {
    val eraserSizes = listOf(
        "Small" to 30f,
        "Medium" to 60f,
        "Large" to 120f
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        if (selectedTool == DrawTool.Eraser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                eraserSizes.forEach { (label, size) ->
                    val isSelected = selectedEraserSize == size
                    val pillShape = RoundedCornerShape(20.dp)
                    Surface(
                        onClick = { onEraserSizeSelected(size) },
                        shape = pillShape,
                        color = if (isSelected) SolariTheme.colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(
                            1.dp,
                            SolariTheme.colors.primary
                        ) else null
                    ) {
                        Box(modifier = Modifier.padding(12.dp, 6.dp, 12.dp, 6.dp)) {
                            Text(
                                text = label,
                                color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PlusJakartaSans
                            )
                        }
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PresetColors) { color ->
                ColorButton(color, isSelected = selectedColor == color) { onColorSelected(color) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                "Brush",
                Icons.Default.Brush,
                isSelected = selectedTool == DrawTool.Brush
            ) { onToolSelected(DrawTool.Brush) }
            Spacer(modifier = Modifier.width(24.dp))
            ToolButton(
                "Eraser",
                Icons.Default.AutoFixNormal,
                isSelected = selectedTool == DrawTool.Eraser
            ) { onToolSelected(DrawTool.Eraser) }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TextWorkspace(
    bitmap: Bitmap,
    selectedColor: Color,
    applyTrigger: Boolean,
    onApply: (Bitmap) -> Unit
) {
    var overlay by remember { mutableStateOf<TextOverlay?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var currentWorkingBitmap by remember { mutableStateOf(bitmap) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    val bakeText = {
        overlay?.let { o ->
            if (o.text.isNotEmpty()) {
                val result = currentWorkingBitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = AndroidCanvas(result)

                val imageWidth = currentWorkingBitmap.width.toFloat()
                val imageHeight = currentWorkingBitmap.height.toFloat()
                val containerWidth = canvasSize.width
                val containerHeight = canvasSize.height

                val scale = min(containerWidth / imageWidth, containerHeight / imageHeight)
                val drawWidth = imageWidth * scale
                val drawHeight = imageHeight * scale
                val left = (containerWidth - drawWidth) / 2
                val top = (containerHeight - drawHeight) / 2

                val bitmapX = (o.position.x - left) / scale
                val bitmapY = (o.position.y - top) / scale

                val paint = AndroidPaint().apply {
                    color = o.color.toArgb()
                    textSize = with(density) { 24.sp.toPx() } * o.scale / scale
                    textAlign = AndroidPaint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                val fontMetrics = paint.fontMetrics
                val verticalOffset = (fontMetrics.ascent + fontMetrics.descent) / 2f
                val finalY = bitmapY - verticalOffset

                canvas.withRotation(o.rotation, bitmapX, bitmapY) {
                    drawText(o.text, bitmapX, finalY, paint)
                }
                currentWorkingBitmap = result
            }
        }
        overlay = null
    }

    LaunchedEffect(applyTrigger) {
        if (applyTrigger) {
            if (overlay != null) {
                bakeText()
            }
            onApply(currentWorkingBitmap)
        }
    }

    LaunchedEffect(selectedColor) {
        overlay?.color = selectedColor
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { canvasSize = it.size.toSize() }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (overlay == null) {
                        overlay = TextOverlay(position = offset, color = selectedColor)
                    } else {
                        if (overlay?.text?.isNotEmpty() == true) {
                            bakeText()
                        } else {
                            overlay = null
                        }
                    }
                }
            },
        contentAlignment = Alignment.TopStart
    ) {
        androidx.compose.foundation.Image(
            currentWorkingBitmap.asImageBitmap(),
            null,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        overlay?.let { o ->
            var boxSize by remember(o.id) { mutableStateOf(Size.Zero) }
            val handlePadding = 8.dp
            val handleSize = 28.dp

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (o.position.x - boxSize.width / 2).toInt(),
                            (o.position.y - boxSize.height / 2).toInt()
                        )
                    }
                    .onGloballyPositioned { boxSize = it.size.toSize() }
                    .graphicsLayer(
                        rotationZ = o.rotation,
                        scaleX = o.scale,
                        scaleY = o.scale
                    )
                    .pointerInput(o.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            o.position += dragAmount
                        }
                    }
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(handlePadding)
            ) {
                IconButton(
                    onClick = { overlay = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(handleSize)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                RotateHandle(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(handleSize),
                    boxSize = boxSize,
                    handlePaddingPx = with(density) { handlePadding.toPx() },
                    handleSizePx = with(density) { handleSize.toPx() },
                    onRotate = { deltaDegrees -> o.rotation += deltaDegrees }
                )

                BasicTextField(
                    value = o.text,
                    onValueChange = { newVal -> o.text = newVal },
                    modifier = Modifier
                        .padding(32.dp, 24.dp, 32.dp, 24.dp)
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            if (o.text.isEmpty()) {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        },
                    textStyle = TextStyle(
                        color = o.color,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontFamily = PlusJakartaSans
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(o.color),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                    })
                )

                ScaleHandle(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(handleSize),
                    boxSize = boxSize,
                    handlePaddingPx = with(density) { handlePadding.toPx() },
                    handleSizePx = with(density) { handleSize.toPx() },
                    onScale = { scaleFactor ->
                        o.scale = (o.scale * scaleFactor).coerceIn(0.5f, 5f)
                    }
                )
            }
        }
    }
}

@Composable
private fun RotateHandle(
    modifier: Modifier,
    boxSize: Size,
    handlePaddingPx: Float,
    handleSizePx: Float,
    onRotate: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(boxSize) {
                var lastAngle = Float.NaN

                detectDragGestures(
                    onDragStart = { offset ->
                        val touchX = boxSize.width - handlePaddingPx - handleSizePx + offset.x
                        val touchY = handlePaddingPx + offset.y
                        val centerX = boxSize.width / 2f
                        val centerY = boxSize.height / 2f
                        lastAngle = atan2(touchY - centerY, touchX - centerX)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (lastAngle.isNaN()) return@detectDragGestures
                        val touchX = boxSize.width - handlePaddingPx - handleSizePx + change.position.x
                        val touchY = handlePaddingPx + change.position.y
                        val centerX = boxSize.width / 2f
                        val centerY = boxSize.height / 2f
                        val newAngle = atan2(touchY - centerY, touchX - centerX)
                        var deltaAngle = newAngle - lastAngle
                        if (deltaAngle > Math.PI.toFloat()) deltaAngle -= (2 * Math.PI).toFloat()
                        if (deltaAngle < -Math.PI.toFloat()) deltaAngle += (2 * Math.PI).toFloat()
                        onRotate(Math.toDegrees(deltaAngle.toDouble()).toFloat())
                        lastAngle = newAngle
                    },
                    onDragEnd = { lastAngle = Float.NaN },
                    onDragCancel = { lastAngle = Float.NaN }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.RotateRight,
            null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ScaleHandle(
    modifier: Modifier,
    boxSize: Size,
    handlePaddingPx: Float,
    handleSizePx: Float,
    onScale: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(boxSize) {
                var lastDist = Float.NaN

                detectDragGestures(
                    onDragStart = { offset ->
                        val touchX = boxSize.width - handlePaddingPx - handleSizePx + offset.x
                        val touchY = boxSize.height - handlePaddingPx - handleSizePx + offset.y
                        val centerX = boxSize.width / 2f
                        val centerY = boxSize.height / 2f
                        lastDist = sqrt(
                            (touchX - centerX) * (touchX - centerX) +
                                    (touchY - centerY) * (touchY - centerY)
                        )
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (lastDist.isNaN() || lastDist < 1f) return@detectDragGestures
                        val touchX = boxSize.width - handlePaddingPx - handleSizePx + change.position.x
                        val touchY = boxSize.height - handlePaddingPx - handleSizePx + change.position.y
                        val centerX = boxSize.width / 2f
                        val centerY = boxSize.height / 2f
                        val newDist = sqrt(
                            (touchX - centerX) * (touchX - centerX) +
                                    (touchY - centerY) * (touchY - centerY)
                        )
                        onScale(newDist / lastDist)
                        lastDist = newDist
                    },
                    onDragEnd = { lastDist = Float.NaN },
                    onDragCancel = { lastDist = Float.NaN }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.OpenInFull,
            null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun TextBottomRow(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PresetColors) { color ->
                ColorButton(color, isSelected = selectedColor == color) { onColorSelected(color) }
            }
        }
        Spacer(modifier = Modifier.height(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AdjustWorkspace(
    bitmap: Bitmap,
    values: Map<AdjustType, Float>,
    applyTrigger: Boolean,
    onApply: (Bitmap) -> Unit
) {
    val adjustedBitmap = remember(bitmap, values) {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = AndroidCanvas(result)
        val paint = AndroidPaint()

        val cm = ColorMatrix()

        val exposure = values[AdjustType.Exposure] ?: 0f
        val contrast = (values[AdjustType.Contrast] ?: 0f) + 1f
        val brightness = (values[AdjustType.Brightness] ?: 0f) * 255f
        val saturation = (values[AdjustType.Saturation] ?: 0f) + 1f
        val temperature = values[AdjustType.Temperature] ?: 0f

        cm.set(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness + exposure * 100,
                0f, contrast, 0f, 0f, brightness + exposure * 100,
                0f, 0f, contrast, 0f, brightness + exposure * 100,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val satCm = ColorMatrix()
        satCm.setSaturation(saturation)
        cm.postConcat(satCm)

        val tempCm = ColorMatrix(
            floatArrayOf(
                1f + temperature * 0.1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f - temperature * 0.1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(tempCm)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        result
    }

    LaunchedEffect(applyTrigger) {
        if (applyTrigger) {
            onApply(adjustedBitmap)
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Image(
            adjustedBitmap.asImageBitmap(),
            null,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun AdjustBottomRow(
    activeType: AdjustType?,
    onTypeSelected: (AdjustType) -> Unit,
    values: Map<AdjustType, Float>,
    onValuesChange: (Map<AdjustType, Float>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(AdjustType.entries) { type ->
                FilterChip(
                    selected = activeType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.name, fontSize = 12.sp, fontFamily = PlusJakartaSans) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SolariTheme.colors.primary.copy(alpha = 0.2f),
                        selectedLabelColor = SolariTheme.colors.primary
                    )
                )
            }
        }

        Slider(
            value = values[activeType] ?: 0f,
            onValueChange = { newVal ->
                activeType?.let { type ->
                    val updated = values.toMutableMap()
                    updated[type] = newVal
                    onValuesChange(updated)
                }
            },
            valueRange = -1f..1f,
            modifier = Modifier.padding(32.dp, 8.dp, 32.dp, 8.dp),
            enabled = activeType != null,
            colors = SliderDefaults.colors(
                thumbColor = SolariTheme.colors.primary,
                activeTrackColor = SolariTheme.colors.primary,
                inactiveTrackColor = SolariTheme.colors.surfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SubScreenTopBar(onCancel: () -> Unit, onApply: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp, 12.dp, 16.dp, 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, null, tint = SolariTheme.colors.secondary)
        }
        IconButton(onClick = onApply) {
            Icon(Icons.Default.Check, null, tint = SolariTheme.colors.primary)
        }
    }
}

@Composable
private fun SubScreenActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, null, tint = SolariTheme.colors.onBackground, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            color = SolariTheme.colors.onBackground,
            fontSize = 10.sp,
            fontFamily = PlusJakartaSans
        )
    }
}

@Composable
private fun ColorButton(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                2.dp,
                if (isSelected) SolariTheme.colors.primary else Color.Transparent,
                CircleShape
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ToolButton(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) SolariTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            1.dp,
            SolariTheme.colors.primary
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground,
                fontSize = 14.sp,
                fontFamily = PlusJakartaSans
            )
        }
    }
}
