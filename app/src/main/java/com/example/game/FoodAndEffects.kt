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
