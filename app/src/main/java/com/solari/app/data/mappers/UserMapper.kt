package com.solari.app.data.mappers

import com.solari.app.data.remote.common.ApiUserDto
import com.solari.app.ui.models.User

fun ApiUserDto.toUiUser(): User {
    return User(
        id = id,
        displayName = displayName ?: username,
        username = username,
        email = email.orEmpty(),
        profileImageUrl = avatarUrl.orEmpty()
    )
}
