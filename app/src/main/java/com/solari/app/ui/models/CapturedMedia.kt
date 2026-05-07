package com.solari.app.ui.models

import android.net.Uri

enum class CapturedMediaSource {
    Camera,
    Gallery
}

data class CapturedMedia(
    val uri: Uri,
    val contentType: String,
    val isVideo: Boolean,
    val durationMs: Long? = null,
    val source: CapturedMediaSource = CapturedMediaSource.Camera
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
