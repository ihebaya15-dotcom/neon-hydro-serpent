package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    baseColor: Color = NeonCyan,
    onMove: (x: Float, y: Float) -> Unit
) {
    val sizeDp = 140.dp
    val knobRadiusDp = 28.dp
    val baseRadiusDp = 60.dp

    val coroutineScope = rememberCoroutineScope()
    val knobOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("virtual_joystick")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val maxDist = baseRadiusDp.toPx()
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y)
                        val clampedOffset = if (dist > maxDist) {
                            delta * (maxDist / dist)
                        } else {
                            delta
                        }
                        coroutineScope.launch { knobOffset.snapTo(clampedOffset) }
                        val normX = (clampedOffset.x / maxDist).coerceIn(-1f, 1f)
                        val normY = (clampedOffset.y / maxDist).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val current = knobOffset.value + dragAmount
                        val maxDist = baseRadiusDp.toPx()
                        val dist = sqrt(current.x * current.x + current.y * current.y)
                        val clampedOffset = if (dist > maxDist) {
                            current * (maxDist / dist)
                        } else {
                            current
                        }
                        coroutineScope.launch { knobOffset.snapTo(clampedOffset) }
                        val normX = (clampedOffset.x / maxDist).coerceIn(-1f, 1f)
                        val normY = (clampedOffset.y / maxDist).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    },
                    onDragEnd = {
                        isDragging = false
                        coroutineScope.launch {
                            knobOffset.animateTo(Offset.Zero)
                        }
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            knobOffset.animateTo(Offset.Zero)
                        }
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = baseRadiusDp.toPx()
            val knobRadius = knobRadiusDp.toPx()

            // Outer Base Ring Background (Semi-transparent dark glass)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = if (isDragging) 0.25f else 0.12f),
                        baseColor.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.1f
                ),
                radius = baseRadius,
                center = center
            )

            // Outer Glowing Ring Border
            drawCircle(
                color = baseColor.copy(alpha = if (isDragging) 0.8f else 0.45f),
                radius = baseRadius,
                center = center,
                style = Stroke(width = if (isDragging) 3.5f else 2.5f)
            )

            // Cardinal direction crosshairs
            val tickLen = 8f
            drawLine(
                color = baseColor.copy(alpha = 0.4f),
                start = Offset(center.x, center.y - baseRadius + tickLen),
                end = Offset(center.x, center.y - baseRadius - tickLen),
                strokeWidth = 2f
            )
            drawLine(
                color = baseColor.copy(alpha = 0.4f),
                start = Offset(center.x, center.y + baseRadius - tickLen),
                end = Offset(center.x, center.y + baseRadius + tickLen),
                strokeWidth = 2f
            )
            drawLine(
                color = baseColor.copy(alpha = 0.4f),
                start = Offset(center.x - baseRadius + tickLen, center.y),
                end = Offset(center.x - baseRadius - tickLen, center.y),
                strokeWidth = 2f
            )
            drawLine(
                color = baseColor.copy(alpha = 0.4f),
                start = Offset(center.x + baseRadius - tickLen, center.y),
                end = Offset(center.x + baseRadius + tickLen, center.y),
                strokeWidth = 2f
            )

            // Inner Thumbstick Knob
            val currentKnobPos = center + knobOffset.value

            // Knob Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = if (isDragging) 0.6f else 0.35f),
                        NeonCyanGlow.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = currentKnobPos,
                    radius = knobRadius * 1.4f
                ),
                radius = knobRadius * 1.4f,
                center = currentKnobPos
            )

            // Knob Solid Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        baseColor,
                        baseColor.copy(alpha = 0.85f)
                    ),
                    center = currentKnobPos,
                    radius = knobRadius
                ),
                radius = knobRadius,
                center = currentKnobPos
            )

            // Knob Center Dot
            drawCircle(
                color = Color.White,
                radius = knobRadius * 0.28f,
                center = currentKnobPos
            )
        }
    }
}
