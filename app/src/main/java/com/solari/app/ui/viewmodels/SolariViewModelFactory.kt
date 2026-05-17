package com.solari.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.preferences.UserPreferencesStore
import com.solari.app.data.user.UserRepository
import com.solari.app.data.websocket.WebSocketManager

class SolariViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val friendRepository: FriendRepository,
    private val conversationRepository: ConversationRepository,
    private val recentEmojiStore: RecentEmojiStore,
    private val postUploadCoordinator: PostUploadCoordinator,
    private val webSocketManager: WebSocketManager,
    private val userPreferencesStore: UserPreferencesStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(userPreferencesStore)
            }

            modelClass.isAssignableFrom(AppAuthViewModel::class.java) -> {
                AppAuthViewModel(authRepository)
            }

            modelClass.isAssignableFrom(SignInViewModel::class.java) -> {
                SignInViewModel(authRepository)
            }

            modelClass.isAssignableFrom(WelcomeViewModel::class.java) -> {
                WelcomeViewModel(authRepository)
            }

            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> {
                SignUpViewModel(authRepository)
            }

            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(
                    feedRepository = feedRepository,
                    userRepository = userRepository,
                    friendRepository = friendRepository,
                    conversationRepository = conversationRepository,
                    postUploadCoordinator = postUploadCoordinator,
                    userPreferencesStore = userPreferencesStore
                )
            }

            modelClass.isAssignableFrom(FeedBrowseViewModel::class.java) -> {
                FeedBrowseViewModel(
                    feedRepository = feedRepository,
                    friendRepository = friendRepository,
                    userRepository = userRepository,
                    postUploadCoordinator = postUploadCoordinator,
                    userPreferencesStore = userPreferencesStore
                )
            }

            modelClass.isAssignableFrom(ConversationViewModel::class.java) -> {
                ConversationViewModel(
                    conversationRepository = conversationRepository,
                    friendRepository = friendRepository,
                    webSocketManager = webSocketManager
                )
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(
                    conversationRepository = conversationRepository,
                    userRepository = userRepository,
                    feedRepository = feedRepository,
                    recentEmojiStore = recentEmojiStore,
                    webSocketManager = webSocketManager
                )
            }

            modelClass.isAssignableFrom(ChatSettingsViewModel::class.java) -> {
                ChatSettingsViewModel(
                    conversationRepository = conversationRepository,
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(BlockedAccountsViewModel::class.java) -> {
                BlockedAccountsViewModel(
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    userRepository = userRepository,
                    authRepository = authRepository
                )
            }

            modelClass.isAssignableFrom(FriendManagementViewModel::class.java) -> {
                FriendManagementViewModel(
                    friendRepository = friendRepository,
                    userRepository = userRepository,
                    webSocketManager = webSocketManager
                )
            }

            modelClass.isAssignableFrom(FriendInvitePreviewViewModel::class.java) -> {
                FriendInvitePreviewViewModel(
                    friendRepository = friendRepository,
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(HomepageBeforeCapturingViewModel::class.java) -> {
                HomepageBeforeCapturingViewModel(userRepository, userPreferencesStore)
            }

            modelClass.isAssignableFrom(HomepageAfterCapturingViewModel::class.java) -> {
                HomepageAfterCapturingViewModel(
                    friendRepository = friendRepository,
                    postUploadCoordinator = postUploadCoordinator
                )
            }

            modelClass.isAssignableFrom(ChangePasswordViewModel::class.java) -> {
                ChangePasswordViewModel(userRepository)
            }

            modelClass.isAssignableFrom(PasswordRecoveryViewModel::class.java) -> {
                PasswordRecoveryViewModel(authRepository)
            }

            modelClass.isAssignableFrom(OTPConfirmationViewModel::class.java) -> {
                OTPConfirmationViewModel(authRepository)
            }

            modelClass.isAssignableFrom(CompletePasswordResetViewModel::class.java) -> {
                CompletePasswordResetViewModel(authRepository)
            }

            modelClass.isAssignableFrom(ImageEditingViewModel::class.java) -> {
                ImageEditingViewModel()
            }

            else -> throw IllegalArgumentException(
                "Unsupported ViewModel type: ${modelClass.name}"
            )
        }

        return modelClass.cast(viewModel)
            ?: throw IllegalStateException("Failed to create ${modelClass.name}")
    }
}
