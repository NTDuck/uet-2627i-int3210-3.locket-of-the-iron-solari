package com.solari.app.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed interface GoogleIdTokenResult {
    data class Success(val idToken: String) : GoogleIdTokenResult
    data class Failure(val message: String) : GoogleIdTokenResult
}

private const val GoogleSignInLogTag = "SolariGoogleSignIn"

suspend fun requestGoogleIdToken(
    context: Context,
    credentialManager: CredentialManager,
    serverClientId: String
): GoogleIdTokenResult {
    if (serverClientId.isBlank()) {
        Log.d(
            GoogleSignInLogTag,
            "Google sign-in failed before credential request: missing server client ID."
        )
        return GoogleIdTokenResult.Failure("Google sign-in is not configured.")
    }

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(
            GetSignInWithGoogleOption.Builder(serverClientId = serverClientId)
                .build()
        )
        .build()

    return try {
        val credential = credentialManager.getCredential(
            context = context,
            request = request
        ).credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            GoogleIdTokenResult.Success(googleCredential.idToken)
        } else {
            Log.d(
                GoogleSignInLogTag,
                "Google sign-in returned unsupported credential type: ${credential.type}."
            )
            GoogleIdTokenResult.Failure("Google sign-in returned an unsupported credential.")
        }
    } catch (error: GetCredentialCancellationException) {
        Log.d(GoogleSignInLogTag, "Google sign-in credential request canceled.", error)
        GoogleIdTokenResult.Failure("Google sign-in was canceled.")
    } catch (error: GoogleIdTokenParsingException) {
        Log.d(GoogleSignInLogTag, "Google sign-in ID token parsing failed.", error)
        GoogleIdTokenResult.Failure("Google sign-in returned an invalid ID token.")
    } catch (error: GetCredentialException) {
        Log.d(GoogleSignInLogTag, "Google sign-in credential request failed.", error)
        GoogleIdTokenResult.Failure(error.message ?: "Google sign-in failed.")
    }
}
