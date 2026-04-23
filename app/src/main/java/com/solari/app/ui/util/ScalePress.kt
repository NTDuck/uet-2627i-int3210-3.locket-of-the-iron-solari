package com.solari.app.ui.util

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay

private const val ScalePressDurationMillis = 240
private const val MinimumVisiblePressMillis = 160L

fun Modifier.scaleOnPress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float,
    durationMillis: Int = ScalePressDurationMillis
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "ScaleOnPress"
    )

    this.scale(scale)
}

fun Modifier.scaledClickable(
    pressedScale: Float,
    enabled: Boolean = true,
    durationMillis: Int = ScalePressDurationMillis,
    scaleFromTouch: Boolean = false,
    onPressPosition: ((Offset) -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    var pressPosition by remember { mutableStateOf(Offset.Unspecified) }
    var elementSize by remember { mutableStateOf(IntSize.Zero) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "ScaledClickable"
    )
    val transformOrigin = remember(scaleFromTouch, pressPosition, elementSize) {
        if (!scaleFromTouch || !pressPosition.isSpecified || elementSize.width == 0 || elementSize.height == 0) {
            TransformOrigin.Center
        } else {
            TransformOrigin(
                pivotFractionX = (pressPosition.x / elementSize.width).coerceIn(0f, 1f),
                pivotFractionY = (pressPosition.y / elementSize.height).coerceIn(0f, 1f)
            )
        }
    }

    this
        .onSizeChanged { elementSize = it }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.transformOrigin = transformOrigin
        }
        .semantics {
            if (enabled) {
                onClick {
                    onClick()
                    true
                }
            }
        }
        .pointerInput(enabled, onClick, onPressPosition) {
            if (!enabled) return@pointerInput

            detectTapGestures(
                onPress = { offset ->
                    pressPosition = offset
                    onPressPosition?.invoke(offset)
                    isPressed = true
                    val startedAt = System.nanoTime()
                    val released = tryAwaitRelease()
                    val elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L

                    if (released) {
                        val remainingMillis = (MinimumVisiblePressMillis - elapsedMillis)
                            .coerceAtLeast(0L)
                        if (remainingMillis > 0L) {
                            delay(remainingMillis)
                        }
                        isPressed = false
                        onClick()
                    } else {
                        isPressed = false
                    }
                }
            )
        }
}
