package com.solari.app.ui.models

data class User(
    val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val profileImageUrl: String = ""
)
