package com.solari.app.data.auth

import android.database.sqlite.SQLiteException
import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.remote.auth.AuthApi
import com.solari.app.data.remote.auth.GoogleSignInRequestDto
import com.solari.app.data.remote.auth.PasswordResetCompleteRequestDto
import com.solari.app.data.remote.auth.PasswordResetRequestDto
import com.solari.app.data.remote.auth.PasswordResetVerifyRequestDto
import com.solari.app.data.remote.auth.RefreshSessionRequestDto
import com.solari.app.data.remote.auth.SignInRequestDto
import com.solari.app.data.remote.auth.SignInResponseDto
import com.solari.app.data.remote.auth.SignOutRequestDto
import com.solari.app.data.remote.auth.SignUpRequestDto
import com.solari.app.data.security.TokenCipher
import java.security.GeneralSecurityException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val authSessionDao: AuthSessionDao,
    private val apiExecutor: ApiExecutor,
    private val tokenCipher: TokenCipher,
    private val recentEmojiStore: RecentEmojiStore,
    private val sessionInvalidationNotifier: AuthSessionInvalidationNotifier
) : AuthRepository {
    override val currentSession: Flow<AuthSession?> =
        authSessionDao.observeCurrentSession().map { entity -> entity?.toDomainOrNull() }
    override val sessionInvalidationEvents = sessionInvalidationNotifier.events

    override suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): ApiResult<Unit> {
        val normalizedUsername = username.trim()
        val normalizedEmail = email.trim()
        if (normalizedUsername.isEmpty() || normalizedEmail.isEmpty() || password.isEmpty()) {
            return ApiResult.Failure(
                statusCode = null,
                type = "MISSING_SIGNUP_FIELDS",
                message = "Username, email, and password are required."
            )
        }

        return when (
            val result = apiExecutor.execute {
                authApi.signUp(
                    SignUpRequestDto(
                        username = normalizedUsername,
                        email = normalizedEmail,
                        password = password
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

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

    override suspend fun signInWithGoogle(idToken: String): ApiResult<AuthSession> {
        val normalizedIdToken = idToken.trim()
        if (normalizedIdToken.isEmpty()) {
            return ApiResult.Failure(
                statusCode = null,
                type = "MISSING_GOOGLE_ID_TOKEN",
                message = "Google sign-in did not return an ID token."
            )
        }

        return when (
            val result = apiExecutor.execute {
                authApi.signInWithGoogle(
                    GoogleSignInRequestDto(idToken = normalizedIdToken)
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> storeSession(result.data)
        }
    }

    override suspend fun requestPasswordReset(email: String): ApiResult<Unit> {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isEmpty()) {
            return ApiResult.Failure(
                statusCode = null,
                type = "MISSING_EMAIL",
                message = "Email is required."
            )
        }

        return when (
            val result = apiExecutor.execute {
                authApi.requestPasswordReset(PasswordResetRequestDto(email = normalizedEmail))
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
        }
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): ApiResult<Unit> {
        val normalizedEmail = email.trim()
        val normalizedCode = code.trim()
        if (normalizedEmail.isEmpty() || normalizedCode.isEmpty()) {
            return ApiResult.Failure(
                statusCode = null,
                type = "MISSING_RESET_CODE_FIELDS",
                message = "Email and reset code are required."
            )
        }

        return when (
            val result = apiExecutor.execute {
                authApi.verifyPasswordReset(
                    PasswordResetVerifyRequestDto(
                        email = normalizedEmail,
                        code = normalizedCode
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> {
                if (result.data.verified) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Failure(
                        statusCode = null,
                        type = "RESET_CODE_NOT_VERIFIED",
                        message = "Password reset code could not be verified."
                    )
                }
            }
        }
    }

    override suspend fun completePasswordReset(
        email: String,
        newPassword: String
    ): ApiResult<Unit> {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isEmpty() || newPassword.isEmpty()) {
            return ApiResult.Failure(
                statusCode = null,
                type = "MISSING_RESET_FIELDS",
                message = "Email and new password are required."
            )
        }

        return when (
            val result = apiExecutor.execute {
                authApi.completePasswordReset(
                    PasswordResetCompleteRequestDto(
                        email = normalizedEmail,
                        newPassword = newPassword
                    )
                )
            }
        ) {
            is ApiResult.Failure -> result
            is ApiResult.Success -> ApiResult.Success(Unit)
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
            is ApiResult.Success -> storeSession(
                response = result.data,
                fallbackSignInMethod = currentSession.signInMethod
            )
            is ApiResult.Failure -> {
                if (result.isInvalidSessionFailure()) {
                    authSessionDao.clear()
                    sessionInvalidationNotifier.notifySessionInvalidated()
                }
                result
            }
        }
    }

    override suspend fun signOut(deviceToken: String?): ApiResult<Unit> {
        val result = apiExecutor.execute {
            authApi.signOut(
                SignOutRequestDto(deviceToken = deviceToken?.trim()?.takeIf { it.isNotEmpty() })
            )
        }

        return when (result) {
            is ApiResult.Success -> {
                clearSession()
                ApiResult.Success(Unit)
            }

            is ApiResult.Failure -> {
                if (result.isSessionNotFoundFailure()) {
                    clearSession()
                    sessionInvalidationNotifier.notifySessionInvalidated()
                    result
                } else {
                    result
                }
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

    override fun clearSessionInvalidation() {
        sessionInvalidationNotifier.clear()
    }

    private suspend fun storeSession(
        response: SignInResponseDto,
        fallbackSignInMethod: AuthSignInMethod? = null
    ): ApiResult<AuthSession> {
        val session = try {
            response.toEntity(fallbackSignInMethod)
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

    private fun SignInResponseDto.toEntity(
        fallbackSignInMethod: AuthSignInMethod?
    ): AuthSessionEntity {
        val resolvedSignInMethod = AuthSignInMethod.fromApiValue(signInMethod)
            ?: fallbackSignInMethod
        return AuthSessionEntity(
            sessionId = sessionId,
            accessTokenCiphertext = tokenCipher.encrypt(accessToken),
            refreshTokenCiphertext = tokenCipher.encrypt(refreshToken),
            expiresAt = expiresAt,
            signInMethod = resolvedSignInMethod?.apiValue,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
    }

    private fun AuthSessionEntity.toDomain(): AuthSession {
        return AuthSession(
            sessionId = sessionId,
            accessToken = tokenCipher.decrypt(accessTokenCiphertext),
            refreshToken = tokenCipher.decrypt(refreshTokenCiphertext),
            expiresAt = expiresAt,
            signInMethod = AuthSignInMethod.fromApiValue(signInMethod)
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

    private fun ApiResult.Failure.isInvalidSessionFailure(): Boolean {
        return statusCode == 400 ||
                statusCode == 401 ||
                isSessionNotFoundFailure()
    }

    private fun ApiResult.Failure.isSessionNotFoundFailure(): Boolean {
        return statusCode == 404 ||
                type == "SESSION_NOT_FOUND" ||
                message.contains("session not found", ignoreCase = true)
    }
}
