package com.solari.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.user.UserRepository

class SolariViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val friendRepository: FriendRepository,
    private val conversationRepository: ConversationRepository,
    private val recentEmojiStore: RecentEmojiStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
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
                    conversationRepository = conversationRepository
                )
            }

            modelClass.isAssignableFrom(FeedBrowseViewModel::class.java) -> {
                FeedBrowseViewModel(
                    feedRepository = feedRepository,
                    friendRepository = friendRepository,
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(ConversationViewModel::class.java) -> {
                ConversationViewModel(
                    conversationRepository = conversationRepository,
                    friendRepository = friendRepository
                )
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(
                    conversationRepository = conversationRepository,
                    userRepository = userRepository,
                    feedRepository = feedRepository,
                    recentEmojiStore = recentEmojiStore
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
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(FriendInvitePreviewViewModel::class.java) -> {
                FriendInvitePreviewViewModel(
                    friendRepository = friendRepository,
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(HomepageBeforeCapturingViewModel::class.java) -> {
                HomepageBeforeCapturingViewModel(userRepository)
            }

            modelClass.isAssignableFrom(HomepageAfterCapturingViewModel::class.java) -> {
                HomepageAfterCapturingViewModel(
                    friendRepository = friendRepository,
                    feedRepository = feedRepository
                )
            }

            modelClass.isAssignableFrom(PasswordResetViewModel::class.java) -> {
                PasswordResetViewModel(userRepository)
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

            else -> throw IllegalArgumentException(
                "Unsupported ViewModel type: ${modelClass.name}"
            )
        }

        return modelClass.cast(viewModel)
            ?: throw IllegalStateException("Failed to create ${modelClass.name}")
    }
}
