package com.solari.app.data.auth

import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.network.ApiExecutor
import com.solari.app.data.network.ApiResult
import com.solari.app.data.remote.auth.AuthApi
import com.solari.app.data.remote.auth.SignInResponseDto
import com.solari.app.data.security.TokenCipher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TokenRefreshAuthenticatorTest {
    private val authApi = mockk<AuthApi>()
    private val authSessionDao = mockk<AuthSessionDao>(relaxed = true)
    private val apiExecutor = mockk<ApiExecutor>()
    private val tokenCipher = mockk<TokenCipher>()
    private val sessionInvalidationNotifier = mockk<AuthSessionInvalidationNotifier>(relaxed = true)

    private lateinit var authenticator: TokenRefreshAuthenticator

    @Before
    fun setup() {
        authenticator = TokenRefreshAuthenticator(
            authApi,
            authSessionDao,
            apiExecutor,
            tokenCipher,
            sessionInvalidationNotifier
        )
    }

    @Test
    fun `authenticate successful refresh returns request with new token`() {
        val request = Request.Builder()
            .url("https://api.example.com/data")
            .header("Authorization", "Bearer old_at")
            .build()
        val response = mockk<Response>()
        every { response.request } returns request
        every { response.priorResponse } returns null

        val entity = AuthSessionEntity(
            sessionId = "sid",
            accessTokenCiphertext = "atc",
            refreshTokenCiphertext = "rtc",
            expiresAt = "2024-01-01T00:00:00Z",
            signInMethod = "email",
            updatedAtEpochMillis = 1000L
        )
        coEvery { authSessionDao.getCurrentSession() } returns entity
        coEvery { tokenCipher.decrypt("atc") } returns "old_at"
        coEvery { tokenCipher.decrypt("rtc") } returns "old_rt"
        
        val refreshResponse = SignInResponseDto(
            message = "Success",
            sessionId = "sid",
            accessToken = "new_at",
            refreshToken = "new_rt",
            expiresAt = "2024-01-01T01:00:00Z"
        )
        coEvery { apiExecutor.execute<SignInResponseDto>(any()) } returns ApiResult.Success(refreshResponse)
        coEvery { tokenCipher.encrypt("new_at") } returns "new_atc"
        coEvery { tokenCipher.encrypt("new_rt") } returns "new_rtc"

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNotNull(authenticatedRequest)
        assertEquals("Bearer new_at", authenticatedRequest?.header("Authorization"))
        coVerify { authSessionDao.replace(any()) }
    }

    @Test
    fun `authenticate refresh failure with 401 clears session`() {
        val request = Request.Builder()
            .url("https://api.example.com/data")
            .header("Authorization", "Bearer old_at")
            .build()
        val response = mockk<Response>()
        every { response.request } returns request
        every { response.priorResponse } returns null

        val entity = AuthSessionEntity(
            sessionId = "sid",
            accessTokenCiphertext = "atc",
            refreshTokenCiphertext = "rtc",
            expiresAt = "2024-01-01T00:00:00Z",
            signInMethod = "email",
            updatedAtEpochMillis = 1000L
        )
        coEvery { authSessionDao.getCurrentSession() } returns entity
        coEvery { tokenCipher.decrypt("atc") } returns "old_at"
        coEvery { tokenCipher.decrypt("rtc") } returns "old_rt"
        
        coEvery { apiExecutor.execute<SignInResponseDto>(any()) } returns ApiResult.Failure(401, "UNAUTHORIZED", "Invalid session")

        val authenticatedRequest = authenticator.authenticate(null, response)

        assertNull(authenticatedRequest)
        coVerify { authSessionDao.clear() }
        coVerify { sessionInvalidationNotifier.notifySessionInvalidated() }
    }
}
