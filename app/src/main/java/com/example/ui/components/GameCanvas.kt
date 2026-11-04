package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.BionicPlanktonType
import com.example.game.GameEngine
import com.example.game.GameMath
import com.example.game.Serpent
import com.example.ui.theme.AbyssDark
import com.example.ui.theme.AbyssNavy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(8f, 0f, 0f, android.graphics.Color.CYAN)
        }
    }

    val namePaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(200, 240, 240, 255)
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val iconPaint = remember {
        Paint().apply {
            textSize = 26f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val canvasCenter = Offset(canvasWidth / 2f, canvasHeight / 2f)

        // Camera smoothly follows player head
        val cameraPos = engine.player.head
        val zoom = (1.0f - ((engine.player.mass - 50f) / 1200f).coerceIn(0f, 0.42f))

        fun worldToScreen(worldPos: Offset): Offset {
            return Offset(
                canvasCenter.x + (worldPos.x - cameraPos.x) * zoom,
                canvasCenter.y + (worldPos.y - cameraPos.y) * zoom
            )
        }

        fun isVisible(screenPos: Offset, margin: Float = 60f): Boolean {
            return screenPos.x >= -margin && screenPos.x <= canvasWidth + margin &&
                   screenPos.y >= -margin && screenPos.y <= canvasHeight + margin
        }

        // 1. Draw Deep Ocean Water Abyss Background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(AbyssDark, AbyssNavy, AbyssDark)
            )
        )

        // 2. Draw Subtle Deep-Sea Spatial Grid
        val gridSize = 120f * zoom
        val startX = (canvasCenter.x - (cameraPos.x % 120f) * zoom) % gridSize
        val startY = (canvasCenter.y - (cameraPos.y % 120f) * zoom) % gridSize

        var curX = startX - gridSize
        while (curX < canvasWidth + gridSize) {
            drawLine(
                color = NeonCyan.copy(alpha = 0.05f),
                start = Offset(curX, 0f),
                end = Offset(curX, canvasHeight),
                strokeWidth = 1f
            )
            curX += gridSize
        }

        var curY = startY - gridSize
        while (curY < canvasHeight + gridSize) {
            drawLine(
                color = NeonCyan.copy(alpha = 0.05f),
                start = Offset(0f, curY),
                end = Offset(canvasWidth, curY),
                strokeWidth = 1f
            )
            curY += gridSize
        }

        // 3. Draw Ambient Water Plankton & Light Spores
        for (particle in engine.waterParticles) {
            val sPos = worldToScreen(particle.position)
            if (isVisible(sPos, 20f)) {
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha),
                    radius = particle.radius * zoom,
                    center = sPos
                )
            }
        }

        // 4. Draw Bubble Trails
        for (bubble in engine.bubbleTrails) {
            val sPos = worldToScreen(bubble.position)
            if (isVisible(sPos, 30f)) {
                drawCircle(
                    color = bubble.color.copy(alpha = bubble.alpha * 0.4f),
                    radius = bubble.radius * zoom * 1.5f,
                    center = sPos
                )
                drawCircle(
                    color = Color.White.copy(alpha = bubble.alpha * 0.7f),
                    radius = bubble.radius * zoom * 0.5f,
                    center = sPos
                )
            }
        }

        // 4b. Draw Bioluminescent Neon Sparks (Movement wakes & Prey consumption bursts)
        for (spark in engine.neonSparks) {
            val sPos = worldToScreen(spark.position)
            if (isVisible(sPos, 25f)) {
                val sparkR = spark.radius * zoom
                // Outer soft glow
                drawCircle(
                    color = spark.glowColor.copy(alpha = spark.alpha * 0.5f),
                    radius = sparkR * 2.2f,
                    center = sPos
                )
                // Inner intense color core
                drawCircle(
                    color = spark.color.copy(alpha = spark.alpha),
                    radius = sparkR,
                    center = sPos
                )
                // Hotspot white center
                drawCircle(
                    color = Color.White.copy(alpha = spark.alpha * 0.85f),
                    radius = sparkR * 0.4f,
                    center = sPos
                )
            }
        }

        // 5. Draw Circular Arena Boundary (Electrified Neon Barrier)
        val screenWorldCenter = worldToScreen(engine.worldCenter)
        val screenWorldRadius = engine.worldRadius * zoom

        // Outer glow barrier
        drawCircle(
            color = NeonMagenta.copy(alpha = 0.2f),
            radius = screenWorldRadius,
            center = screenWorldCenter,
            style = Stroke(width = 16f * zoom)
        )
        // Bright barrier line
        drawCircle(
            color = NeonMagenta.copy(alpha = 0.85f),
            radius = screenWorldRadius,
            center = screenWorldCenter,
            style = Stroke(width = 4f * zoom)
        )

        // 6. Draw Bioluminescent Food Pellets
        for (food in engine.foods) {
            val sPos = worldToScreen(food.position)
            if (isVisible(sPos, 40f)) {
                val pulse = (sin(food.glowPhase) + 1f) * 0.5f
                val glowR = (food.radius * (1.8f + pulse * 0.6f)) * zoom
                val coreR = food.radius * zoom

                // Outer Soft Glow Halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            food.glowColor.copy(alpha = 0.55f),
                            food.glowColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = sPos,
                        radius = glowR
                    ),
                    radius = glowR,
                    center = sPos
                )

                // Solid Core
                drawCircle(
                    color = food.color,
                    radius = coreR,
                    center = sPos
                )

                // Bright center highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = coreR * 0.35f,
                    center = sPos
                )
            }
        }

        // 6b. Draw Bionic Plankton Power-ups (Shield, Speed Boost)
        for (bp in engine.bionicPlanktons) {
            val sPos = worldToScreen(bp.position)
            if (isVisible(sPos, 60f)) {
                val pulse = (sin(bp.pulsePhase) + 1f) * 0.5f
                val baseR = bp.radius * zoom
                val glowR = baseR * (1.8f + pulse * 0.5f)

                // Outer soft glowing aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            bp.type.glowColor.copy(alpha = 0.7f),
                            bp.type.glowColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = sPos,
                        radius = glowR
                    ),
                    radius = glowR,
                    center = sPos
                )

                // Rotating Orbital Rings / Tech Brackets
                val rot = bp.rotationAngle
                val bracketRadius = baseR * 1.35f
                for (a in 0 until 4) {
                    val angleOffset = rot + (a * (kotlin.math.PI.toFloat() / 2f))
                    val deg = Math.toDegrees(angleOffset.toDouble()).toFloat()
                    drawArc(
                        color = bp.type.color,
                        startAngle = deg,
                        sweepAngle = 45f,
                        useCenter = false,
                        topLeft = Offset(sPos.x - bracketRadius, sPos.y - bracketRadius),
                        size = Size(bracketRadius * 2f, bracketRadius * 2f),
                        style = Stroke(width = 2.5f * zoom)
                    )
                }

                // Inner glowing power-up core
                drawCircle(
                    color = bp.type.color,
                    radius = baseR,
                    center = sPos
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = baseR * 0.55f,
                    center = sPos
                )

                // Symbol text (Shield 🛡 or Bolt ⚡)
                val symbol = if (bp.type == BionicPlanktonType.SHIELD) "🛡" else "⚡"
                iconPaint.textSize = (18f * zoom).coerceAtLeast(14f)
                drawContext.canvas.nativeCanvas.drawText(
                    symbol,
                    sPos.x,
                    sPos.y + (iconPaint.textSize * 0.35f),
                    iconPaint
                )
            }
        }

        // 7. Draw Serpents (Bots first, then Player on top)
        for (bot in engine.bots) {
            if (bot.isAlive) {
                drawSerpent(bot, ::worldToScreen, ::isVisible, zoom, namePaint)
            }
        }

        if (engine.player.isAlive) {
            drawSerpent(engine.player, ::worldToScreen, ::isVisible, zoom, namePaint)
        }

        // 8. Draw Floating Notices (+150 Mass, Kill streaks)
        for (notice in engine.floatingNotices) {
            val sPos = worldToScreen(notice.position)
            if (isVisible(sPos, 50f)) {
                textPaint.color = notice.color.toArgb()
                textPaint.alpha = (notice.alpha * 255).toInt().coerceIn(0, 255)
                drawContext.canvas.nativeCanvas.drawText(
                    notice.text,
                    sPos.x,
                    sPos.y,
                    textPaint
                )
            }
        }
    }
}

private fun DrawScope.drawSerpent(
    serpent: Serpent,
    worldToScreen: (Offset) -> Offset,
    isVisible: (Offset, Float) -> Boolean,
    zoom: Float,
    namePaint: Paint
) {
    val skin = serpent.skin
    val segRadius = serpent.bodyRadius * zoom
    val segments = serpent.segments
    if (segments.isEmpty()) return

    // Draw from tail to head
    for (i in segments.indices.reversed()) {
        val segPos = segments[i]
        val sPos = worldToScreen(segPos)

        if (!isVisible(sPos, segRadius * 2.5f)) continue

        val progress = i.toFloat() / segments.size.coerceAtLeast(1)
        // Subtle taper towards the tail
        val taperFactor = (1.0f - (progress * 0.45f)).coerceIn(0.55f, 1.0f)
        val r = segRadius * taperFactor

        // Segment glow
        drawCircle(
            color = skin.glowColor.copy(alpha = 0.3f),
            radius = r * 1.55f,
            center = sPos
        )

        // Main segment body
        val segmentColor = if (i % 2 == 0) skin.primaryColor else skin.secondaryColor
        drawCircle(
            color = segmentColor,
            radius = r,
            center = sPos
        )

        // Inner glowing core ring
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = r * 0.4f,
            center = sPos
        )
    }

    // Draw Head
    val headScreenPos = worldToScreen(serpent.head)
    if (isVisible(headScreenPos, segRadius * 3f)) {
        val headRadius = (serpent.bodyRadius * 1.15f) * zoom

        // Head Outer Glow
        drawCircle(
            color = skin.glowColor.copy(alpha = 0.65f),
            radius = headRadius * 1.6f,
            center = headScreenPos
        )

        // Head Main
        drawCircle(
            color = skin.primaryColor,
            radius = headRadius,
            center = headScreenPos
        )

        // Head Dorsal Crown / Highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = headRadius * 0.4f,
            center = headScreenPos
        )

        // Draw Bioluminescent Eyes
        val angle = serpent.angle
        val eyeDist = headRadius * 0.6f
        val eyeAngleOffset = 0.7f
        val eyeRadius = headRadius * 0.28f

        val leftEyePos = Offset(
            headScreenPos.x + cos(angle - eyeAngleOffset) * eyeDist,
            headScreenPos.y + sin(angle - eyeAngleOffset) * eyeDist
        )
        val rightEyePos = Offset(
            headScreenPos.x + cos(angle + eyeAngleOffset) * eyeDist,
            headScreenPos.y + sin(angle + eyeAngleOffset) * eyeDist
        )

        // Left Eye Glow & Pupil
        drawCircle(color = skin.eyeColor, radius = eyeRadius, center = leftEyePos)
        drawCircle(
            color = Color.Black,
            radius = eyeRadius * 0.45f,
            center = Offset(leftEyePos.x + cos(angle) * (eyeRadius * 0.35f), leftEyePos.y + sin(angle) * (eyeRadius * 0.35f))
        )

        // Right Eye Glow & Pupil
        drawCircle(color = skin.eyeColor, radius = eyeRadius, center = rightEyePos)
        drawCircle(
            color = Color.Black,
            radius = eyeRadius * 0.45f,
            center = Offset(rightEyePos.x + cos(angle) * (eyeRadius * 0.35f), rightEyePos.y + sin(angle) * (eyeRadius * 0.35f))
        )

        // Active Power-up Visual Auras around head
        if (serpent.hasShield) {
            val shieldRadius = headRadius * 2.3f
            // Translucent glowing shield bubble
            drawCircle(
                color = Color(0x3300E5FF),
                radius = shieldRadius,
                center = headScreenPos
            )
            // Vibrant kinetic perimeter
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = shieldRadius,
                center = headScreenPos,
                style = Stroke(width = 3.5f * zoom)
            )
            // Outer pulse wave
            val pulseWave = (sin(serpent.shieldDuration * 6f) + 1f) * 0.5f
            drawCircle(
                color = Color(0x6600E5FF),
                radius = shieldRadius + (pulseWave * 8f * zoom),
                center = headScreenPos,
                style = Stroke(width = 1.5f * zoom)
            )
        }

        if (serpent.hasSpeedBoost) {
            val speedAuraRadius = headRadius * 1.8f
            // Fiery electric amber aura
            drawCircle(
                color = Color(0x44FFB800),
                radius = speedAuraRadius,
                center = headScreenPos
            )
            drawCircle(
                color = Color(0xFFFFB800),
                radius = speedAuraRadius,
                center = headScreenPos,
                style = Stroke(width = 2.5f * zoom)
            )
        }

        // Serpent Name Label above head
        val nameOffset = Offset(headScreenPos.x, headScreenPos.y - (headRadius * (if (serpent.hasShield) 2.6f else 1.7f)))
        drawContext.canvas.nativeCanvas.drawText(
            serpent.name,
            nameOffset.x,
            nameOffset.y,
            namePaint
        )
    }
}
