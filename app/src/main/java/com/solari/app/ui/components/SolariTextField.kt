package com.solari.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.solari.app.ui.util.scaledClickable

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
    color: Color = SolariTheme.colors.tertiary,
    textFieldModifier: Modifier = Modifier,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityChange: ((Boolean) -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Text(
                text = label.uppercase(),
                color = color,
                fontSize = labelFontSize,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .then(textFieldModifier)
                .fillMaxWidth()
                .height(56.dp),
            textStyle = TextStyle(
                fontFamily = PlusJakartaSans,
                fontSize = textFontSize,
                color = Color.White
            ),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontFamily = PlusJakartaSans
                )
            },
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword && onPasswordVisibilityChange != null) {
                {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scaledClickable(
                                pressedScale = 1.2f,
                                onClick = { onPasswordVisibilityChange(!isPasswordVisible) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (isPasswordVisible) {
                                "Hide password"
                            } else {
                                "Show password"
                            },
                            tint = SolariTheme.colors.onSurface.copy(alpha = 0.72f)
                        )
                    }
                }
            } else {
                null
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
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true
        )
    }
}
