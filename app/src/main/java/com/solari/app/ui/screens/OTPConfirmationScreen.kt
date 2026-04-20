package com.solari.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.components.SolariButton
import com.solari.app.ui.viewmodels.OTPConfirmationViewModel

@Composable
fun OTPConfirmationScreen(
    viewModel: OTPConfirmationViewModel,
    onNavigateBack: () -> Unit,
    onConfirmComplete: () -> Unit
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
                text = "OTP has been sent to",
                color = Color.Gray,
                fontSize = 18.sp
            )
            Text(
                text = "bnetanyahu@knesset.gov.il.",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Please enter\nthe code to continue.",
                color = Color.Gray,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Gray, CircleShape)
                        )
                    }
                }
            }

            SolariButton(
                text = "Confirm Verification",
                onClick = onConfirmComplete,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Resend OTP",
                color = Color(0xFF4A90E2),
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Resend OTP */ }
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Didn't receive a code? Check your spam folder.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
