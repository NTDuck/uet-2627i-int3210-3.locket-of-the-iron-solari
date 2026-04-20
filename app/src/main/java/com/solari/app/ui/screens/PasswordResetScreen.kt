package com.solari.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.components.SolariTextField
import com.solari.app.ui.viewmodels.PasswordResetViewModel

@Composable
fun PasswordResetScreen(
    viewModel: PasswordResetViewModel,
    showTopBar: Boolean = true,
    onNavigateBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            if (showTopBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = if (!showTopBar) Arrangement.Center else Arrangement.Top
        ) {
            SolariTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = "Old Password",
                placeholder = "••••••••",
                isPassword = true,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SolariTextField(
                value = viewModel.newPassword,
                onValueChange = { viewModel.newPassword = it },
                label = "New Password",
                placeholder = "••••••••",
                isPassword = true,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SolariTextField(
                value = viewModel.confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = "Confirm New Password",
                placeholder = "••••••••",
                isPassword = true,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            SolariButton(
                text = "Reset",
                onClick = onResetComplete
            )
        }
    }
}
