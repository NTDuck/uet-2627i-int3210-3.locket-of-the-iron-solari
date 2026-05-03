package com.solari.app.data.auth

import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.auth.AuthApi
import com.solari.app.data.remote.auth.RefreshSessionRequestDto
import com.solari.app.data.remote.auth.SignInResponseDto
import com.solari.app.data.security.TokenCipher
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.security.GeneralSecurityException

class TokenRefreshAuthenticator(
    private val authApi: AuthApi,
    private val authSessionDao: AuthSessionDao,
    private val apiExecutor: ApiExecutor,
    private val tokenCipher: TokenCipher,
    private val sessionInvalidationNotifier: AuthSessionInvalidationNotifier
) : Authenticator {
    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (
            response.request.url.encodedPath in UnauthenticatedPaths ||
            response.responseCount >= MaxAuthAttempts
        ) {
            return null
        }

        val requestAccessToken = response.request.accessToken()
        return synchronized(refreshLock) {
            runBlocking {
                val currentSession = authSessionDao.getCurrentSession() ?: return@runBlocking null
                val currentAccessToken = currentSession.decryptAccessTokenOrClear()
                    ?: return@runBlocking null

                // Another in-flight request may have refreshed the session while this call waited.
                if (requestAccessToken == null || requestAccessToken != currentAccessToken) {
                    return@runBlocking response.request.withBearerToken(currentAccessToken)
                }

                val currentRefreshToken = currentSession.decryptRefreshTokenOrClear()
                    ?: return@runBlocking null

                when (
                    val refreshResult = apiExecutor.execute {
                        authApi.refreshSession(
                            RefreshSessionRequestDto(refreshToken = currentRefreshToken)
                        )
                    }
                ) {
                    is ApiResult.Success -> {
                        val refreshedSession = refreshResult.data.toEntity(
                            fallbackSignInMethod = currentSession.signInMethod
                        ) ?: return@runBlocking null
                        authSessionDao.replace(refreshedSession)
                        response.request.withBearerToken(refreshResult.data.accessToken)
                    }

                    is ApiResult.Failure -> {
                        if (refreshResult.isInvalidSessionFailure()) {
                            authSessionDao.clear()
                            sessionInvalidationNotifier.notifySessionInvalidated()
                        }
                        null
                    }
                }
            }
        }
    }

    private suspend fun AuthSessionEntity.decryptAccessTokenOrClear(): String? {
        return decryptOrClear(accessTokenCiphertext)
    }

    private suspend fun AuthSessionEntity.decryptRefreshTokenOrClear(): String? {
        return decryptOrClear(refreshTokenCiphertext)
    }

    private suspend fun decryptOrClear(ciphertext: String): String? {
        return try {
            tokenCipher.decrypt(ciphertext)
        } catch (error: GeneralSecurityException) {
            authSessionDao.clear()
            null
        }
    }

    private fun SignInResponseDto.toEntity(fallbackSignInMethod: String?): AuthSessionEntity? {
        return try {
            val refreshedSignInMethod = AuthSignInMethod.fromApiValue(signInMethod)?.apiValue
                ?: fallbackSignInMethod
            AuthSessionEntity(
                sessionId = sessionId,
                accessTokenCiphertext = tokenCipher.encrypt(accessToken),
                refreshTokenCiphertext = tokenCipher.encrypt(refreshToken),
                expiresAt = expiresAt,
                signInMethod = refreshedSignInMethod,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
        } catch (error: GeneralSecurityException) {
            null
        }
    }

    private fun Request.accessToken(): String? {
        return header("Authorization")
            ?.takeIf { it.startsWith(BearerPrefix) }
            ?.removePrefix(BearerPrefix)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun Request.withBearerToken(accessToken: String): Request {
        return newBuilder()
            .header("Authorization", "$BearerPrefix$accessToken")
            .build()
    }

    private val Response.responseCount: Int
        get() {
            var count = 1
            var priorResponse = priorResponse
            while (priorResponse != null) {
                count += 1
                priorResponse = priorResponse.priorResponse
            }
            return count
        }

    private companion object {
        const val BearerPrefix = "Bearer "
        const val MaxAuthAttempts = 2

        val UnauthenticatedPaths = setOf(
            "/signin",
            "/signin/google",
            "/signup",
            "/sessions/refresh",
            "/password-resets",
            "/password-resets/verify",
            "/password-resets/complete"
        )
    }

    private fun ApiResult.Failure.isInvalidSessionFailure(): Boolean {
        return statusCode == 400 ||
                statusCode == 401 ||
                statusCode == 404 ||
                type == "SESSION_NOT_FOUND" ||
                message.contains("session not found", ignoreCase = true)
    }
}
