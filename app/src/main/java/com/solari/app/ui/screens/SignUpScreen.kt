package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.SignUpViewModel

private val SignUpBackground = Color(0xFF111316)
private val SignUpInputBackground = Color(0xFF080B0E)
private val SignUpAvatarBackground = Color(0xFF303238)
private val SignUpPrimary = Color(0xFFFF8426)
private val SignUpPrimaryContent = Color(0xFF5F2900)
private val SignUpMutedText = Color(0xFFD7C0B2)
private val SignUpPlaceholderText = Color(0xFF3B3E45)
private val SignUpContentText = Color(0xFFE3E2E6)

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onSignUpComplete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SignUpBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .imePadding()
                .padding(horizontal = 28.dp)
                .padding(top = 52.dp, bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(SignUpAvatarBackground, RoundedCornerShape(8.dp))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Choose Avatar",
                        tint = SignUpMutedText,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column {
                    Text(
                        text = "OPTIONAL",
                        fontSize = 15.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = SignUpMutedText
                    )
                    Text(
                        text = "Choose Avatar",
                        fontSize = 22.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        color = SignUpContentText
                    )
                }
            }

            SignUpTextField(
                value = viewModel.username,
                onValueChange = { viewModel.username = it },
                label = "Username",
                placeholder = "john_netanyahu",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SignUpTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = "Email Address",
                placeholder = "bnetanyahu@knesset.gov.il",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SignUpTextField(
                value = viewModel.displayName,
                onValueChange = { viewModel.displayName = it },
                label = "Display Name (Optional)",
                placeholder = "John Netanyahu",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SignUpTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = "Password",
                placeholder = "••••••••",
                isPassword = true,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SignUpTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = "Confirm",
                placeholder = "••••••••",
                isPassword = true,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            SolariButton(
                text = "Create Account",
                onClick = onSignUpComplete,
                containerColor = SignUpPrimary,
                contentColor = SignUpPrimaryContent,
                buttonHeight = 64.dp,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.7f)
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    color = SignUpMutedText,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Log In",
                    color = SignUpPrimary,
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}

@Composable
private fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    fieldHeight: Dp = 48.dp
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = SignUpMutedText,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .background(SignUpInputBackground, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp),
            textStyle = TextStyle(
                color = SignUpContentText,
                fontFamily = PlusJakartaSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            cursorBrush = SolidColor(SignUpContentText),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = SignUpPlaceholderText,
                            fontFamily = PlusJakartaSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview(
    name = "Sign Up Screen",
    showBackground = true,
    backgroundColor = 0xFF111316
)
@Composable
private fun SignUpScreenPreview() {
    val previewViewModel = remember { SignUpViewModel() }

    SolariTheme {
        SignUpScreen(
            viewModel = previewViewModel,
            onNavigateBack = {},
            onNavigateToSignIn = {},
            onSignUpComplete = {}
        )
    }
}
