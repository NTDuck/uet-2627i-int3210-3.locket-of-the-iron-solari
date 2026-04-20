package com.solari.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariTextField
import com.solari.app.ui.viewmodels.PasswordRecoveryViewModel

@Composable
fun PasswordRecoveryScreen(
    viewModel: PasswordRecoveryViewModel,
    onNavigateBack: () -> Unit,
    onGetRecoveryCode: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Solari",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 48.dp).align(Alignment.CenterHorizontally)
            )

            SolariTextField(
                value = viewModel.emailOrUsername,
                onValueChange = { viewModel.emailOrUsername = it },
                label = "Username or Email",
                placeholder = "e.g. user@example.com",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SolariButton(
                text = "GET RECOVERY CODE",
                onClick = onGetRecoveryCode
            )
        }
    }
}
