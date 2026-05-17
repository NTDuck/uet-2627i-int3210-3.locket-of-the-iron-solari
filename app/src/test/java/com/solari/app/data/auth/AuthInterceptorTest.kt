package com.solari.app.data.auth

import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.local.auth.AuthSessionEntity
import com.solari.app.data.security.TokenCipher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {
    private val authSessionDao = mockk<AuthSessionDao>()
    private val tokenCipher = mockk<TokenCipher>()
    private lateinit var interceptor: AuthInterceptor

    @Before
    fun setup() {
        interceptor = AuthInterceptor(authSessionDao, tokenCipher)
    }

    @Test
    fun `intercept adds Authorization header when token exists`() {
        val request = Request.Builder()
            .url("https://api.example.com/data")
            .build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        
        val entity = AuthSessionEntity(
            sessionId = "sid",
            accessTokenCiphertext = "atc",
            refreshTokenCiphertext = "rtc",
            expiresAt = "2024-01-01T00:00:00Z",
            signInMethod = "email",
            updatedAtEpochMillis = 1000L
        )
        coEvery { authSessionDao.getCurrentSession() } returns entity
        coEvery { tokenCipher.decrypt("atc") } returns "valid_token"
        
        val response = mockk<Response>()
        every { chain.proceed(any()) } returns response

        interceptor.intercept(chain)

        io.mockk.verify {
            chain.proceed(withArg {
                assertEquals("Bearer valid_token", it.header("Authorization"))
            })
        }
    }

    @Test
    fun `intercept does not add Authorization header for unauthenticated paths`() {
        val request = Request.Builder()
            .url("https://api.example.com/signin")
            .build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        
        val response = mockk<Response>()
        every { chain.proceed(any()) } returns response

        interceptor.intercept(chain)

        io.mockk.verify {
            chain.proceed(withArg {
                assertNull(it.header("Authorization"))
            })
        }
    }
}
