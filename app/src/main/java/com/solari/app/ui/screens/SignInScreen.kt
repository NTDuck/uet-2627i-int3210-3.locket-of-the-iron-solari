package com.solari.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.SignInViewModel

@Composable
fun SignInScreen(
    viewModel: SignInViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onSignInComplete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SolariTextField(
                value = viewModel.emailOrUsername,
                onValueChange = { viewModel.emailOrUsername = it },
                label = "Username or Email",
                placeholder = "Email or username",
                labelFontSize = 17.sp,
                textFontSize = 16.sp,
                modifier = Modifier.padding(bottom = 24.dp),
                color = SolariTheme.colors.tertiary
            )

            SolariTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
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

            Spacer(modifier = Modifier.height(48.dp))

            SolariButton(
                text = "Sign In",
                onClick = onSignInComplete,
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
        SignInViewModel().apply {
            emailOrUsername = "alex@solari.app"
            password = "password"
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
