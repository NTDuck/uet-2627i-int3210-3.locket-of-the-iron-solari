package com.solari.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.solari.app.ui.theme.PlusJakartaSans

enum class SortSelection(val label: String, val apiValue: String?) {
    Default(label = "Default", apiValue = null),
    Newest(label = "Newest", apiValue = "newest"),
    Oldest(label = "Oldest", apiValue = "oldest")
}

@Composable
fun SortDropdownButton(
    selected: SortSelection,
    onSelected: (SortSelection) -> Unit,
    iconTint: Color,
    menuContainerColor: Color,
    menuContentColor: Color,
    modifier: Modifier = Modifier,
    iconSize: Int = 17
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Sort",
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(0, 28),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                var menuVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    menuVisible = true
                }
                AnimatedVisibility(
                    visible = menuVisible,
                    enter = fadeIn(animationSpec = tween(90)) + scaleIn(
                        initialScale = 0.7f,
                        transformOrigin = TransformOrigin(0.5f, 0f),
                        animationSpec = tween(140)
                    )
                ) {
                    Surface(
                        color = menuContainerColor,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 8.dp
                    ) {
                        Column {
                            SortSelection.entries.forEachIndexed { index, option ->
                                val optionShape = optionShapeForIndex(
                                    index = index,
                                    lastIndex = SortSelection.entries.lastIndex
                                )

                                Row(
                                    modifier = Modifier
                                        .widthIn(min = 132.dp)
                                        .height(40.dp)
                                        .clip(optionShape)
                                        .background(
                                            if (option == selected) {
                                                iconTint.copy(alpha = 0.12f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            expanded = false
                                            onSelected(option)
                                        }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (option == selected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = iconTint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = option.label,
                                        color = menuContentColor,
                                        fontSize = 14.sp,
                                        fontFamily = PlusJakartaSans
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun optionShapeForIndex(index: Int, lastIndex: Int): RoundedCornerShape {
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        )

        lastIndex -> RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomEnd = 8.dp,
            bottomStart = 8.dp
        )

        else -> RoundedCornerShape(0.dp)
    }
}
