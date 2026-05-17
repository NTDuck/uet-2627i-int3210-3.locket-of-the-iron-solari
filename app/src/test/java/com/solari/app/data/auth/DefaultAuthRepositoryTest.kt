package com.solari.app.data.auth

import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.preferences.PushNotificationStore
import com.solari.app.data.preferences.RecentEmojiStore
import com.solari.app.data.remote.auth.AuthApi
import com.solari.app.data.remote.auth.SignInResponseDto
import com.solari.app.data.security.TokenCipher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DefaultAuthRepositoryTest {
    private val authApi = mockk<AuthApi>()
    private val authSessionDao = mockk<AuthSessionDao>(relaxed = true)
    private val apiExecutor = mockk<ApiExecutor>()
    private val tokenCipher = mockk<TokenCipher>()
    private val recentEmojiStore = mockk<RecentEmojiStore>(relaxed = true)
    private val sessionInvalidationNotifier = mockk<AuthSessionInvalidationNotifier>(relaxed = true)
    private val pushNotificationStore = mockk<PushNotificationStore>(relaxed = true)

    private lateinit var repository: DefaultAuthRepository

    @Before
    fun setup() {
        repository = DefaultAuthRepository(
            authApi,
            authSessionDao,
            apiExecutor,
            tokenCipher,
            recentEmojiStore,
            sessionInvalidationNotifier,
            pushNotificationStore
        )
    }

    @Test
    fun `signIn success stores session and returns success`() = runTest {
        val response = SignInResponseDto(
            message = "Success",
            sessionId = "sid",
            accessToken = "at",
            refreshToken = "rt",
            expiresAt = "2024-01-01T00:00:00Z"
        )
        coEvery { apiExecutor.execute<SignInResponseDto>(any()) } returns ApiResult.Success(response)
        coEvery { tokenCipher.encrypt(any()) } returns "encrypted"
        coEvery { tokenCipher.decrypt(any()) } returns "decrypted"

        val result = repository.signIn("user", "pass")

        assertTrue(result is ApiResult.Success)
        coVerify { authSessionDao.replace(any()) }
    }

    @Test
    fun `restoreSession failure with invalid session clears session`() = runTest {
        val entity = AuthSessionEntity(
            sessionId = "sid",
            accessTokenCiphertext = "atc",
            refreshTokenCiphertext = "rtc",
            expiresAt = "2024-01-01T00:00:00Z",
            signInMethod = "email",
            updatedAtEpochMillis = 1000L
        )
        coEvery { authSessionDao.getCurrentSession() } returns entity
        coEvery { tokenCipher.decrypt(any()) } returns "decrypted"
        coEvery { apiExecutor.execute<SignInResponseDto>(any()) } returns ApiResult.Failure(401, "UNAUTHORIZED", "Invalid session")

        val result = repository.restoreSession()

        assertTrue(result is ApiResult.Failure)
        coVerify { authSessionDao.clear() }
        coVerify { sessionInvalidationNotifier.notifySessionInvalidated() }
    }
}
