package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.feed.InitiatePostUploadRequest
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.User
import java.util.TimeZone
import kotlinx.coroutines.launch

class HomepageAfterCapturingViewModel(
    private val friendRepository: FriendRepository,
    private val feedRepository: FeedRepository
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var capturedMedia by mutableStateOf<CapturedMedia?>(null)
        private set

    var caption by mutableStateOf("")
        private set

    var isUploading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadFriends()
    }

    fun updateCapturedMedia(media: CapturedMedia?) {
        capturedMedia = media
        if (media == null) {
            caption = ""
        }
    }

    fun updateCaption(value: String) {
        caption = value.take(48)
    }

    fun loadFriends() {
        viewModelScope.launch {
            when (val result = friendRepository.getFriends()) {
                is ApiResult.Success -> {
                    friends = result.data
                    errorMessage = null
                }

                is ApiResult.Failure -> errorMessage = result.message
            }
        }
    }

    fun uploadPost(
        contentType: String,
        bytes: ByteArray,
        width: Int,
        height: Int,
        durationMs: Long?,
        isPublic: Boolean,
        selectedFriendIds: Set<String>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            isUploading = true
            errorMessage = null
            successMessage = null

            val initiateRequest = InitiatePostUploadRequest(
                contentType = contentType,
                caption = caption.trim().takeIf { it.isNotEmpty() },
                audienceType = if (isPublic) "all" else "selected",
                viewerIds = if (isPublic) emptyList() else selectedFriendIds.toList(),
                width = width,
                height = height,
                byteSize = bytes.size.toLong(),
                durationMs = durationMs,
                timezone = TimeZone.getDefault().id
            )

            when (val initiateResult = feedRepository.initiatePostUpload(initiateRequest)) {
                is ApiResult.Failure -> {
                    errorMessage = initiateResult.message
                    isUploading = false
                }

                is ApiResult.Success -> {
                    when (
                        val uploadResult = feedRepository.uploadPostBinary(
                            uploadUrl = initiateResult.data.uploadUrl,
                            contentType = contentType,
                            bytes = bytes
                        )
                    ) {
                        is ApiResult.Failure -> {
                            errorMessage = uploadResult.message
                            isUploading = false
                        }

                        is ApiResult.Success -> {
                            when (
                                val finalizeResult = feedRepository.finalizePostUpload(
                                    postId = initiateResult.data.postId,
                                    objectKey = initiateResult.data.objectKey
                                )
                            ) {
                                is ApiResult.Failure -> errorMessage = finalizeResult.message
                                is ApiResult.Success -> {
                                    successMessage = "Post upload queued"
                                    onSuccess(initiateResult.data.postId)
                                }
                            }
                            isUploading = false
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
