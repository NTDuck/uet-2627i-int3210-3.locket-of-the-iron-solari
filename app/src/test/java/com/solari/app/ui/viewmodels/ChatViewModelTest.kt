package com.solari.app.ui.viewmodels

import com.solari.app.data.conversation.ConversationMessagesPage
import com.solari.app.data.conversation.ConversationRepository
import com.solari.app.data.feed.FeedRepository
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.user.UserRepository
import com.solari.app.data.websocket.WebSocketManager
import com.solari.app.ui.models.Conversation
import com.solari.app.ui.models.Message
import com.solari.app.ui.models.MessageDeliveryState
import com.solari.app.ui.models.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val conversationRepository = mockk<ConversationRepository>()
    private val userRepository = mockk<UserRepository>()
    private val feedRepository = mockk<FeedRepository>()
    private val recentEmojiStore = mockk<RecentEmojiStore>()
    private val webSocketManager = mockk<WebSocketManager>()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { recentEmojiStore.recentEmojis } returns emptyFlow()
        every { webSocketManager.events } returns MutableSharedFlow()
        coEvery { userRepository.getMe() } returns ApiResult.Success(
            User(id = "my-id", username = "me", email = "me@example.com", displayName = "Me", profileImageUrl = null)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage adds optimistic message and then replaces with server message`() = runTest {
        val otherUser = User(id = "other-id", username = "other", email = "other@example.com", displayName = "Other", profileImageUrl = null)
        val initialConversation = Conversation(
            id = "chat-123",
            otherUser = otherUser,
            messages = emptyList(),
            lastMessage = "",
            lastMessageSenderId = null,
            timestamp = 0L,
            isUnread = false
        )

        val serverMessage = Message(
            id = "server-msg-1",
            senderId = "my-id",
            text = "Hello",
            timestamp = 1000L,
            deliveryState = MessageDeliveryState.Sent
        )

        coEvery { conversationRepository.getConversation("chat-123") } returns ApiResult.Success(initialConversation)
        coEvery { conversationRepository.getMessages("chat-123", any()) } returns ApiResult.Success(
            ConversationMessagesPage(emptyList(), null, null)
        )
        coEvery { conversationRepository.markConversationRead("chat-123") } returns ApiResult.Success(Unit)
        coEvery { conversationRepository.sendMessage("chat-123", "Hello", repliedMessageId = null) } returns ApiResult.Success(serverMessage)

        val viewModel = ChatViewModel(
            conversationRepository, userRepository, feedRepository, recentEmojiStore, webSocketManager
        )
        
        // Load conversation to ensure currentUserId is set
        viewModel.loadConversation("chat-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.messageText = "Hello"
        viewModel.sendMessage("chat-123")

        // 1. Check optimistic message
        val optimisticConv = viewModel.conversation
        assertNotNull(optimisticConv)
        assertEquals(1, optimisticConv!!.messages.size)
        assertEquals("Hello", optimisticConv.messages[0].text)
        assertEquals(MessageDeliveryState.Sending, optimisticConv.messages[0].deliveryState)
        assertTrue("Message ID ${optimisticConv.messages[0].id} should start with local-", 
            optimisticConv.messages[0].id.startsWith("local-"))

        testDispatcher.scheduler.advanceUntilIdle()

        // 2. Check server message replacement
        val finalConv = viewModel.conversation
        assertNotNull(finalConv)
        assertEquals(1, finalConv!!.messages.size)
        assertEquals("server-msg-1", finalConv.messages[0].id)
        assertEquals(MessageDeliveryState.Sent, finalConv.messages[0].deliveryState)
    }
}
