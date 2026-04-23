package com.solari.app.data.auth

import com.solari.app.data.network.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSession: Flow<AuthSession?>

    suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): ApiResult<Unit>

    suspend fun signIn(
        identifier: String,
        password: String
    ): ApiResult<AuthSession>

    suspend fun signInWithGoogle(idToken: String): ApiResult<AuthSession>

    suspend fun requestPasswordReset(email: String): ApiResult<Unit>

    suspend fun verifyPasswordResetCode(email: String, code: String): ApiResult<Unit>

    suspend fun completePasswordReset(email: String, newPassword: String): ApiResult<Unit>

    suspend fun restoreSession(): ApiResult<AuthSession>

    suspend fun getCurrentSession(): AuthSession?

    suspend fun clearSession()
}
