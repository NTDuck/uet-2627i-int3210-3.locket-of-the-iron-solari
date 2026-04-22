package com.solari.app.data.auth

data class AuthSession(
    val sessionId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String,
    val signInMethod: AuthSignInMethod?
)
