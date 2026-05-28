package com.solari.app.ui.models

import android.graphics.Path
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class CapturedMediaSource {
    Camera,
    Gallery
}

data class DrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean
) {
    fun deepCopy(): DrawPath {
        return copy(path = Path(path))
    }
}

data class TextOverlayState(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val position: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val color: Color = Color.Red,
    val fontSizePx: Float = 48f
)

data class CapturedMediaEditState(
    val baseUri: Uri,
    val drawingPaths: List<DrawPath> = emptyList(),
    val textOverlays: List<TextOverlayState> = emptyList()
) {
    fun deepCopy(): CapturedMediaEditState {
        return copy(drawingPaths = drawingPaths.map { it.deepCopy() })
    }
}

data class CapturedMedia(
    val uri: Uri,
    val contentType: String,
    val isVideo: Boolean,
    val durationMs: Long? = null,
    val source: CapturedMediaSource = CapturedMediaSource.Camera,
    val editState: CapturedMediaEditState? = null
)

data class OptimisticPostDraft(
    val id: String,
    val mediaUri: Uri,
    val contentType: String,
    val caption: String,
    val captionType: String = "text",
    val captionMetadata: CaptionMetadata? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val uploadStatus: PostUploadStatus = PostUploadStatus.Uploading,
    val uploadError: String? = null
)
