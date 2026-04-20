package com.solari.app.ui.models

data class User(
    val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val profileImageUrl: String = "https://www.politicon.com/wp-content/uploads/2017/06/Charlie-Kirk-2019-1024x1024.jpg"
)
