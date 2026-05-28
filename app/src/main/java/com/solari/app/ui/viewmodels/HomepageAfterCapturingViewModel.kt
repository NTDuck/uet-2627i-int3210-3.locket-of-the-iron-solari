package com.solari.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.UserPreferencesStore
import com.solari.app.ui.models.CapturedMedia
import com.solari.app.ui.models.OptimisticPostDraft
import com.solari.app.ui.models.User
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomepageAfterCapturingViewModel(
    private val friendRepository: FriendRepository,
    private val postUploadCoordinator: PostUploadCoordinator,
    private val userPreferencesStore: UserPreferencesStore
) : ViewModel() {
    var friends by mutableStateOf<List<User>>(emptyList())
        private set

    var capturedMedia by mutableStateOf<CapturedMedia?>(null)
        private set

    var caption by mutableStateOf("")
        private set

    var captionType by mutableStateOf("text")
        private set

    var captionMetadata by mutableStateOf<com.solari.app.ui.models.CaptionMetadata?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var hasRequestedLocationPermission by mutableStateOf(false)
        private set

    init {
        loadFriends()
        userPreferencesStore.userPreferencesFlow
            .onEach { preferences ->
                hasRequestedLocationPermission = preferences.hasRequestedLocationPermission
            }
            .launchIn(viewModelScope)
    }

    fun updateCapturedMedia(media: CapturedMedia?) {
        capturedMedia = media
        if (media == null) {
            caption = ""
        }
    }

    fun updateCaption(type: String, metadata: com.solari.app.ui.models.CaptionMetadata?, textFallback: String) {
        captionType = type
        captionMetadata = metadata
        caption = textFallback.take(48)
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
            captionType = captionType,
            captionMetadata = captionMetadata,
            isPublic = isPublic,
            selectedFriendIds = selectedFriendIds
        )
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    fun markLocationPermissionRequested() {
        viewModelScope.launch {
            userPreferencesStore.markLocationPermissionRequested()
        }
    }
}
