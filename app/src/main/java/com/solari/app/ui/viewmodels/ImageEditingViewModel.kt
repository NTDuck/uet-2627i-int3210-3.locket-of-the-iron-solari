package com.solari.app.ui.viewmodels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ImageEditingViewModel : ViewModel() {
    var editedBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var hasChanges by mutableStateOf(false)
        private set

    fun setInitialBitmap(bitmap: Bitmap) {
        if (editedBitmap == null) {
            editedBitmap = bitmap
        }
    }

    fun updateBitmap(bitmap: Bitmap) {
        editedBitmap = bitmap
        hasChanges = true
    }
}
