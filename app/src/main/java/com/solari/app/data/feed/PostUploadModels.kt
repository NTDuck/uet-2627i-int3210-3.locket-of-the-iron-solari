package com.solari.app.data.feed

data class InitiatePostUploadRequest(
    val contentType: String,
    val caption: String?,
    val audienceType: String,
    val viewerIds: List<String>,
    val width: Int,
    val height: Int,
    val byteSize: Long,
    val durationMs: Long? = null,
    val timezone: String
)

data class PostUploadSession(
    val postId: String,
    val objectKey: String,
    val uploadUrl: String
)
