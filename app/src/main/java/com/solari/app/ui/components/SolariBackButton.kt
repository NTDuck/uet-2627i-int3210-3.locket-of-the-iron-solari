package com.solari.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.solari.app.ui.theme.SolariTheme
import com.solari.app.ui.util.scaledClickable

@Composable
fun SolariBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 24.dp, start = 16.dp)
            .scaledClickable(pressedScale = 1.2f, onClick = onClick)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = SolariTheme.colors.primary
        )
    }
}
