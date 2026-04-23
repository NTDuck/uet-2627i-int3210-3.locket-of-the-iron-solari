package com.solari.app.ui.models

import java.io.Serializable

data class User(
    val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val nickname: String? = null
) : Serializable

data class BlockedUser(
    val user: User,
    val blockedAt: Long = System.currentTimeMillis()
) : Serializable
