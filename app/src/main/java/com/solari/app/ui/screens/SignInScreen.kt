package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariTextField
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.auth.AuthSession
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.SignInViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun SignInScreen(
    viewModel: SignInViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onSignInComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) {
            viewModel.consumeSignedIn()
            onSignInComplete()
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
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SolariTextField(
                    value = uiState.emailOrUsername,
                    onValueChange = viewModel::onEmailOrUsernameChanged,
                    label = "Username or Email",
                    placeholder = "Email or username",
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp),
                    color = SolariTheme.colors.tertiary
                )

                SolariTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = SolariTheme.colors.tertiary
                )

                Text(
                    text = "Forgot password?",
                    color = SolariTheme.colors.secondary,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp)
                        .clickable { onNavigateToForgotPassword() }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    uiState.errorMessage?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SolariButton(
                    text = "Sign In",
                    onClick = viewModel::signIn,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .padding(bottom = 48.dp),
                    contentColor = Color(0xFF5F2900),
                    fontSize = 18.sp,
                )

                Text(
                    text = "New to Solari?",
                    color = SolariTheme.colors.tertiary,
                    fontFamily = PlusJakartaSans,
                    fontSize = 17.6.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                SolariButton(
                    text = "Create Account",
                    onClick = onNavigateToSignUp,
                    containerColor = Color(0xFF343538),
                    contentColor = Color(0xFFE3E2E6),
                    modifier = Modifier.fillMaxWidth(0.7f),
                    fontSize = 16.sp,
                )
            }

            if (uiState.isLoading) {
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

@Preview(
    name = "Sign In Screen",
    showBackground = true,
    backgroundColor = 0xFF111316
)
@Composable
private fun SignInScreenPreview() {
    val previewViewModel = remember {
        SignInViewModel(PreviewAuthRepository()).apply {
            onEmailOrUsernameChanged("alex@solari.app")
            onPasswordChanged("password")
        }
    }

    SolariTheme {
        SignInScreen(
            viewModel = previewViewModel,
            onNavigateBack = {},
            onNavigateToSignUp = {},
            onNavigateToForgotPassword = {},
            onSignInComplete = {}
        )
    }
}

private class PreviewAuthRepository : AuthRepository {
    override val currentSession: Flow<AuthSession?> = flowOf(null)

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

    override suspend fun restoreSession(): ApiResult<AuthSession> {
        return ApiResult.Failure(
            statusCode = null,
            type = "PREVIEW",
            message = "Preview mode does not restore sessions."
        )
    }

    override suspend fun getCurrentSession(): AuthSession? = null

    override suspend fun clearSession() = Unit
}
