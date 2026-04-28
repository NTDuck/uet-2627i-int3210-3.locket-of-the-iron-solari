package com.solari.app.ui.models

import android.net.Uri

data class CapturedMedia(
    val uri: Uri,
    val contentType: String,
    val isVideo: Boolean,
    val durationMs: Long? = null
)

data class OptimisticPostDraft(
    val id: String,
    val mediaUri: Uri,
    val contentType: String,
    val caption: String,
    val createdAt: Long = System.currentTimeMillis(),
    val uploadStatus: PostUploadStatus = PostUploadStatus.Uploading,
    val uploadError: String? = null
)
