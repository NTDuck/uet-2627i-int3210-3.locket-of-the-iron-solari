package com.solari.app.data.network

import android.os.SystemClock
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

class ApiLatencyLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val endpoint = request.monitoredEndpointLabel()
            ?: return chain.proceed(request)

        val startedAtNanos = SystemClock.elapsedRealtimeNanos()
        return try {
            val response = chain.proceed(request)
            val elapsedMs = elapsedMillis(startedAtNanos)
            Log.d(
                LogTag,
                "API_LATENCY $endpoint ${elapsedMs}ms status=${response.code}"
            )
            response
        } catch (error: IOException) {
            val elapsedMs = elapsedMillis(startedAtNanos)
            Log.d(
                LogTag,
                "API_LATENCY $endpoint ${elapsedMs}ms status=NETWORK_ERROR error=${error.javaClass.simpleName}"
            )
            throw error
        }
    }

    private fun elapsedMillis(startedAtNanos: Long): String {
        val elapsedNanos = SystemClock.elapsedRealtimeNanos() - startedAtNanos
        val elapsedMs = TimeUnit.NANOSECONDS.toMicros(elapsedNanos) / 1_000.0
        return String.format(Locale.US, "%.1f", elapsedMs)
    }

    private fun okhttp3.Request.monitoredEndpointLabel(): String? {
        val method = method.uppercase(Locale.US)
        val segments = url.pathSegments

        return when {
            method == "POST" && segments == listOf("signin") ->
                "POST /signin"

            method == "GET" && segments == listOf("feed") ->
                "GET /feed"

            method == "POST" && segments == listOf("posts", "initiate") ->
                "POST /posts/initiate"

            method == "POST" && segments == listOf("posts", "finalize") ->
                "POST /posts/finalize"

            method == "POST" &&
                    segments.size == 3 &&
                    segments[0] == "conversations" &&
                    segments[2] == "messages" ->
                "POST /conversations/{id}/messages"

            else -> null
        }
    }

    private companion object {
        const val LogTag = "SolariApiLatency"
    }
}
