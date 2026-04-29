package com.solari.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

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
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean
)

private data class TextOverlay(
    val id: String = UUID.randomUUID().toString(),
    var text: String = "...",
    var position: Offset = Offset.Zero,
    var rotation: Float = 0f,
    var scale: Float = 1f,
    var color: Color = Color.Red
)

private val PresetColors = listOf(
    Color.White,
    Color.Gray,
    Color.Black,
    Color(0xFF800080), // Purple
    Color.Red,
    Color(0xFFFFA500), // Orange
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

    LaunchedEffect(initialMedia) {
        if (initialMedia != null && baseBitmap == null) {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(initialMedia.uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                    baseBitmap = bitmap
                    currentBitmap = bitmap
                    bitmap?.let { viewModel.setInitialBitmap(it) }
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
            if (currentMode == EditMode.Main) {
                EditingTopBar(
                    onCancel = { onNavigateBack(null) },
                    onApply = {
                        coroutineScope.launch {
                            val finalBitmap = currentBitmap ?: return@launch
                            val file = File(context.cacheDir, "edited_${System.currentTimeMillis()}.jpg")
                            withContext(Dispatchers.IO) {
                                try {
                                    FileOutputStream(file).use { out ->
                                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
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
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SolariTheme.colors.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    currentBitmap?.let { bitmap ->
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
                                CropSubScreen(
                                    bitmap = bitmap,
                                    onCancel = { currentMode = EditMode.Main },
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                    }
                                )
                            }
                            EditMode.Draw -> {
                                DrawSubScreen(
                                    bitmap = bitmap,
                                    onCancel = { currentMode = EditMode.Main },
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                    }
                                )
                            }
                            EditMode.Text -> {
                                TextSubScreen(
                                    bitmap = bitmap,
                                    onCancel = { currentMode = EditMode.Main },
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                    }
                                )
                            }
                            EditMode.Adjust -> {
                                AdjustSubScreen(
                                    bitmap = bitmap,
                                    onCancel = { currentMode = EditMode.Main },
                                    onApply = { edited ->
                                        currentBitmap = edited
                                        viewModel.updateBitmap(edited)
                                        currentMode = EditMode.Main
                                    }
                                )
                            }
                        }
                    }
                }

                if (currentMode == EditMode.Main) {
                    EditingBottomBar(
                        onModeSelected = { currentMode = it }
                    )
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
                tint = SolariTheme.colors.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onApply,
            enabled = canApply
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Apply",
                tint = if (canApply) SolariTheme.colors.primary else SolariTheme.colors.primary.copy(alpha = 0.38f)
            )
        }
    }
}

@Composable
private fun EditingBottomBar(
    onModeSelected: (EditMode) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 24.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item { EditModeButton("Crop", Icons.Default.Crop, onClick = { onModeSelected(EditMode.Crop) }) }
        item { EditModeButton("Draw", Icons.Default.Brush, onClick = { onModeSelected(EditMode.Draw) }) }
        item { EditModeButton("Text", Icons.Default.TextFields, onClick = { onModeSelected(EditMode.Text) }) }
        item { EditModeButton("Adjust", Icons.Default.Tune, onClick = { onModeSelected(EditMode.Adjust) }) }
    }
}

@Composable
private fun EditModeButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = SolariTheme.colors.onBackground,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            color = SolariTheme.colors.onBackground,
            fontSize = 12.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CropSubScreen(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onApply: (Bitmap) -> Unit
) {
    var rotation by remember { mutableStateOf(0f) }
    var flipH by remember { mutableStateOf(1f) }
    var flipV by remember { mutableStateOf(1f) }
    
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var imageRect by remember { mutableStateOf(RectF()) }
    var cropRect by remember { mutableStateOf(RectF()) }

    val transformedBitmap = remember(bitmap, rotation, flipH, flipV) {
        val matrix = Matrix()
        matrix.postRotate(rotation)
        matrix.postScale(flipH, flipV)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    Column(Modifier.fillMaxSize()) {
        SubScreenTopBar(onCancel, onApply = {
            val scaleX = transformedBitmap.width / imageRect.width()
            val scaleY = transformedBitmap.height / imageRect.height()
            
            val left = ((cropRect.left - imageRect.left) * scaleX).toInt().coerceIn(0, transformedBitmap.width - 1)
            val top = ((cropRect.top - imageRect.top) * scaleY).toInt().coerceIn(0, transformedBitmap.height - 1)
            val right = ((cropRect.right - imageRect.left) * scaleX).toInt().coerceIn(left + 1, transformedBitmap.width)
            val bottom = ((cropRect.bottom - imageRect.top) * scaleY).toInt().coerceIn(top + 1, transformedBitmap.height)
            
            val width = right - left
            val height = bottom - top
            if (width > 0 && height > 0) {
                onApply(Bitmap.createBitmap(transformedBitmap, left, top, width, height))
            } else {
                onApply(transformedBitmap)
            }
        })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { 
                    containerSize = it.size.toSize()
                },
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
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Image(
                        transformedBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newRect = RectF(cropRect)
                                // Square constraint: move 2 sides closest to finger
                                val dx = dragAmount.x
                                val dy = dragAmount.y
                                
                                val distLeft = Math.abs(change.position.x - cropRect.left)
                                val distRight = Math.abs(change.position.x - cropRect.right)
                                val distTop = Math.abs(change.position.y - cropRect.top)
                                val distBottom = Math.abs(change.position.y - cropRect.bottom)
                                
                                if (distLeft < distRight) {
                                    newRect.left = (newRect.left + dx).coerceIn(imageRect.left, newRect.right - 50f)
                                } else {
                                    newRect.right = (newRect.right + dx).coerceIn(newRect.left + 50f, imageRect.right)
                                }
                                
                                if (distTop < distBottom) {
                                    newRect.top = (newRect.top + dy).coerceIn(imageRect.top, newRect.bottom - 50f)
                                } else {
                                    newRect.bottom = (newRect.bottom + dy).coerceIn(newRect.top + 50f, imageRect.bottom)
                                }
                                
                                // Enforce square
                                val newWidth = newRect.width()
                                val newHeight = newRect.height()
                                val size = min(newWidth, newHeight)
                                
                                if (distLeft < distRight) newRect.left = newRect.right - size
                                else newRect.right = newRect.left + size
                                
                                if (distTop < distBottom) newRect.top = newRect.bottom - size
                                else newRect.bottom = newRect.top + size
                                
                                // Re-verify bounds
                                if (newRect.left < imageRect.left) {
                                    newRect.left = imageRect.left
                                    newRect.right = newRect.left + size
                                }
                                if (newRect.right > imageRect.right) {
                                    newRect.right = imageRect.right
                                    newRect.left = newRect.right - size
                                }
                                if (newRect.top < imageRect.top) {
                                    newRect.top = imageRect.top
                                    newRect.bottom = newRect.top + size
                                }
                                if (newRect.bottom > imageRect.bottom) {
                                    newRect.bottom = imageRect.bottom
                                    newRect.top = newRect.bottom - size
                                }

                                cropRect = newRect
                            }
                        }
                    ) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            size = Size(imageRect.left, size.height)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(imageRect.right, 0f),
                            size = Size(size.width - imageRect.right, size.height)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(imageRect.left, 0f),
                            size = Size(imageRect.width(), imageRect.top)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.5f),
                            topLeft = Offset(imageRect.left, imageRect.bottom),
                            size = Size(imageRect.width(), size.height - imageRect.bottom)
                        )
                        
                        // Mask outside crop
                        drawRect(
                            color = Color.Black.copy(alpha = 0.3f),
                            topLeft = Offset(imageRect.left, imageRect.top),
                            size = Size(imageRect.width(), cropRect.top - imageRect.top)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.3f),
                            topLeft = Offset(imageRect.left, cropRect.bottom),
                            size = Size(imageRect.width(), imageRect.bottom - cropRect.bottom)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.3f),
                            topLeft = Offset(imageRect.left, cropRect.top),
                            size = Size(cropRect.left - imageRect.left, cropRect.height())
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.3f),
                            topLeft = Offset(cropRect.right, cropRect.top),
                            size = Size(imageRect.right - cropRect.right, cropRect.height())
                        )

                        drawRect(
                            color = Color.White,
                            topLeft = Offset(cropRect.left, cropRect.top),
                            size = Size(cropRect.width(), cropRect.height()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SubScreenActionButton("Rotate", Icons.Default.RotateRight) { rotation = (rotation + 90f) % 360f }
            SubScreenActionButton("H-Flip", Icons.Default.Flip) { flipH *= -1f }
            SubScreenActionButton("V-Flip", Icons.Default.Flip) { flipV *= -1f }
        }
    }
}

@Composable
private fun DrawSubScreen(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onApply: (Bitmap) -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var selectedTool by remember { mutableStateOf(DrawTool.Brush) }
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Column(Modifier.fillMaxSize()) {
        SubScreenTopBar(onCancel, onApply = {
            val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(result)
            val scaleX = bitmap.width / canvasSize.width
            val scaleY = bitmap.height / canvasSize.height
            val scale = max(scaleX, scaleY)

            val paint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            
            paths.forEach { drawPath ->
                paint.color = drawPath.color.toArgb()
                paint.strokeWidth = drawPath.strokeWidth * scale
                if (drawPath.isEraser) {
                    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                } else {
                    paint.xfermode = null
                }
                
                val matrix = Matrix()
                matrix.postScale(scale, scale)
                val scaledPath = Path(drawPath.path)
                scaledPath.transform(matrix)
                canvas.drawPath(scaledPath, paint)
            }
            onApply(result)
        })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { canvasSize = it.size.toSize() }
                .pointerInput(selectedColor, selectedTool) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val path = Path()
                            path.moveTo(offset.x, offset.y)
                            currentPath = path
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let {
                                paths.add(DrawPath(it, selectedColor, 10f, selectedTool == DrawTool.Eraser))
                            }
                            currentPath = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawIntoCanvas { canvas ->
                    val imageWidth = bitmap.width.toFloat()
                    val imageHeight = bitmap.height.toFloat()
                    val scale = min(size.width / imageWidth, size.height / imageHeight)
                    val drawWidth = imageWidth * scale
                    val drawHeight = imageHeight * scale
                    val left = (size.width - drawWidth) / 2
                    val top = (size.height - drawHeight) / 2

                    canvas.nativeCanvas.drawBitmap(bitmap, null, RectF(left, top, left + drawWidth, top + drawHeight), null)
                    
                    val paint = androidx.compose.ui.graphics.Paint().apply {
                        strokeWidth = 10f
                        style = PaintingStyle.Stroke
                        strokeJoin = StrokeJoin.Round
                        strokeCap = StrokeCap.Round
                    }

                    paths.forEach { drawPath ->
                        paint.color = drawPath.color
                        paint.blendMode = if (drawPath.isEraser) BlendMode.Clear else BlendMode.SrcOver
                        canvas.drawPath(drawPath.path, paint)
                    }
                    
                    currentPath?.let { path ->
                        paint.color = selectedColor
                        paint.blendMode = if (selectedTool == DrawTool.Eraser) BlendMode.Clear else BlendMode.SrcOver
                        canvas.drawPath(path, paint)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(PresetColors) { color ->
                    ColorButton(color, isSelected = selectedColor == color) { selectedColor = color }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton("Brush", Icons.Default.Brush, isSelected = selectedTool == DrawTool.Brush) { selectedTool = DrawTool.Brush }
                Spacer(modifier = Modifier.width(24.dp))
                ToolButton("Eraser", Icons.Default.AutoFixNormal, isSelected = selectedTool == DrawTool.Eraser) { selectedTool = DrawTool.Eraser }
            }
        }
    }
}

@Composable
private fun TextSubScreen(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onApply: (Bitmap) -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.Red) }
    var activeOverlay by remember { mutableStateOf<TextOverlay?>(null) }
    var isEditingText by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Column(Modifier.fillMaxSize()) {
        SubScreenTopBar(onCancel, onApply = {
            val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(result)
            val scaleX = bitmap.width / canvasSize.width
            val scaleY = bitmap.height / canvasSize.height
            val scale = max(scaleX, scaleY)

            val paint = Paint().apply {
                color = activeOverlay?.color?.toArgb() ?: Color.Red.toArgb()
                textSize = 60f * (activeOverlay?.scale ?: 1f) * scale
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            activeOverlay?.let { overlay ->
                if (overlay.text != "..." && overlay.text.isNotEmpty()) {
                    canvas.save()
                    canvas.rotate(overlay.rotation, overlay.position.x * scale, overlay.position.y * scale)
                    canvas.drawText(overlay.text, overlay.position.x * scale, overlay.position.y * scale, paint)
                    canvas.restore()
                }
            }
            onApply(result)
        })

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { canvasSize = it.size.toSize() }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (activeOverlay == null) {
                            activeOverlay = TextOverlay(position = offset, color = selectedColor)
                        } else {
                            // Clicking outside removes if placeholder
                            if (activeOverlay?.text == "...") activeOverlay = null
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

            activeOverlay?.let { overlay ->
                Box(
                    modifier = Modifier
                        .offset(overlay.position.x.dp - 50.dp, overlay.position.y.dp - 30.dp)
                        .graphicsLayer(rotationZ = overlay.rotation, scaleX = overlay.scale, scaleY = overlay.scale)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = { activeOverlay = null },
                        modifier = Modifier.align(Alignment.TopStart).size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Rotate logic
                                    val centerX = overlay.position.x
                                    val centerY = overlay.position.y
                                    val angle = atan2(change.position.y - centerY, change.position.x - centerX)
                                    overlay.rotation = Math.toDegrees(angle.toDouble()).toFloat()
                                }
                            }
                    ) {
                        Icon(Icons.Default.RotateRight, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    BasicTextField(
                        value = overlay.text,
                        onValueChange = { overlay.text = it },
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .focusRequester(focusRequester)
                            .clickable { isEditingText = true },
                        textStyle = TextStyle(
                            color = overlay.color,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = PlusJakartaSans
                        ),
                        cursorBrush = SolidColor(overlay.color),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { 
                            isEditingText = false
                            keyboardController?.hide()
                        })
                    )

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val dist = sqrt(dragAmount.x * dragAmount.x + dragAmount.y * dragAmount.y)
                                    if (dragAmount.x > 0 || dragAmount.y > 0) overlay.scale += dist * 0.005f
                                    else overlay.scale -= dist * 0.005f
                                    overlay.scale = overlay.scale.coerceIn(0.5f, 4f)
                                }
                            }
                    ) {
                        Icon(Icons.Default.OpenInFull, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PresetColors) { color ->
                ColorButton(color, isSelected = selectedColor == color) { 
                    selectedColor = color
                    activeOverlay?.color = color
                }
            }
        }
    }
}

@Composable
private fun AdjustSubScreen(
    bitmap: Bitmap,
    onCancel: () -> Unit,
    onApply: (Bitmap) -> Unit
) {
    var activeType by remember { mutableStateOf<AdjustType?>(null) }
    var values by remember { mutableStateOf(AdjustType.entries.associateWith { 0f }) }

    val adjustedBitmap = remember(bitmap, values) {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint()
        
        val cm = ColorMatrix()
        
        val exposure = values[AdjustType.Exposure] ?: 0f
        val contrast = (values[AdjustType.Contrast] ?: 0f) + 1f
        val brightness = (values[AdjustType.Brightness] ?: 0f) * 255f
        val saturation = (values[AdjustType.Saturation] ?: 0f) + 1f
        val temperature = values[AdjustType.Temperature] ?: 0f
        
        // Apply brightness and contrast
        cm.set(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness + exposure * 100,
            0f, contrast, 0f, 0f, brightness + exposure * 100,
            0f, 0f, contrast, 0f, brightness + exposure * 100,
            0f, 0f, 0f, 1f, 0f
        ))
        
        val satCm = ColorMatrix()
        satCm.setSaturation(saturation)
        cm.postConcat(satCm)
        
        // Apply temperature (simple blue/yellow shift)
        val tempCm = ColorMatrix(floatArrayOf(
            1f + temperature * 0.1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f - temperature * 0.1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(tempCm)
        
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        result
    }

    Column(Modifier.fillMaxSize()) {
        SubScreenTopBar(onCancel, onApply = { onApply(adjustedBitmap) })

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(adjustedBitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }

        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(AdjustType.entries) { type ->
                    FilterChip(
                        selected = activeType == type,
                        onClick = { activeType = type },
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
                onValueChange = { activeType?.let { type ->
                    val newVal = values.toMutableMap()
                    newVal[type] = it
                    values = newVal
                }},
                valueRange = -1f..1f,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                enabled = activeType != null,
                colors = SliderDefaults.colors(
                    thumbColor = SolariTheme.colors.primary,
                    activeTrackColor = SolariTheme.colors.primary,
                    inactiveTrackColor = SolariTheme.colors.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SubScreenTopBar(onCancel: () -> Unit, onApply: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, null, tint = SolariTheme.colors.onSurfaceVariant)
        }
        IconButton(onClick = onApply) {
            Icon(Icons.Default.Check, null, tint = SolariTheme.colors.primary)
        }
    }
}

@Composable
private fun SubScreenActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Icon(icon, null, tint = SolariTheme.colors.onBackground, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = SolariTheme.colors.onBackground, fontSize = 10.sp, fontFamily = PlusJakartaSans)
    }
}

@Composable
private fun ColorButton(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, if (isSelected) SolariTheme.colors.primary else Color.Transparent, CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ToolButton(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) SolariTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SolariTheme.colors.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (isSelected) SolariTheme.colors.primary else SolariTheme.colors.onBackground, fontSize = 14.sp, fontFamily = PlusJakartaSans)
        }
    }
}
