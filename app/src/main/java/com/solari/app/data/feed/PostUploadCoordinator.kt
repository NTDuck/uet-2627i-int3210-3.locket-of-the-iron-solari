package com.solari.app.data.feed

import android.content.Context
import com.solari.app.data.network.ApiResult
import com.solari.app.data.websocket.WebSocketEvent
import com.solari.app.data.websocket.WebSocketManager
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.PostUploadStatus
import com.solari.app.ui.models.CaptionMetadata
import com.solari.app.ui.util.preparePostMediaForUpload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.TimeZone
import java.util.UUID

data class PostUploadEntry(
    val localId: String,
    val serverPostId: String? = null,
    val draft: OptimisticPostDraft,
    val media: CapturedMedia,
    val isPublic: Boolean,
    val selectedFriendIds: Set<String>,
    val remotePost: Post? = null
)

class PostUploadCoordinator(
    context: Context,
    private val feedRepository: FeedRepository,
    private val webSocketManager: WebSocketManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val applicationContext = context.applicationContext
    private val _uploads = MutableStateFlow<List<PostUploadEntry>>(emptyList())
    private val uploadMutationLock = Any()

    val uploads: StateFlow<List<PostUploadEntry>> = _uploads.asStateFlow()

    init {
        scope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is WebSocketEvent.PostProcessed -> handlePostProcessed(event.postId)
                    is WebSocketEvent.PostFailed -> markFailed(
                        postId = event.postId,
                        message = event.error.ifBlank { "Post processing failed." }
                    )

                    else -> Unit
                }
            }
        }
    }

    fun startUpload(
        media: CapturedMedia,
        caption: String,
        captionType: String = "text",
        captionMetadata: CaptionMetadata? = null,
        isPublic: Boolean,
        selectedFriendIds: Set<String>
    ): OptimisticPostDraft {
        val localId = "local-upload-${UUID.randomUUID()}"
        val draft = OptimisticPostDraft(
            id = localId,
            mediaUri = media.uri,
            contentType = media.contentType,
            caption = caption.trim(),
            captionType = captionType,
            captionMetadata = captionMetadata
        )

        updateUploads { uploads ->
            listOf(
                PostUploadEntry(
                    localId = localId,
                    draft = draft,
                    media = media,
                    isPublic = isPublic,
                    selectedFriendIds = selectedFriendIds
                )
            ) + uploads
        }

        scope.launch {
            uploadPost(
                localId = localId,
                media = media,
                caption = draft.caption,
                captionType = draft.captionType,
                captionMetadata = draft.captionMetadata,
                isPublic = isPublic,
                selectedFriendIds = selectedFriendIds
            )
        }

        return draft
    }

    fun retryUpload(postId: String): Boolean {
        val retryEntry = synchronized(uploadMutationLock) {
            val currentUploads = _uploads.value
            val targetEntry = currentUploads.firstOrNull { entry ->
                entry.matchesIdentifier(postId) &&
                        entry.draft.uploadStatus == PostUploadStatus.Failed
            } ?: return@synchronized null

            val resetEntry = targetEntry.copy(
                serverPostId = null,
                draft = targetEntry.draft.copy(
                    id = targetEntry.localId,
                    uploadStatus = PostUploadStatus.Uploading,
                    uploadError = null
                ),
                remotePost = null
            )

            _uploads.value = currentUploads.map { entry ->
                if (entry.localId == targetEntry.localId) resetEntry else entry
            }
            resetEntry
        } ?: return false

        scope.launch {
            uploadPost(
                localId = retryEntry.localId,
                media = retryEntry.media,
                caption = retryEntry.draft.caption,
                captionType = retryEntry.draft.captionType,
                captionMetadata = retryEntry.draft.captionMetadata,
                isPublic = retryEntry.isPublic,
                selectedFriendIds = retryEntry.selectedFriendIds
            )
        }
        return true
    }

    fun removeSyncedUploads(remotePostIds: Set<String>) {
        if (remotePostIds.isEmpty()) return
        updateUploads { uploads ->
            uploads.filterNot { entry ->
                val serverPostId = entry.serverPostId
                serverPostId != null &&
                        serverPostId in remotePostIds &&
                        entry.draft.uploadStatus == PostUploadStatus.None
            }
        }
    }

    fun removePost(postId: String) {
        updateUploads { uploads ->
            uploads.filterNot { entry ->
                entry.matchesIdentifier(postId)
            }
        }
    }

    private suspend fun uploadPost(
        localId: String,
        media: CapturedMedia,
        caption: String,
        captionType: String = "text",
        captionMetadata: CaptionMetadata? = null,
        isPublic: Boolean,
        selectedFriendIds: Set<String>
    ) {
        val preparedMedia =
            preparePostMediaForUpload(applicationContext, media).getOrElse { error ->
                markFailed(localId = localId, message = error.message ?: "Failed to prepare media.")
                return
            }

        updateDraft(localId) { draft ->
            draft.copy(
                mediaUri = preparedMedia.previewUri,
                contentType = preparedMedia.contentType
            )
        }

        val initiateRequest = InitiatePostUploadRequest(
            contentType = preparedMedia.contentType,
            caption = caption.takeIf { it.isNotEmpty() },
            captionType = captionType,
            captionMetadata = captionMetadata,
            audienceType = if (isPublic) "all" else "selected",
            viewerIds = if (isPublic) emptyList() else selectedFriendIds.toList(),
            width = preparedMedia.width,
            height = preparedMedia.height,
            byteSize = preparedMedia.bytes.size.toLong(),
            durationMs = preparedMedia.durationMs,
            timezone = TimeZone.getDefault().id
        )

        val session = when (val result = feedRepository.initiatePostUpload(initiateRequest)) {
            is ApiResult.Failure -> {
                markFailed(localId = localId, message = result.message)
                return
            }

            is ApiResult.Success -> result.data
        }

        bindServerPostId(localId = localId, serverPostId = session.postId)

        when (
            val uploadResult = feedRepository.uploadPostBinary(
                uploadUrl = session.uploadUrl,
                contentType = preparedMedia.contentType,
                bytes = preparedMedia.bytes
            )
        ) {
            is ApiResult.Failure -> {
                markFailed(postId = session.postId, message = uploadResult.message)
                return
            }

            is ApiResult.Success -> Unit
        }

        when (
            val finalizeResult = feedRepository.finalizePostUpload(
                postId = session.postId,
                objectKey = session.objectKey
            )
        ) {
            is ApiResult.Failure -> markFailed(
                postId = session.postId,
                message = finalizeResult.message
            )

            is ApiResult.Success -> markProcessing(postId = session.postId)
        }
    }

    private suspend fun handlePostProcessed(postId: String) {
        val remotePost = when (val result = feedRepository.getPost(postId)) {
            is ApiResult.Failure -> null
            is ApiResult.Success -> result.data
        }

        updateUploads { uploads ->
            uploads.map { entry ->
                if (entry.serverPostId == postId || entry.draft.id == postId) {
                    entry.copy(
                        serverPostId = postId,
                        draft = entry.draft.copy(
                            id = postId,
                            uploadStatus = PostUploadStatus.None,
                            uploadError = null
                        ),
                        remotePost = remotePost?.copy(
                            uploadStatus = PostUploadStatus.None,
                            uploadError = null
                        )
                    )
                } else {
                    entry
                }
            }
        }
    }

    private fun bindServerPostId(localId: String, serverPostId: String) {
        updateUploads { uploads ->
            uploads.map { entry ->
                if (entry.localId == localId) {
                    entry.copy(
                        serverPostId = serverPostId,
                        draft = entry.draft.copy(id = serverPostId)
                    )
                } else {
                    entry
                }
            }
        }
    }

    private fun markProcessing(postId: String) {
        updateDraft(postId = postId) { draft ->
            draft.copy(
                uploadStatus = PostUploadStatus.Processing,
                uploadError = null
            )
        }
    }

    private fun markFailed(
        localId: String? = null,
        postId: String? = null,
        message: String
    ) {
        updateUploads { uploads ->
            uploads.map { entry ->
                val matchesLocalId = localId != null && entry.localId == localId
                val matchesPostId =
                    postId != null && entry.matchesIdentifier(postId)
                if (matchesLocalId || matchesPostId) {
                    entry.copy(
                        draft = entry.draft.copy(
                            uploadStatus = PostUploadStatus.Failed,
                            uploadError = message
                        ),
                        remotePost = null
                    )
                } else {
                    entry
                }
            }
        }
    }

    private fun updateDraft(
        localId: String? = null,
        postId: String? = null,
        transform: (OptimisticPostDraft) -> OptimisticPostDraft
    ) {
        updateUploads { uploads ->
            uploads.map { entry ->
                val matchesLocalId = localId != null && entry.localId == localId
                val matchesPostId =
                    postId != null && entry.matchesIdentifier(postId)
                if (matchesLocalId || matchesPostId) {
                    entry.copy(draft = transform(entry.draft))
                } else {
                    entry
                }
            }
        }
    }

    private fun updateUploads(transform: (List<PostUploadEntry>) -> List<PostUploadEntry>) {
        synchronized(uploadMutationLock) {
            _uploads.value = transform(_uploads.value)
        }
    }

    private fun PostUploadEntry.matchesIdentifier(id: String): Boolean {
        return localId == id ||
                serverPostId == id ||
                draft.id == id ||
                remotePost?.id == id
    }
}
