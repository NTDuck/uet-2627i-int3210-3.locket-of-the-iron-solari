package com.solari.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.solari.app.ui.util.scaledClickable

@Composable
fun FilterToggleButton(
    selected: SortSelection,
    onToggle: (SortSelection) -> Unit,
    iconTint: Color,
    modifier: Modifier = Modifier,
    iconSize: Int = 17
) {
    val rotation by animateFloatAsState(
        targetValue = if (selected == SortSelection.Oldest) 180f else 0f,
        animationSpec = tween(400),
        label = "FilterToggleRotation"
    )

    Box(
        modifier = modifier
            .size(28.dp)
            .graphicsLayer {
                rotationX = rotation
            }
            .scaledClickable(pressedScale = 1.2f) {
                val next = when (selected) {
                    SortSelection.Oldest -> SortSelection.Newest
                    else -> SortSelection.Oldest
                }
                onToggle(next)
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Toggle Sort",
            tint = iconTint,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

enum class SortSelection(val label: String, val apiValue: String?) {
    Default(label = "Default", apiValue = null),
    Newest(label = "Newest", apiValue = "newest"),
    Oldest(label = "Oldest", apiValue = "oldest")
}
