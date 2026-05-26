package com.solari.app.ui.viewmodels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.solari.app.ui.screens.DrawPath

data class EditState(
    val bitmap: Bitmap,
    val drawingPaths: List<DrawPath>
)

class ImageEditingViewModel : ViewModel() {
    var editedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    val drawingPaths = mutableStateListOf<DrawPath>()

    val history = mutableStateListOf<EditState>()

    val hasChanges: Boolean
        get() = history.isNotEmpty()

    fun setInitialBitmap(bitmap: Bitmap) {
        if (editedBitmap == null) {
            editedBitmap = bitmap
        }
    }

    private fun saveStateBeforeAction() {
        val currentBmp = editedBitmap ?: return
        val currentPaths = drawingPaths.toList()
        history.add(EditState(currentBmp, currentPaths))
    }

    fun updateBitmap(bitmap: Bitmap) {
        saveStateBeforeAction()
        editedBitmap = bitmap
    }

    fun updateDrawingPaths(paths: List<DrawPath>) {
        saveStateBeforeAction()
        drawingPaths.clear()
        drawingPaths.addAll(paths)
    }

    fun updateBitmapAndDrawingPaths(bitmap: Bitmap, paths: List<DrawPath>) {
        saveStateBeforeAction()
        editedBitmap = bitmap
        drawingPaths.clear()
        drawingPaths.addAll(paths)
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val lastState = history.removeAt(history.lastIndex)
            editedBitmap = lastState.bitmap
            drawingPaths.clear()
            drawingPaths.addAll(lastState.drawingPaths)
        }
    }
}
