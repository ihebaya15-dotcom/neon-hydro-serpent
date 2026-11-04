package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class FoodPellet(
    val id: Long,
    var position: Offset,
    val radius: Float,
    val color: Color,
    val glowColor: Color,
    val energyValue: Float,
    var glowPhase: Float = Random.nextFloat() * 6.28f,
    val isDeathOrb: Boolean = false
)

data class WaterParticle(
    var position: Offset,
    val radius: Float,
    val alpha: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color
)

data class BubbleTrail(
    var position: Offset,
    var radius: Float,
    var alpha: Float,
    val color: Color,
    val maxRadius: Float
)

data class FloatingNotice(
    val id: Long,
    val text: String,
    var position: Offset,
    val color: Color,
    var alpha: Float = 1.0f,
    var lifetime: Float = 1.5f
)

data class NeonSpark(
    var position: Offset,
    var velocity: Offset,
    var radius: Float,
    val color: Color,
    val glowColor: Color,
    var alpha: Float = 1.0f,
    val maxLifetime: Float = 0.8f,
    var age: Float = 0f
)

enum class BionicPlanktonType(
    val titleAr: String,
    val titleEn: String,
    val color: Color,
    val glowColor: Color,
    val baseDuration: Float
) {
    SHIELD(
        titleAr = "درع النيون الحركي",
        titleEn = "Kinetic Shield",
        color = Color(0xFF00E5FF),
        glowColor = Color(0x9900E5FF),
        baseDuration = 10f
    ),
    SPEED_BOOST(
        titleAr = "سرعة تيربو خارقة",
        titleEn = "Hyper Velocity",
        color = Color(0xFFFFB800),
        glowColor = Color(0x99FFB800),
        baseDuration = 8f
    )
}

data class BionicPlankton(
    val id: Long,
    var position: Offset,
    val type: BionicPlanktonType,
    val radius: Float = 16f,
    var pulsePhase: Float = Random.nextFloat() * 6.28f,
    var rotationAngle: Float = Random.nextFloat() * 6.28f
)
