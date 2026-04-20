package com.solari.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme

@Composable
fun SolariConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmColor: Color = Color(0xFFE57373),
    dismissText: String = "Cancel"
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SolariTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 12.dp,
            modifier = modifier.width(260.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = SolariTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = message,
                        color = SolariTheme.colors.tertiary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                DialogActionRow(
                    text = confirmText,
                    textColor = confirmColor,
                    shape = RoundedCornerShape(0.dp),
                    onClick = onConfirm
                )
                DialogActionRow(
                    text = dismissText,
                    textColor = SolariTheme.colors.onSurface,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 16.dp,
                        bottomStart = 16.dp
                    ),
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun DialogActionRow(
    text: String,
    textColor: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .background(Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
