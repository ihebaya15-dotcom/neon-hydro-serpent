package com.example.game

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GameMath {
    const val TWO_PI = (2 * PI).toFloat()

    fun distance(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    fun distanceSquared(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }

    fun angleTo(from: Offset, to: Offset): Float {
        return atan2(to.y - from.y, to.x - from.x)
    }

    fun normalizeAngle(angle: Float): Float {
        var a = angle % TWO_PI
        if (a < -PI.toFloat()) a += TWO_PI
        if (a > PI.toFloat()) a -= TWO_PI
        return a
    }

    fun rotateTowards(current: Float, target: Float, maxDelta: Float): Float {
        var diff = normalizeAngle(target - current)
        if (diff > maxDelta) diff = maxDelta
        if (diff < -maxDelta) diff = -maxDelta
        return normalizeAngle(current + diff)
    }

    fun movePoint(point: Offset, angle: Float, distance: Float): Offset {
        return Offset(
            point.x + cos(angle) * distance,
            point.y + sin(angle) * distance
        )
    }

    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t
    }

    fun lerpOffset(a: Offset, b: Offset, t: Float): Offset {
        return Offset(
            lerp(a.x, b.x, t),
            lerp(a.y, b.y, t)
        )
    }
}
