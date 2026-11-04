package com.example.game

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

data class Serpent(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    var skin: SkinTheme,
    var head: Offset,
    var angle: Float,
    var targetAngle: Float,
    var speed: Float = 4.8f,
    var mass: Float = 50f,
    var isBoosting: Boolean = false,
    var isAlive: Boolean = true,
    var kills: Int = 0,
    var score: Int = 0
) {
    // Array of body segment positions from head to tail
    val segments = ArrayList<Offset>()

    var wigglePhase: Float = 0f
    var boostShedTimer: Float = 0f

    // Bot AI state (only used if isPlayer == false)
    var aiStateTimer: Float = 0f
    var aiTargetFood: FoodPellet? = null
    var aiPersonality: AiPersonality = AiPersonality.BALANCED

    val segmentSpacing: Float
        get() = (14f + (mass / 300f).coerceAtMost(8f))

    val bodyRadius: Float
        get() = (10f + (mass / 250f).coerceAtMost(16f))

    val targetSegmentCount: Int
        get() = (12 + (mass / 8f).toInt()).coerceIn(12, 180)

    init {
        // Initialize segments behind head
        segments.add(head)
        val initialSegments = (12 + (mass / 8f).toInt()).coerceIn(12, 30)
        for (i in 1 until initialSegments) {
            val offset = Offset(
                head.x - cos(angle) * (i * segmentSpacing),
                head.y - sin(angle) * (i * segmentSpacing)
            )
            segments.add(offset)
        }
    }

    fun updateMovement(deltaTime: Float, worldRadius: Float): FoodPellet? {
        if (!isAlive) return null

        val currentSpeed = if (isBoosting && mass > 20f) 8.8f else 4.8f
        speed = currentSpeed

        // Turn towards targetAngle with smooth angular speed
        val turnRate = if (isBoosting) 4.2f * deltaTime else 3.4f * deltaTime
        angle = GameMath.rotateTowards(angle, targetAngle, turnRate)

        // Move head forward
        head = Offset(
            head.x + cos(angle) * speed,
            head.y + sin(angle) * speed
        )

        // Prevent escaping outside circular world boundary with gentle deflection
        val distFromCenter = GameMath.distance(head, Offset(worldRadius, worldRadius))
        if (distFromCenter > worldRadius - 30f) {
            val angleToCenter = GameMath.angleTo(head, Offset(worldRadius, worldRadius))
            targetAngle = angleToCenter
        }

        // Update body segments with Inverse Kinematics constraint
        if (segments.isNotEmpty()) {
            segments[0] = head
            val spacing = segmentSpacing
            for (i in 1 until segments.size) {
                val prev = segments[i - 1]
                val curr = segments[i]
                val dist = GameMath.distance(prev, curr)
                if (dist > 0.001f) {
                    val factor = spacing / dist
                    segments[i] = Offset(
                        prev.x - (prev.x - curr.x) * factor,
                        prev.y - (prev.y - curr.y) * factor
                    )
                }
            }
        }

        // Adjust segment count dynamically based on current mass
        val desiredCount = targetSegmentCount
        while (segments.size < desiredCount) {
            val tail = segments.lastOrNull() ?: head
            segments.add(tail)
        }
        while (segments.size > desiredCount && segments.size > 12) {
            segments.removeAt(segments.lastIndex)
        }

        // Update swimming wiggle
        wigglePhase += (speed * 0.08f)

        // Handle boosting cost: shed mass as luminous food droplets
        var shedFood: FoodPellet? = null
        if (isBoosting && mass > 22f) {
            mass -= 2.0f * deltaTime
            boostShedTimer += deltaTime
            if (boostShedTimer >= 0.12f) {
                boostShedTimer = 0f
                val tail = segments.lastOrNull() ?: head
                shedFood = FoodPellet(
                    id = System.nanoTime(),
                    position = tail,
                    radius = 5.5f,
                    color = skin.primaryColor,
                    glowColor = skin.glowColor,
                    energyValue = 1.8f,
                    isDeathOrb = false
                )
            }
        }

        return shedFood
    }
}

enum class AiPersonality {
    AGGRESSIVE, // Seeks to encircle player or other snakes
    SCAVENGER,  // Actively searches for large food clusters or death orbs
    BALANCED,   // Wanders, gathers food, flees when larger heads approach
    TIMID       // Stays away from other serpents, consumes plankton
}
