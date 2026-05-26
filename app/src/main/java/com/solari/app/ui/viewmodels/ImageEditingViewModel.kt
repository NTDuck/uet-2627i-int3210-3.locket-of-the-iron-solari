package com.solari.app.ui.viewmodels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.models.CapturedMediaEditState
import com.solari.app.ui.models.DrawPath
import com.solari.app.ui.models.TextOverlayState

data class EditState(
    val bitmap: Bitmap,
    val drawingPaths: List<DrawPath>,
    val textOverlays: List<TextOverlayState>
)

class ImageEditingViewModel : ViewModel() {
    var editedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    val drawingPaths = mutableStateListOf<DrawPath>()

    val textOverlays = mutableStateListOf<TextOverlayState>()

    val history = mutableStateListOf<EditState>()

    val hasChanges: Boolean
        get() = history.isNotEmpty()

    fun setInitialBitmap(
        bitmap: Bitmap,
        editState: CapturedMediaEditState? = null
    ) {
        if (editedBitmap == null) {
            editedBitmap = bitmap
            drawingPaths.clear()
            drawingPaths.addAll(editState?.drawingPaths.orEmpty().map { it.deepCopy() })
            textOverlays.clear()
            textOverlays.addAll(editState?.textOverlays.orEmpty())
        }
    }

    private fun saveStateBeforeAction() {
        val currentBmp = editedBitmap ?: return
        val currentPaths = drawingPaths.map { it.deepCopy() }
        val currentTextOverlays = textOverlays.toList()
        history.add(EditState(currentBmp, currentPaths, currentTextOverlays))
    }

    fun updateBitmap(bitmap: Bitmap) {
        saveStateBeforeAction()
        editedBitmap = bitmap
    }

    fun updateDrawingPaths(paths: List<DrawPath>) {
        saveStateBeforeAction()
        drawingPaths.clear()
        drawingPaths.addAll(paths.map { it.deepCopy() })
    }

    fun updateTextOverlays(overlays: List<TextOverlayState>) {
        saveStateBeforeAction()
        textOverlays.clear()
        textOverlays.addAll(overlays)
    }

    fun updateBitmapAndDrawingPaths(bitmap: Bitmap, paths: List<DrawPath>) {
        saveStateBeforeAction()
        editedBitmap = bitmap
        drawingPaths.clear()
        drawingPaths.addAll(paths.map { it.deepCopy() })
    }

    fun updateBitmapDrawingPathsAndTextOverlays(
        bitmap: Bitmap,
        paths: List<DrawPath>,
        overlays: List<TextOverlayState>
    ) {
        saveStateBeforeAction()
        editedBitmap = bitmap
        drawingPaths.clear()
        drawingPaths.addAll(paths.map { it.deepCopy() })
        textOverlays.clear()
        textOverlays.addAll(overlays)
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val lastState = history.removeAt(history.lastIndex)
            editedBitmap = lastState.bitmap
            drawingPaths.clear()
            drawingPaths.addAll(lastState.drawingPaths)
            textOverlays.clear()
            textOverlays.addAll(lastState.textOverlays)
        }
    }
}
