package com.solari.app.data.network

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    data class Failure(
        val statusCode: Int?,
        val type: String?,
        val message: String,
        val cause: Throwable? = null
    ) : ApiResult<Nothing>
}
