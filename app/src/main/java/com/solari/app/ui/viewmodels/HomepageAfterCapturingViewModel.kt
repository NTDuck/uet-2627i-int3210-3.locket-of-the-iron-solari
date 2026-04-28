package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.User
import kotlinx.coroutines.launch

class HomepageAfterCapturingViewModel(
    private val friendRepository: FriendRepository,
    private val postUploadCoordinator: PostUploadCoordinator
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var capturedMedia by mutableStateOf<CapturedMedia?>(null)
        private set

    var caption by mutableStateOf("")
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

    fun startOptimisticPostUpload(
        media: CapturedMedia,
        isPublic: Boolean,
        selectedFriendIds: Set<String>
    ): OptimisticPostDraft {
        errorMessage = null
        successMessage = "Post upload queued"
        return postUploadCoordinator.startUpload(
            media = media,
            caption = caption,
            isPublic = isPublic,
            selectedFriendIds = selectedFriendIds
        )
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
