package com.solari.app.data.websocket

import android.util.Log
import com.solari.app.ui.models.Message
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class WebSocketEventParserTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val parser = WebSocketEventParser(json)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.d(any<String>(), any<String>()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `parse NEW_MESSAGE returns NewMessage event`() {
        val rawText = """
            {
                "type": "NEW_MESSAGE",
                "payload": {
                    "conversationId": "conv-123",
                    "message": {
                        "id": "msg-456",
                        "conversationId": "conv-123",
                        "senderId": "user-789",
                        "content": "Hello world",
                        "createdAt": "2023-10-27T10:00:00Z"
                    }
                }
            }
        """.trimIndent()

        val event = parser.parse(rawText)

        assertNotNull(event)
        val newMessage = event as WebSocketEvent.NewMessage
        assertEquals("conv-123", newMessage.conversationId)
        assertEquals("msg-456", newMessage.message.id)
        assertEquals("user-789", newMessage.message.senderId)
        assertEquals("Hello world", newMessage.message.text)
    }

    @Test
    fun `parse POST_PROCESSED returns PostProcessed event`() {
        val rawText = """
            {
                "type": "POST_PROCESSED",
                "payload": {
                    "postId": "post-123",
                    "status": "ready"
                }
            }
        """.trimIndent()

        val event = parser.parse(rawText)

        assertNotNull(event)
        val postProcessed = event as WebSocketEvent.PostProcessed
        assertEquals("post-123", postProcessed.postId)
        assertEquals("ready", postProcessed.status)
    }

    @Test
    fun `parse POST_FAILED returns PostFailed event`() {
        val rawText = """
            {
                "type": "POST_FAILED",
                "payload": {
                    "postId": "post-123",
                    "error": "Failed to process image"
                }
            }
        """.trimIndent()

        val event = parser.parse(rawText)

        assertNotNull(event)
        val postFailed = event as WebSocketEvent.PostFailed
        assertEquals("post-123", postFailed.postId)
        assertEquals("Failed to process image", postFailed.error)
    }

    @Test
    fun `parse unknown event returns null`() {
        val rawText = """
            {
                "type": "UNKNOWN_TYPE",
                "payload": {}
            }
        """.trimIndent()

        val event = parser.parse(rawText)

        assertNull(event)
    }

    @Test
    fun `parse invalid json returns null`() {
        val rawText = "invalid json"

        val event = parser.parse(rawText)

        assertNull(event)
    }
}
