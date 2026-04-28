package com.solari.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.solari.app.ui.theme.PlusJakartaSans
import com.solari.app.ui.theme.SolariTheme

private val DefaultAvatarBackground @Composable get() = SolariTheme.colors.surfaceVariant
private val DefaultAvatarText @Composable get() = SolariTheme.colors.onSurfaceVariant

@Composable
fun SolariAvatar(
    imageUrl: String?,
    username: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8),
    contentDescription: String? = null,
    fontSize: TextUnit = 18.sp
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(DefaultAvatarBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstLetter(),
                color = DefaultAvatarText,
                fontSize = fontSize,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun String.firstLetter(): String {
    return trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "?"
}
