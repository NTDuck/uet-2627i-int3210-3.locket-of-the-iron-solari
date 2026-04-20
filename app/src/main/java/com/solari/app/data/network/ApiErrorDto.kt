package com.solari.app.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponseDto(
    val error: ApiErrorDto
)

@Serializable
data class ApiErrorDto(
    val type: String,
    val message: String
)
