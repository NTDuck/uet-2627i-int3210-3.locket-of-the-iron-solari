package com.solari.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.R
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.viewmodels.WelcomeViewModel

@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SolariTheme.colors.background
    ) {
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
                onClick = { },
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
                    modifier = Modifier.clickable { onNavigateToSignIn() }
                )
            }
        }
    }
}

@Preview(
    name = "Welcome Screen",
    showBackground = true,
    backgroundColor = 0xFF111316
)
@Composable
private fun WelcomeScreenPreview() {
    val previewViewModel = remember { WelcomeViewModel() }

    SolariTheme {
        WelcomeScreen(
            viewModel = previewViewModel,
            onNavigateToSignUp = {},
            onNavigateToSignIn = {}
        )
    }
}
