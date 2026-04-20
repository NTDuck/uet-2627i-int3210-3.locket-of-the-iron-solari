package com.solari.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme

@Composable
fun SolariTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    labelFontSize: TextUnit = 12.sp,
    textFontSize: TextUnit = TextUnit.Unspecified,
    color: Color = SolariTheme.colors.tertiary
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            color = color,
            fontSize = labelFontSize,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            textStyle = TextStyle(fontFamily = PlusJakartaSans, fontSize = textFontSize, color = Color.White),
            placeholder = { Text(text = placeholder, color = Color.Gray, fontFamily = PlusJakartaSans) },
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SolariTheme.colors.surface,
                unfocusedContainerColor = SolariTheme.colors.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = SolariTheme.colors.onSurface,
                focusedTextColor = SolariTheme.colors.onSurface,
                unfocusedTextColor = SolariTheme.colors.onSurface
            ),
            singleLine = true
        )
    }
}
