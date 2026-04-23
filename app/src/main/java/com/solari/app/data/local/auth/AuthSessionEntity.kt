package com.solari.app.data.local.auth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_sessions")
data class AuthSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "access_token_ciphertext")
    val accessTokenCiphertext: String,
    @ColumnInfo(name = "refresh_token_ciphertext")
    val refreshTokenCiphertext: String,
    @ColumnInfo(name = "expires_at")
    val expiresAt: String,
    @ColumnInfo(name = "sign_in_method")
    val signInMethod: String?,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long
)
