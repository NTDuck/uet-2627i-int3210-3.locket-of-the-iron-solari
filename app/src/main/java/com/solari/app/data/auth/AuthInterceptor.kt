package com.solari.app.data.auth

import com.solari.app.data.local.auth.AuthSessionDao
import com.solari.app.data.security.TokenCipher
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authSessionDao: AuthSessionDao,
    private val tokenCipher: TokenCipher
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken = runBlocking {
            authSessionDao.getCurrentSession()?.let { session ->
                runCatching { tokenCipher.decrypt(session.accessTokenCiphertext) }.getOrNull()
            }
        }

        val authenticatedRequest = if (accessToken == null) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        return chain.proceed(authenticatedRequest)
    }
}
