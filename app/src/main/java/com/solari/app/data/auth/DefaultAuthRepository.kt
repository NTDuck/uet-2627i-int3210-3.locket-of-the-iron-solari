package com.solari.app.data.auth

import android.database.sqlite.SQLiteException
import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.auth.AuthApi
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
    private val tokenCipher: TokenCipher
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
                val session = try {
                    result.data.toEntity()
                } catch (error: GeneralSecurityException) {
                    return sessionStorageFailure(error)
                }

                try {
                    authSessionDao.replace(session)
                    ApiResult.Success(session.toDomain())
                } catch (error: SQLiteException) {
                    sessionStorageFailure(error)
                } catch (error: GeneralSecurityException) {
                    sessionStorageFailure(error)
                }
            }
        }
    }

    override suspend fun getCurrentSession(): AuthSession? {
        return authSessionDao.getCurrentSession()?.toDomainOrNull()
    }

    override suspend fun clearSession() {
        authSessionDao.clear()
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
