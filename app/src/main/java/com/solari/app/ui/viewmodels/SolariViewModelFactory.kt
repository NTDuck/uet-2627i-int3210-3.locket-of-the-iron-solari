package com.solari.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.user.UserRepository

class SolariViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository,
    private val friendRepository: FriendRepository,
    private val conversationRepository: ConversationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel = when {
            modelClass.isAssignableFrom(SignInViewModel::class.java) -> {
                SignInViewModel(authRepository)
            }

            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(
                    feedRepository = feedRepository,
                    userRepository = userRepository,
                    friendRepository = friendRepository
                )
            }

            modelClass.isAssignableFrom(FeedBrowseViewModel::class.java) -> {
                FeedBrowseViewModel(
                    feedRepository = feedRepository,
                    friendRepository = friendRepository
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
                    userRepository = userRepository
                )
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(
                    userRepository = userRepository,
                    authRepository = authRepository
                )
            }

            modelClass.isAssignableFrom(HomepageAfterCapturingViewModel::class.java) -> {
                HomepageAfterCapturingViewModel(friendRepository)
            }

            else -> throw IllegalArgumentException(
                "Unsupported ViewModel type: ${modelClass.name}"
            )
        }

        return modelClass.cast(viewModel)
            ?: throw IllegalStateException("Failed to create ${modelClass.name}")
    }
}
