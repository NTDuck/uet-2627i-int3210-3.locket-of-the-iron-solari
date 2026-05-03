package com.solari.app.data.websocket

import android.util.Log
import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.security.TokenCipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

/**
 * Manages a single persistent WebSocket connection to the backend.
 *
 * Design decisions:
 * - Uses a dedicated OkHttpClient with short timeouts and no interceptors
 *   (auth is handled via query-less header on the upgrade request).
 * - Exponential backoff with jitter for reconnection (1s -> 2s -> 4s -> ... -> 30s cap).
 * - SharedFlow with replay=0 and extraBufferCapacity=64 so events are not dropped
 *   when collectors are briefly suspended (e.g. during recomposition).
 * - Thread-safe via AtomicBoolean/AtomicInteger guards + single coroutine scope.
 */
class WebSocketManager(
    private val baseUrl: String,
    private val authSessionDao: AuthSessionDao,
    private val tokenCipher: TokenCipher,
    private val json: Json,
    private val eventParser: WebSocketEventParser
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableSharedFlow<WebSocketEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val events: SharedFlow<WebSocketEvent> = _events.asSharedFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MINUTES) // WebSocket keep-alive relies on pings, not read timeout
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private val isConnecting = AtomicBoolean(false)
    private val consecutiveFailures = AtomicInteger(0)
    private val isIntentionallyClosed = AtomicBoolean(false)

    /**
     * Opens (or re-opens) the WebSocket connection.
     * Safe to call multiple times; concurrent calls are coalesced.
     */
    fun connect() {
        if (isConnecting.getAndSet(true)) return
        isIntentionallyClosed.set(false)
        reconnectJob?.cancel()
        reconnectJob = null

        scope.launch {
            try {
                val accessToken = resolveAccessToken()
                if (accessToken == null) {
                    Log.w(Tag, "No valid access token; skipping WebSocket connection")
                    isConnecting.set(false)
                    return@launch
                }

                val wsUrl = buildWsUrl()
                val request = Request.Builder()
                    .url(wsUrl)
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                // Close any existing connection before opening a new one
                webSocket?.close(NormalClosureCode, "Reconnecting")
                webSocket = client.newWebSocket(request, Listener())
                Log.d(Tag, "WebSocket connection initiated to $wsUrl")
            } catch (e: Exception) {
                Log.e(Tag, "WebSocket connect error: ${e.message}")
                isConnecting.set(false)
                scheduleReconnect()
            }
        }
    }

    /**
     * Gracefully closes the WebSocket. No reconnect will be attempted.
     */
    fun disconnect() {
        isIntentionallyClosed.set(true)
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket?.close(NormalClosureCode, "Client disconnect")
        webSocket = null
        consecutiveFailures.set(0)
        isConnecting.set(false)
        Log.d(Tag, "WebSocket disconnected intentionally")
    }

    fun sendTypingState(
        conversationId: String,
        receiverId: String,
        isTyping: Boolean
    ): Boolean {
        val socket = webSocket
        if (socket == null) {
            Log.d(Tag, "Skipping typing state send; WebSocket is not connected")
            return false
        }

        val action = WebSocketActionDto(
            action = "SEND_TYPING_STATE",
            payload = json.encodeToJsonElement(
                SendTypingStatePayloadDto(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    isTyping = isTyping
                )
            )
        )
        return socket.send(json.encodeToString(action))
    }

    private fun scheduleReconnect() {
        if (isIntentionallyClosed.get()) return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val failures = consecutiveFailures.getAndIncrement()
            val baseDelay = min(
                InitialReconnectDelayMs * (1L shl min(failures, MaxBackoffShift)),
                MaxReconnectDelayMs
            )
            // Add jitter: 0-25% of the base delay
            val jitter = (baseDelay * 0.25 * Math.random()).toLong()
            val delayMs = baseDelay + jitter
            Log.d(Tag, "Scheduling reconnect in ${delayMs}ms (attempt ${failures + 1})")
            delay(delayMs)
            isConnecting.set(false)
            connect()
        }
    }

    private suspend fun resolveAccessToken(): String? {
        val session = authSessionDao.getCurrentSession() ?: return null
        return runCatching { tokenCipher.decrypt(session.accessTokenCiphertext) }.getOrNull()
    }

    /**
     * Converts the REST base URL (http/https) to the corresponding WebSocket URL (ws/wss).
     */
    private fun buildWsUrl(): String {
        val trimmed = baseUrl.trimEnd('/')
        val wsBase = when {
            trimmed.startsWith("https://") -> trimmed.replaceFirst("https://", "wss://")
            trimmed.startsWith("http://") -> trimmed.replaceFirst("http://", "ws://")
            else -> trimmed
        }
        return "$wsBase/ws"
    }

    private inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(Tag, "WebSocket connected (HTTP ${response.code})")
            isConnecting.set(false)
            consecutiveFailures.set(0)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val event = eventParser.parse(text) ?: return
            val emitted = _events.tryEmit(event)
            if (!emitted) {
                Log.w(
                    Tag,
                    "WebSocket event buffer full; dropping event: ${event::class.simpleName}"
                )
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(Tag, "WebSocket closing: $code $reason")
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(Tag, "WebSocket closed: $code $reason")
            isConnecting.set(false)
            this@WebSocketManager.webSocket = null
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(Tag, "WebSocket failure: ${t.message} (HTTP ${response?.code})")
            isConnecting.set(false)
            this@WebSocketManager.webSocket = null
            scheduleReconnect()
        }
    }

    private companion object {
        const val Tag = "WebSocketManager"
        const val NormalClosureCode = 1000
        const val InitialReconnectDelayMs = 1_000L
        const val MaxReconnectDelayMs = 30_000L

        // Cap exponent to avoid overflow: 2^4 = 16, so max base = 16_000ms before cap
        const val MaxBackoffShift = 4
    }
}
