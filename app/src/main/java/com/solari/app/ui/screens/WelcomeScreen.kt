package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.solari.app.BuildConfig
import com.solari.app.R
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.auth.AuthSession
import com.solari.app.data.auth.AuthSessionInvalidationEvent
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.auth.GoogleIdTokenResult
import com.solari.app.ui.auth.requestGoogleIdToken
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.WelcomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onGoogleSignInComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.feedback?.sequence) {
        val feedback = uiState.feedback ?: return@LaunchedEffect
        delay(if (feedback.isSuccess) 1_200 else 1_600)
        viewModel.clearFeedback(feedback.sequence)
    }

    LaunchedEffect(uiState.isSignedInWithGoogle) {
        if (uiState.isSignedInWithGoogle) {
            delay(800)
            viewModel.consumeSignedInWithGoogle()
            onGoogleSignInComplete()
        }
    }

    fun submitGoogleSignIn() {
        if (!viewModel.onGoogleCredentialRequestStarted()) return

        coroutineScope.launch {
            when (
                val result = requestGoogleIdToken(
                    context = context,
                    credentialManager = credentialManager,
                    serverClientId = BuildConfig.SOLARI_GOOGLE_SERVER_CLIENT_ID
                )
            ) {
                is GoogleIdTokenResult.Success -> viewModel.signInWithGoogle(result.idToken)
                is GoogleIdTokenResult.Failure -> viewModel.onGoogleCredentialRequestFailed(result.message)
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Solari",
                    fontSize = 64.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.ExtraBold,
                    color = SolariTheme.colors.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Live pics from your friends,\nright on your home screen.",
                    fontSize = 18.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 80.dp)
                )

                SolariButton(
                    text = "Create an account",
                    onClick = onNavigateToSignUp,
                    buttonHeight = 67.dp,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    contentColor = Color(0xFF5E2800),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                )

                SolariButton(
                    text = "Sign in with Google",
                    onClick = ::submitGoogleSignIn,
                    enabled = !uiState.isGoogleSignInLoading,
                    containerColor = Color(0xFF343538),
                    contentColor = Color.White,
                    buttonHeight = 67.dp,
                    fontSize = 18.sp,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 24.dp)
                )

                Row {
                    Text(
                        text = "Already have an account? ",
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SolariTheme.colors.tertiary,
                    )
                    Text(
                        text = "Sign in",
                        fontFamily = PlusJakartaSans,
                        fontSize = 16.sp,
                        color = SolariTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.scaledClickable(
                            pressedScale = 1.08f,
                            onClick = onNavigateToSignIn
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = uiState.feedback != null,
                enter = slideInVertically(
                    animationSpec = tween(durationMillis = 260),
                    initialOffsetY = { -it * 2 }
                ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 220),
                    targetOffsetY = { -it * 2 }
                ) + fadeOut(animationSpec = tween(durationMillis = 160)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                val feedback = uiState.feedback
                if (feedback != null) {
                    SolariFeedbackPill(
                        message = feedback.message,
                        isSuccess = feedback.isSuccess
                    )
                }
            }

            if (uiState.isGoogleSignInLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = SolariTheme.colors.primary,
                        trackColor = SolariTheme.colors.surface
                    )
                }
            }
        }
    }
}

private class WelcomePreviewAuthRepository : AuthRepository {
    override val currentSession: Flow<AuthSession?> = flowOf(null)
    override val sessionInvalidationEvents: StateFlow<AuthSessionInvalidationEvent?> =
        MutableStateFlow(null)

    override suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): ApiResult<Unit> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not sign up."
        )
    }

    override suspend fun signIn(
        identifier: String,
        password: String
    ): ApiResult<AuthSession> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not sign in."
        )
    }

    override suspend fun signInWithGoogle(idToken: String): ApiResult<AuthSession> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not sign in with Google."
        )
    }

    override suspend fun requestPasswordReset(email: String): ApiResult<Unit> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not request password reset codes."
        )
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): ApiResult<Unit> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not verify password reset codes."
        )
    }

    override suspend fun completePasswordReset(
        email: String,
        newPassword: String
    ): ApiResult<Unit> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not complete password resets."
        )
    }

    override suspend fun restoreSession(): ApiResult<AuthSession> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not restore sessions."
        )
    }

    override suspend fun signOut(deviceToken: String?): ApiResult<Unit> {
        return ApiResult.Success(Unit)
    }

    override suspend fun getCurrentSession(): AuthSession? = null

    override suspend fun clearSession() = Unit

    override fun clearSessionInvalidation() = Unit
}

@Preview(
    name = "Welcome Screen",
    showBackground = true,
    backgroundColor = 0xFF111316
)
@Composable
private fun WelcomeScreenPreview() {
    val previewViewModel = remember { WelcomeViewModel(WelcomePreviewAuthRepository()) }

    SolariTheme {
        WelcomeScreen(
            viewModel = previewViewModel,
            onNavigateToSignUp = {},
            onNavigateToSignIn = {},
            onGoogleSignInComplete = {}
        )
    }
}
