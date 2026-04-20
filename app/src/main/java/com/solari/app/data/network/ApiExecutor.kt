package com.solari.app.data.network

import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.Response

class ApiExecutor(
    private val json: Json
) {
    suspend fun <T : Any> execute(call: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body == null) {
                    ApiResult.Failure(
                        statusCode = response.code(),
                        type = "EMPTY_RESPONSE",
                        message = "Server returned an empty response."
                    )
                } else {
                    ApiResult.Success(body)
                }
            } else {
                response.toFailure(json)
            }
        } catch (error: IOException) {
            ApiResult.Failure(
                statusCode = null,
                type = "NETWORK_ERROR",
                message = "Network request failed. Check your connection and try again.",
                cause = error
            )
        } catch (error: SerializationException) {
            ApiResult.Failure(
                statusCode = null,
                type = "SERIALIZATION_ERROR",
                message = "Server response could not be parsed.",
                cause = error
            )
        }
    }

    private fun <T : Any> Response<T>.toFailure(json: Json): ApiResult.Failure {
        val rawError = errorBody()?.string()
        val parsedError = rawError?.let { body ->
            runCatching { json.decodeFromString<ApiErrorResponseDto>(body).error }.getOrNull()
        }

        return ApiResult.Failure(
            statusCode = code(),
            type = parsedError?.type ?: "HTTP_${code()}",
            message = parsedError?.message ?: message().ifBlank { "Request failed." }
        )
    }
}
