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
        .readTimeout(0, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private val isConnecting = AtomicBoolean(false)
    private val consecutiveFailures = AtomicInteger(0)
    private val isIntentionallyClosed = AtomicBoolean(false)

    fun connect() {
        if (isConnecting.getAndSet(true)) return
        isIntentionallyClosed.set(false)
        reconnectJob?.cancel()
        reconnectJob = null

        scope.launch {
            try {
                val accessToken = resolveAccessToken()
                if (accessToken == null) {
                    isConnecting.set(false)
                    return@launch
                }

                val wsUrl = buildWsUrl()
                val request = Request.Builder()
                    .url(wsUrl)
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                webSocket?.close(NORMAL_CLOSURE_CODE, "Reconnecting")
                webSocket = client.newWebSocket(request, Listener())
                Log.d(TAG, "WebSocket connection initiated to $wsUrl")
            } catch (e: Exception) {
                Log.e(TAG, "WebSocket connect error: ${e.message}")
                isConnecting.set(false)
                scheduleReconnect()
            }
        }
    }

    fun disconnect() {
        isIntentionallyClosed.set(true)
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket?.close(NORMAL_CLOSURE_CODE, "Client disconnect")
        webSocket = null
        consecutiveFailures.set(0)
        isConnecting.set(false)
        Log.d(TAG, "WebSocket disconnected intentionally")
    }

    fun sendTypingState(
        conversationId: String,
        receiverId: String,
        isTyping: Boolean
    ): Boolean {
        val socket = webSocket
        if (socket == null) {
            Log.d(TAG, "Skipping typing state send; WebSocket is not connected")
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
                INITIAL_RECONNECT_DELAY_MS * (1L shl min(failures, MAX_BACKOFF_SHIFT)),
                MAX_RECONNECT_DELAY_MS
            )
            val jitter = (baseDelay * 0.25 * Math.random()).toLong()
            val delayMs = baseDelay + jitter
            Log.d(TAG, "Scheduling reconnect in ${delayMs}ms (attempt ${failures + 1})")
            delay(delayMs)
            isConnecting.set(false)
            connect()
        }
    }

    private suspend fun resolveAccessToken(): String? {
        val session = authSessionDao.getCurrentSession() ?: return null
        return runCatching { tokenCipher.decrypt(session.accessTokenCiphertext) }.getOrNull()
    }

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
            Log.d(TAG, "WebSocket connected (HTTP ${response.code})")
            isConnecting.set(false)
            consecutiveFailures.set(0)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val event = eventParser.parse(text) ?: return
            val emitted = _events.tryEmit(event)
            if (!emitted) {
                Log.w(
                    TAG,
                    "WebSocket event buffer full; dropping event: ${event::class.simpleName}"
                )
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code $reason")
            webSocket.close(code, reason)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code $reason")
            isConnecting.set(false)
            this@WebSocketManager.webSocket = null
            scheduleReconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message} (HTTP ${response?.code})")
            isConnecting.set(false)
            this@WebSocketManager.webSocket = null
            scheduleReconnect()
        }
    }

    private companion object {
        const val TAG = "WebSocketManager"
        const val NORMAL_CLOSURE_CODE = 1000
        const val INITIAL_RECONNECT_DELAY_MS = 1_000L
        const val MAX_RECONNECT_DELAY_MS = 30_000L
        const val MAX_BACKOFF_SHIFT = 4
    }
}
