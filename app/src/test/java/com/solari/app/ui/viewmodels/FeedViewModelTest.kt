package com.solari.app.ui.viewmodels

import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.feed.PaginatedFeed
import com.solari.app.data.feed.PostUploadCoordinator
import com.solari.app.data.friend.FriendRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.UserPreferencesStore
import com.solari.app.data.user.UserRepository
import com.solari.app.ui.models.Post
import com.solari.app.ui.models.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val feedRepository = mockk<FeedRepository>()
    private val userRepository = mockk<UserRepository>()
    private val friendRepository = mockk<FriendRepository>()
    private val conversationRepository = mockk<ConversationRepository>()
    private val postUploadCoordinator = mockk<PostUploadCoordinator>()
    private val userPreferencesStore = mockk<UserPreferencesStore>()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mocks for init block
        every { postUploadCoordinator.uploads } returns MutableStateFlow(emptyList())
        every { feedRepository.deletedPostIds } returns MutableStateFlow(emptySet())
        coEvery { userRepository.getMe() } returns ApiResult.Success(mockk())
        coEvery { friendRepository.getFriends() } returns ApiResult.Success(emptyList())
        coEvery { feedRepository.getFeed(any(), any(), any(), any()) } returns ApiResult.Success(PaginatedFeed(emptyList(), null))
        coEvery { userPreferencesStore.updateLastFeedViewedTimestamp(any()) } returns Unit
        every { postUploadCoordinator.removeSyncedUploads(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial refresh loads user, friends and feed`() = runTest {
        val user = User(id = "me", username = "me", email = "me@example.com", displayName = "Me", profileImageUrl = null)
        val post = mockk<Post>()
        every { post.id } returns "post-1"
        every { post.author } returns user
        
        coEvery { userRepository.getMe() } returns ApiResult.Success(user)
        coEvery { friendRepository.getFriends() } returns ApiResult.Success(emptyList())
        coEvery { feedRepository.getFeed(any(), any(), any(), any()) } returns ApiResult.Success(PaginatedFeed(listOf(post), "cursor-1"))

        val viewModel = FeedViewModel(
            feedRepository, userRepository, friendRepository, 
            conversationRepository, postUploadCoordinator, userPreferencesStore
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(user, viewModel.currentUser)
        assertEquals(1, viewModel.posts.size)
        assertEquals("post-1", viewModel.posts[0].id)
        assertFalse(viewModel.hasReachedEnd)
    }

    @Test
    fun `loadNextPage appends posts to list`() = runTest {
        val post1 = mockk<Post>()
        every { post1.id } returns "post-1"
        val post2 = mockk<Post>()
        every { post2.id } returns "post-2"

        coEvery { feedRepository.getFeed(any(), any(), any(), null) } returns ApiResult.Success(PaginatedFeed(listOf(post1), "cursor-1"))
        coEvery { feedRepository.getFeed(any(), any(), any(), "cursor-1") } returns ApiResult.Success(PaginatedFeed(listOf(post2), null))

        val viewModel = FeedViewModel(
            feedRepository, userRepository, friendRepository, 
            conversationRepository, postUploadCoordinator, userPreferencesStore
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.posts.size)

        viewModel.loadNextPage()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.posts.size)
        assertEquals("post-2", viewModel.posts[1].id)
        assertTrue(viewModel.hasReachedEnd)
    }
}
