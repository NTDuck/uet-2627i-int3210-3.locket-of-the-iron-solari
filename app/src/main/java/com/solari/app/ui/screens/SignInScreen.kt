package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariBackButton
import com.solari.app.ui.components.SolariTextField
import com.solari.app.data.auth.AuthRepository
import com.solari.app.data.auth.AuthSession
import com.solari.app.data.network.ApiResult
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable
import com.solari.app.ui.viewmodels.SignInViewModel
import com.solari.app.data.auth.AuthSessionInvalidationEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val identifierFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    var isPasswordVisible by remember { mutableStateOf(false) }

    fun submitSignIn() {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        viewModel.signIn()
    }

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
            SolariBackButton(onClick = onNavigateBack)

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
                    color = SolariTheme.colors.tertiary,
                    textFieldModifier = Modifier.focusRequester(identifierFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    )
                )

                SolariTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    isPasswordVisible = isPasswordVisible,
                    onPasswordVisibilityChange = { isPasswordVisible = it },
                    labelFontSize = 17.sp,
                    textFontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = SolariTheme.colors.tertiary,
                    textFieldModifier = Modifier.focusRequester(passwordFocusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { identifierFocusRequester.requestFocus() }
                    )
                )

                Text(
                    text = "Forgot password?",
                    color = SolariTheme.colors.secondary,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 0.dp)
                        .scaledClickable(
                            pressedScale = 1.08f,
                            onClick = onNavigateToForgotPassword
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
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
                    onClick = ::submitSignIn,
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
