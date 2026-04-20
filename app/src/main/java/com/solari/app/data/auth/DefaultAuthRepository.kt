package com.solari.app.data.auth

import android.database.sqlite.SQLiteException
import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.remote.auth.AuthApi
import com.solari.app.data.remote.auth.RefreshSessionRequestDto
import com.solari.app.data.remote.auth.SignInRequestDto
import com.solari.app.data.remote.auth.SignInResponseDto
import com.solari.app.data.security.TokenCipher
import java.security.GeneralSecurityException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val authSessionDao: AuthSessionDao,
    private val apiExecutor: ApiExecutor,
    private val tokenCipher: TokenCipher,
    private val recentEmojiStore: RecentEmojiStore
) : AuthRepository {
    override val currentSession: Flow<AuthSession?> =
        authSessionDao.observeCurrentSession().map { entity -> entity?.toDomainOrNull() }

    override suspend fun signIn(
        identifier: String,
        password: String
    ): ApiResult<AuthSession> {
        val normalizedIdentifier = identifier.trim()
        if (normalizedIdentifier.isEmpty() || password.isEmpty()) {
            return ApiResult.Failure(
                statusCode = null,
                type = "MISSING_CREDENTIALS",
                message = "Username/email and password are required."
            )
        }

        return when (
            val result = apiExecutor.execute {
                authApi.signIn(
                    SignInRequestDto(
                        identifier = normalizedIdentifier,
                        password = password
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                storeSession(result.data)
            }
        }
    }

    override suspend fun restoreSession(): ApiResult<AuthSession> {
        val currentSession = getCurrentSession()
            ?: return ApiResult.Failure(
                statusCode = null,
                type = "NO_STORED_SESSION",
                message = "No stored session found."
            )

        return when (
            val result = apiExecutor.execute {
                authApi.refreshSession(
                    RefreshSessionRequestDto(refreshToken = currentSession.refreshToken)
                )
            }
        ) {
            is ApiResult.Success -> storeSession(result.data)
            is ApiResult.Failure -> {
                if (result.statusCode == 400 || result.statusCode == 401) {
                    authSessionDao.clear()
                }
                result
            }
        }
    }

    override suspend fun getCurrentSession(): AuthSession? {
        return authSessionDao.getCurrentSession()?.toDomainOrNull()
    }

    override suspend fun clearSession() {
        authSessionDao.clear()
        recentEmojiStore.clear()
    }

    private suspend fun storeSession(response: SignInResponseDto): ApiResult<AuthSession> {
        val session = try {
            response.toEntity()
        } catch (error: GeneralSecurityException) {
            return sessionStorageFailure(error)
        }

        return try {
            authSessionDao.replace(session)
            ApiResult.Success(session.toDomain())
        } catch (error: SQLiteException) {
            sessionStorageFailure(error)
        } catch (error: GeneralSecurityException) {
            sessionStorageFailure(error)
        }
    }

    private fun SignInResponseDto.toEntity(): AuthSessionEntity {
        return AuthSessionEntity(
            sessionId = sessionId,
            accessTokenCiphertext = tokenCipher.encrypt(accessToken),
            refreshTokenCiphertext = tokenCipher.encrypt(refreshToken),
            expiresAt = expiresAt,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
    }

    private fun AuthSessionEntity.toDomain(): AuthSession {
        return AuthSession(
            sessionId = sessionId,
            accessToken = tokenCipher.decrypt(accessTokenCiphertext),
            refreshToken = tokenCipher.decrypt(refreshTokenCiphertext),
            expiresAt = expiresAt
        )
    }

    private fun AuthSessionEntity.toDomainOrNull(): AuthSession? {
        return runCatching { toDomain() }.getOrNull()
    }

    private fun sessionStorageFailure(cause: Throwable): ApiResult.Failure {
        return ApiResult.Failure(
            statusCode = null,
            type = "SESSION_STORAGE_ERROR",
            message = "Signed in, but the session could not be stored securely.",
            cause = cause
        )
    }
}
