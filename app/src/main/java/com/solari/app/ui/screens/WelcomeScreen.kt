package com.solari.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.solari.app.BuildConfig
import com.solari.app.R
import com.solari.app.ui.auth.GoogleIdTokenResult
import com.solari.app.ui.auth.requestGoogleIdToken
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariFeedbackPill
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.WelcomeViewModel
import kotlinx.coroutines.delay
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
                    color = SolariTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 80.dp)
                )

                SolariButton(
                    text = "Create an account",
                    onClick = onNavigateToSignUp,
                    buttonHeight = 67.dp,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    contentColor = SolariTheme.colors.onPrimary,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                )

                SolariButton(
                    text = "Sign in with Google",
                    onClick = ::submitGoogleSignIn,
                    enabled = !uiState.isGoogleSignInLoading,
                    containerColor = SolariTheme.colors.surfaceVariant,
                    contentColor = SolariTheme.colors.onBackground,
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
                        .background(SolariTheme.colors.background.copy(alpha = 0.42f))
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