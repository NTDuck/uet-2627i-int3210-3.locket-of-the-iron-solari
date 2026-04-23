package com.solari.app.data.auth

enum class AuthSignInMethod(
    val apiValue: String
) {
    Password("password"),
    Google("google");

    companion object {
        fun fromApiValue(value: String?): AuthSignInMethod? {
            return entries.firstOrNull { it.apiValue == value }
        }
    }
}
