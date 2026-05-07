package com.solari.app.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class TokenCipher {
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val ciphertext = Base64.encodeToString(
            cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8)),
            Base64.NO_WRAP
        )

        return "$PAYLOAD_VERSION:$iv:$ciphertext"
    }

    fun decrypt(payload: String): String {
        val parts = payload.split(":")
        if (parts.size != 3 || parts[0] != PAYLOAD_VERSION) {
            throw GeneralSecurityException("Unsupported token payload format.")
        }

        val iv = Base64.decode(parts[1], Base64.NO_WRAP)
        val ciphertext = Base64.decode(parts[2], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        )

        return String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(KEY_ALIAS, null)
        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "solari.auth.session.v1"
        const val PAYLOAD_VERSION = "v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
