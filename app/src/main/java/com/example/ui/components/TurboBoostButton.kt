package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonMagenta

@Composable
fun TurboBoostButton(
    modifier: Modifier = Modifier,
    color: Color = NeonMagenta,
    currentMass: Float,
    minBoostMass: Float = 22f,
    onBoostChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.92f else 1.0f, label = "boostScale")
    val canBoost = currentMass > minBoostMass

    val buttonSize = 84.dp

    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale)
            .testTag("turbo_boost_button")
            .pointerInput(canBoost) {
                if (!canBoost) {
                    onBoostChange(false)
                    return@pointerInput
                }
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    onBoostChange(true)

                    waitForUpOrCancellation()
                    isPressed = false
                    onBoostChange(false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(buttonSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width / 2f) - 6f

            // Outer Radial Glow
            val glowColor = if (canBoost) color else Color.Gray
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isPressed) 0.5f else 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.3f
                ),
                radius = radius * 1.3f,
                center = center
            )

            // Button Core Fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (canBoost) {
                        listOf(
                            if (isPressed) Color.White else color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.6f),
                            Color(0xFF150A20)
                        )
                    } else {
                        listOf(Color(0xFF2A2A3A), Color(0xFF1A1A24))
                    },
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Outer Ring Stroke
            drawCircle(
                color = if (canBoost) color.copy(alpha = if (isPressed) 1f else 0.7f) else Color.DarkGray,
                radius = radius,
                center = center,
                style = Stroke(width = if (isPressed) 4f else 2.5f)
            )

            // Mass Fuel Gauge Arc
            if (canBoost) {
                val massRatio = ((currentMass - minBoostMass) / 100f).coerceIn(0.1f, 1f)
                val sweepAngle = massRatio * 360f
                drawArc(
                    color = Color.White.copy(alpha = 0.85f),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 3f)
                )
            }
        }

        // Inner Lightning Bolt Icon
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Turbo Boost",
            tint = if (canBoost) Color.White else Color.Gray,
            modifier = Modifier.size(42.dp)
        )
    }
}
