package com.solari.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme

@Composable
fun SolariInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "OK",
    dismissOnBackOrOutside: Boolean = true,
    messageTextAlign: TextAlign = TextAlign.Center,
    width: Dp = 310.dp,
    titleFontSize: TextUnit = 18.sp,
    messageFontSize: TextUnit = 15.sp,
    messageLineHeight: TextUnit = 23.sp,
    actionFontSize: TextUnit = 15.sp
) {
    Dialog(
        onDismissRequest = {
            if (dismissOnBackOrOutside) {
                onDismiss()
            }
        }
    ) {
        Surface(
            color = SolariTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 12.dp,
            modifier = modifier.width(width)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = title,
                        color = SolariTheme.colors.onSurface,
                        fontSize = titleFontSize,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = message,
                        color = SolariTheme.colors.tertiary,
                        fontSize = messageFontSize,
                        lineHeight = messageLineHeight,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium,
                        textAlign = messageTextAlign,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomEnd = 16.dp,
                                bottomStart = 16.dp
                            )
                        )
                        .background(Color.Transparent)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = confirmText,
                        color = SolariTheme.colors.primary,
                        fontSize = actionFontSize,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
