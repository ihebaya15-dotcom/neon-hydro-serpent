package com.example

import androidx.compose.ui.geometry.Offset
import com.example.game.GameEngine
import com.example.game.GameMath
import com.example.game.SkinTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test_game_math_calculations() {
        val p1 = Offset(0f, 0f)
        val p2 = Offset(3f, 4f)
        assertEquals(5f, GameMath.distance(p1, p2), 0.001f)
        assertEquals(25f, GameMath.distanceSquared(p1, p2), 0.001f)
    }

    @Test
    fun test_game_engine_init() {
        val engine = GameEngine(worldRadius = 1200f)
        assertNotNull(engine.player)
        assertTrue(engine.player.isAlive)
        assertEquals(12, engine.bots.size)
        assertTrue(engine.foods.size >= 400)
    }

    @Test
    fun test_serpent_movement_and_food_consumption() {
        val engine = GameEngine(worldRadius = 1200f)
        val initialMass = engine.player.mass
        engine.setPlayerBoosting(false)
        engine.update(0.016f)
        assertTrue(engine.player.isAlive)
        assertTrue(engine.player.segments.isNotEmpty())
    }

    @Test
    fun test_neon_sparks_particle_system() {
        val engine = GameEngine(worldRadius = 1200f)
        engine.setPlayerBoosting(true)
        for (i in 0 until 10) {
            engine.update(0.016f)
        }
        assertTrue("Sparks should be generated during movement/boost", engine.neonSparks.isNotEmpty())
    }

    @Test
    fun test_bionic_plankton_powerups() {
        val engine = GameEngine(worldRadius = 1200f)
        assertTrue("Bionic planktons should spawn", engine.bionicPlanktons.isNotEmpty())

        // Test shield mechanics
        engine.player.shieldDuration = 10f
        assertTrue(engine.player.hasShield)

        // Test speed boost mechanics
        engine.player.speedBoostDuration = 8f
        assertTrue(engine.player.hasSpeedBoost)

        engine.update(0.1f)
        assertTrue(engine.player.shieldDuration < 10f)
        assertTrue(engine.player.speedBoostDuration < 8f)
        assertEquals(10.5f, engine.player.speed, 0.01f)
    }
}
