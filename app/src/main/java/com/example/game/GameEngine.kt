package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class GameEngine(
    val worldRadius: Float = 2600f,
    var onPlayerKilled: (Serpent) -> Unit = {},
    var onPlayerKillStreak: (String) -> Unit = {},
    var onEatOrb: () -> Unit = {}
) {
    val worldCenter = Offset(worldRadius, worldRadius)

    var player: Serpent = createPlayerSerpent(SkinTheme.CYAN_STORM)
        private set

    val bots = ArrayList<Serpent>()
    val foods = ArrayList<FoodPellet>()
    val waterParticles = ArrayList<WaterParticle>()
    val bubbleTrails = ArrayList<BubbleTrail>()
    val floatingNotices = ArrayList<FloatingNotice>()
    val neonSparks = ArrayList<NeonSpark>()
    val bionicPlanktons = ArrayList<BionicPlankton>()

    var isGameOver = false
    var elapsedTime = 0f

    private var nextFoodId = 1L
    private var nextNoticeId = 1L
    private var nextPlanktonId = 1L

    private val botNames = listOf(
        "أفعى المرجان", "شبح الأعماق", "الناشر الأزرق", "تنين البحار",
        "القرش اللامع", "تريتون", "البرق المائي", "لوياثان",
        "قنديل الرعب", "سيد الهاوية", "الوميض الخاطف", "أوروكا النيون"
    )

    private val foodGlowColors = listOf(
        Color(0xFF00F0FF), Color(0xFFFF007A), Color(0xFF00FFA3),
        Color(0xFFFFB800), Color(0xFF9D00FF), Color(0xFFFF4D4D)
    )

    init {
        resetGame(SkinTheme.CYAN_STORM)
    }

    fun resetGame(playerSkin: SkinTheme) {
        isGameOver = false
        elapsedTime = 0f
        foods.clear()
        bots.clear()
        waterParticles.clear()
        bubbleTrails.clear()
        floatingNotices.clear()
        neonSparks.clear()
        bionicPlanktons.clear()

        player = createPlayerSerpent(playerSkin)

        // Spawn bionic planktons (power-ups)
        for (i in 0 until 12) {
            spawnBionicPlankton()
        }

        // Spawn initial ambient foods
        for (i in 0 until 420) {
            spawnRandomFood()
        }

        // Spawn ambient water particles
        for (i in 0 until 90) {
            waterParticles.add(
                WaterParticle(
                    position = randomPointInWorld(),
                    radius = Random.nextFloat() * 3f + 1.2f,
                    alpha = Random.nextFloat() * 0.45f + 0.15f,
                    speedX = (Random.nextFloat() - 0.5f) * 0.6f,
                    speedY = (Random.nextFloat() - 0.5f) * 0.6f,
                    color = foodGlowColors.random().copy(alpha = 0.5f)
                )
            )
        }

        // Spawn initial bots
        val availableSkins = SkinTheme.ALL_SKINS
        for (i in 0 until 12) {
            val botSkin = availableSkins[i % availableSkins.size]
            val botName = botNames[i % botNames.size]
            bots.add(createBotSerpent(botName, botSkin))
        }
    }

    private fun createPlayerSerpent(skin: SkinTheme): Serpent {
        return Serpent(
            id = "player",
            name = "أنت (الثعبان المائي)",
            isPlayer = true,
            skin = skin,
            head = worldCenter,
            angle = 0f,
            targetAngle = 0f,
            mass = 55f
        )
    }

    private fun createBotSerpent(name: String, skin: SkinTheme): Serpent {
        var spawnPos = randomPointInWorld()
        var attempts = 0
        while (attempts < 8 && GameMath.distance(spawnPos, player.head) < 220f) {
            spawnPos = randomPointInWorld()
            attempts++
        }
        val randomAngle = Random.nextFloat() * GameMath.TWO_PI
        val bot = Serpent(
            id = "bot_${System.nanoTime()}_${Random.nextInt(1000)}",
            name = name,
            isPlayer = false,
            skin = skin,
            head = spawnPos,
            angle = randomAngle,
            targetAngle = randomAngle,
            mass = Random.nextFloat() * 80f + 35f
        )
        bot.aiPersonality = AiPersonality.values().random()
        return bot
    }

    private fun randomPointInWorld(): Offset {
        val angle = Random.nextFloat() * GameMath.TWO_PI
        val r = (worldRadius - 120f) * kotlin.math.sqrt(Random.nextFloat())
        return Offset(
            worldCenter.x + cos(angle) * r,
            worldCenter.y + sin(angle) * r
        )
    }

    private fun spawnRandomFood() {
        val pos = randomPointInWorld()
        val color = foodGlowColors.random()
        foods.add(
            FoodPellet(
                id = nextFoodId++,
                position = pos,
                radius = Random.nextFloat() * 3.5f + 3.5f,
                color = color,
                glowColor = color.copy(alpha = 0.65f),
                energyValue = Random.nextFloat() * 2.5f + 1.2f,
                isDeathOrb = false
            )
        )
    }

    private fun spawnBionicPlankton() {
        val pos = randomPointInWorld()
        val type = if (Random.nextBoolean()) BionicPlanktonType.SHIELD else BionicPlanktonType.SPEED_BOOST
        bionicPlanktons.add(
            BionicPlankton(
                id = nextPlanktonId++,
                position = pos,
                type = type,
                radius = 16f
            )
        )
    }

    fun setPlayerDirection(joystickX: Float, joystickY: Float) {
        if (!player.isAlive) return
        if (joystickX != 0f || joystickY != 0f) {
            player.targetAngle = kotlin.math.atan2(joystickY, joystickX)
        }
    }

    fun setPlayerBoosting(boosting: Boolean) {
        if (!player.isAlive) return
        player.isBoosting = boosting && player.mass > 22f
    }

    fun update(deltaTime: Float) {
        if (isGameOver) return
        elapsedTime += deltaTime

        // 1. Update Player
        if (player.isAlive) {
            val shedFood = player.updateMovement(deltaTime, worldRadius)
            if (shedFood != null) {
                foods.add(shedFood)
                addBubbleTrail(shedFood.position, player.skin.glowColor)
            }
            emitMovementSparks(player, deltaTime)
        }

        // 2. Update Bots AI & Movement
        for (bot in bots) {
            if (bot.isAlive) {
                updateBotAi(bot, deltaTime)
                val shedFood = bot.updateMovement(deltaTime, worldRadius)
                if (shedFood != null) {
                    foods.add(shedFood)
                    if (bot.isBoosting) {
                        addBubbleTrail(shedFood.position, bot.skin.glowColor)
                    }
                }
                emitMovementSparks(bot, deltaTime)
            }
        }

        // 3. Update Ambient Water Particles
        for (particle in waterParticles) {
            particle.position = Offset(
                particle.position.x + particle.speedX,
                particle.position.y + particle.speedY
            )
            // Wrap particles around world boundary
            val dist = GameMath.distance(particle.position, worldCenter)
            if (dist > worldRadius) {
                particle.position = worldCenter
            }
        }

        // 4. Update Bubble Trails
        val bubbleIterator = bubbleTrails.iterator()
        while (bubbleIterator.hasNext()) {
            val bubble = bubbleIterator.next()
            bubble.radius += deltaTime * 8f
            bubble.alpha -= deltaTime * 1.5f
            if (bubble.alpha <= 0f) {
                bubbleIterator.remove()
            }
        }

        // 5. Update Neon Sparks Particles
        val sparkIterator = neonSparks.iterator()
        while (sparkIterator.hasNext()) {
            val spark = sparkIterator.next()
            spark.age += deltaTime
            if (spark.age >= spark.maxLifetime) {
                sparkIterator.remove()
                continue
            }
            val progress = (spark.age / spark.maxLifetime).coerceIn(0f, 1f)
            spark.alpha = (1.0f - progress).coerceIn(0f, 1f)
            spark.position = Offset(
                spark.position.x + spark.velocity.x * deltaTime,
                spark.position.y + spark.velocity.y * deltaTime
            )
            spark.velocity = Offset(
                spark.velocity.x * 0.94f,
                spark.velocity.y * 0.94f
            )
        }

        // 6. Update Floating Notices
        val noticeIterator = floatingNotices.iterator()
        while (noticeIterator.hasNext()) {
            val notice = noticeIterator.next()
            notice.position = Offset(notice.position.x, notice.position.y - (deltaTime * 35f))
            notice.lifetime -= deltaTime
            notice.alpha = (notice.lifetime / 1.5f).coerceIn(0f, 1f)
            if (notice.lifetime <= 0f) {
                noticeIterator.remove()
            }
        }

        // 6. Food Collisions (Head eating food)
        checkFoodCollisions()

        // 7. Bionic Plankton Power-ups (Shield, Speed Boost)
        for (bp in bionicPlanktons) {
            bp.pulsePhase += deltaTime * 3.5f
            bp.rotationAngle += deltaTime * 1.8f
        }
        checkBionicPlanktonCollisions()
        while (bionicPlanktons.size < 12) {
            spawnBionicPlankton()
        }

        // 8. Serpent-to-Serpent Collisions (Combat / Predation)
        checkSerpentCollisions()

        // 9. Maintain Food Population
        while (foods.size < 400) {
            spawnRandomFood()
        }

        // 10. Respawn dead bots away from player
        for (i in bots.indices) {
            val bot = bots[i]
            if (!bot.isAlive) {
                bot.aiStateTimer += deltaTime
                if (bot.aiStateTimer > 2.5f) {
                    val newSkin = SkinTheme.ALL_SKINS.random()
                    val newName = botNames.random()
                    bots[i] = createBotSerpent(newName, newSkin)
                }
            }
        }
    }

    private fun addBubbleTrail(pos: Offset, color: Color) {
        if (bubbleTrails.size < 120) {
            bubbleTrails.add(
                BubbleTrail(
                    position = pos,
                    radius = 3.5f,
                    alpha = 0.85f,
                    color = color,
                    maxRadius = 12f
                )
            )
        }
    }

    private fun addNotice(text: String, pos: Offset, color: Color) {
        if (floatingNotices.size < 15) {
            floatingNotices.add(
                FloatingNotice(
                    id = nextNoticeId++,
                    text = text,
                    position = pos,
                    color = color
                )
            )
        }
    }

    private fun checkFoodCollisions() {
        val allSerpents = mutableListOf<Serpent>()
        if (player.isAlive) allSerpents.add(player)
        for (b in bots) if (b.isAlive) allSerpents.add(b)

        val foodIterator = foods.iterator()
        while (foodIterator.hasNext()) {
            val food = foodIterator.next()
            for (serpent in allSerpents) {
                val eatRadius = serpent.bodyRadius * 1.4f + food.radius
                if (GameMath.distanceSquared(serpent.head, food.position) <= eatRadius * eatRadius) {
                    serpent.mass += food.energyValue
                    serpent.score += (food.energyValue * 10).toInt()
                    foodIterator.remove()

                    if (serpent.isPlayer) {
                        onEatOrb()
                        if (food.isDeathOrb && food.energyValue >= 5f) {
                            addNotice("+${(food.energyValue * 10).toInt()} طاقة نيون!", food.position, food.color)
                        }
                    }
                    emitPreySparks(food.position, food.color, food.glowColor, if (food.isDeathOrb) 14 else 6)
                    break
                }
            }
        }
    }

    private fun checkBionicPlanktonCollisions() {
        val allSerpents = mutableListOf<Serpent>()
        if (player.isAlive) allSerpents.add(player)
        for (b in bots) if (b.isAlive) allSerpents.add(b)

        val iterator = bionicPlanktons.iterator()
        while (iterator.hasNext()) {
            val bp = iterator.next()
            for (serpent in allSerpents) {
                val pickupRadius = serpent.bodyRadius * 1.5f + bp.radius
                if (GameMath.distanceSquared(serpent.head, bp.position) <= pickupRadius * pickupRadius) {
                    iterator.remove()
                    when (bp.type) {
                        BionicPlanktonType.SHIELD -> {
                            serpent.shieldDuration = bp.type.baseDuration
                            if (serpent.isPlayer) {
                                addNotice("درع النيون الحركي! 🛡️ (10ث)", bp.position, bp.type.color)
                                onPlayerKillStreak("درع الحماية نشط! 🛡️")
                            }
                        }
                        BionicPlanktonType.SPEED_BOOST -> {
                            serpent.speedBoostDuration = bp.type.baseDuration
                            if (serpent.isPlayer) {
                                addNotice("سرعة تيربو خارقة! ⚡ (8ث)", bp.position, bp.type.color)
                                onPlayerKillStreak("سرعة خارقة نشطة! ⚡")
                            }
                        }
                    }
                    emitPreySparks(bp.position, bp.type.color, bp.type.glowColor, count = 22)
                    serpent.score += 50
                    break
                }
            }
        }
    }

    private fun checkSerpentCollisions() {
        val allSerpents = mutableListOf<Serpent>()
        if (player.isAlive) allSerpents.add(player)
        for (b in bots) if (b.isAlive) allSerpents.add(b)

        for (attacker in allSerpents) {
            if (!attacker.isAlive) continue

            // World edge boundary collision
            val distToCenter = GameMath.distance(attacker.head, worldCenter)
            if (distToCenter >= worldRadius - 10f) {
                if (attacker.hasShield) {
                    attacker.shieldDuration = 0f
                    emitPreySparks(attacker.head, Color(0xFF00E5FF), Color.White, count = 22)
                    if (attacker.isPlayer) {
                        addNotice("الدرع امتص صدمة الحافة! 🛡️", attacker.head, Color(0xFF00E5FF))
                    }
                    attacker.targetAngle = GameMath.angleTo(attacker.head, worldCenter)
                } else {
                    killSerpent(attacker, null)
                }
                continue
            }

            // Head vs Other Serpent Bodies
            for (defender in allSerpents) {
                if (attacker.id == defender.id || !defender.isAlive) continue

                // Check collision between attacker's head and defender's body segments
                val collisionRadius = attacker.bodyRadius * 0.9f + defender.bodyRadius * 0.9f
                val collisionRadiusSq = collisionRadius * collisionRadius

                // Skip head-adjacent segments to avoid unfair grazing
                val startIndex = 2
                var collided = false
                for (sIdx in startIndex until defender.segments.size) {
                    val segmentPos = defender.segments[sIdx]
                    if (GameMath.distanceSquared(attacker.head, segmentPos) < collisionRadiusSq) {
                        collided = true
                        break
                    }
                }

                if (collided) {
                    if (attacker.hasShield) {
                        // Shield absorbs impact and deflects
                        attacker.shieldDuration = 0f
                        emitPreySparks(attacker.head, Color(0xFF00E5FF), Color.White, count = 28)
                        if (attacker.isPlayer) {
                            addNotice("الدرع صد الضربة القاتلة! 🛡️💥", attacker.head, Color(0xFF00E5FF))
                            onPlayerKillStreak("درع الحماية أنقذك من الاصطدام!")
                        }
                        // Deflect backwards
                        attacker.head = Offset(
                            attacker.head.x - cos(attacker.angle) * 35f,
                            attacker.head.y - sin(attacker.angle) * 35f
                        )
                        attacker.targetAngle = GameMath.normalizeAngle(attacker.angle + kotlin.math.PI.toFloat())
                    } else {
                        killSerpent(attacker, defender)
                    }
                    break
                }
            }
        }
    }

    private fun killSerpent(victim: Serpent, killer: Serpent?) {
        victim.isAlive = false

        // Explode body segments into rich bioluminescent death orbs
        val dropColor = victim.skin.primaryColor
        val glowColor = victim.skin.glowColor

        for (i in 0 until victim.segments.size step 2) {
            val seg = victim.segments[i]
            // Scatter slightly
            val scatterOffset = Offset(
                seg.x + (Random.nextFloat() - 0.5f) * 25f,
                seg.y + (Random.nextFloat() - 0.5f) * 25f
            )
            foods.add(
                FoodPellet(
                    id = nextFoodId++,
                    position = scatterOffset,
                    radius = 6.5f + Random.nextFloat() * 4.5f,
                    color = dropColor,
                    glowColor = glowColor,
                    energyValue = 7.5f + Random.nextFloat() * 6.0f,
                    isDeathOrb = true
                )
            )
        }

        // Emit dramatic bioluminescent kill burst sparks across the serpent's body
        emitKillBurst(victim)

        if (killer != null) {
            killer.kills++
            killer.score += (victim.mass * 12).toInt()
            if (killer.isPlayer) {
                val killTitles = listOf("افتراس مائي رائع! ⚔️", "صياد الأعماق! 🌊", "قضاء نيون مميت! ⚡")
                val title = killTitles.random()
                addNotice(title, victim.head, victim.skin.primaryColor)
                onPlayerKillStreak(title)
            }
        }

        if (victim.isPlayer) {
            isGameOver = true
            onPlayerKilled(victim)
        }
    }

    private fun emitMovementSparks(serpent: Serpent, deltaTime: Float) {
        if (neonSparks.size >= 280) return
        val emissionChance = if (serpent.isBoosting) 0.70f else 0.25f
        if (Random.nextFloat() < emissionChance) {
            val segments = serpent.segments
            if (segments.isNotEmpty()) {
                val segIdx = (segments.size - 1 - Random.nextInt(minOf(5, segments.size))).coerceAtLeast(0)
                val segPos = segments[segIdx]
                val backwardAngle = serpent.angle + kotlin.math.PI.toFloat() + (Random.nextFloat() - 0.5f) * 1.4f
                val sparkSpeed = if (serpent.isBoosting) Random.nextFloat() * 55f + 30f else Random.nextFloat() * 25f + 12f
                neonSparks.add(
                    NeonSpark(
                        position = Offset(
                            segPos.x + (Random.nextFloat() - 0.5f) * (serpent.bodyRadius * 0.9f),
                            segPos.y + (Random.nextFloat() - 0.5f) * (serpent.bodyRadius * 0.9f)
                        ),
                        velocity = Offset(cos(backwardAngle) * sparkSpeed, sin(backwardAngle) * sparkSpeed),
                        radius = Random.nextFloat() * 3.0f + 1.8f,
                        color = if (Random.nextBoolean()) serpent.skin.primaryColor else serpent.skin.secondaryColor,
                        glowColor = serpent.skin.glowColor,
                        maxLifetime = if (serpent.isBoosting) 0.85f else 0.65f
                    )
                )
            }
        }
    }

    private fun emitPreySparks(pos: Offset, color: Color, glowColor: Color, count: Int = 8) {
        val availableSlots = (290 - neonSparks.size).coerceAtLeast(0)
        val spawnCount = minOf(count, availableSlots)
        for (i in 0 until spawnCount) {
            val angle = Random.nextFloat() * GameMath.TWO_PI
            val speed = Random.nextFloat() * 90f + 35f
            neonSparks.add(
                NeonSpark(
                    position = pos,
                    velocity = Offset(cos(angle) * speed, sin(angle) * speed),
                    radius = Random.nextFloat() * 3.5f + 2.2f,
                    color = color,
                    glowColor = glowColor,
                    maxLifetime = Random.nextFloat() * 0.45f + 0.35f
                )
            )
        }
    }

    private fun emitKillBurst(victim: Serpent) {
        for (i in 0 until victim.segments.size step 2) {
            val seg = victim.segments[i]
            emitPreySparks(seg, victim.skin.primaryColor, victim.skin.glowColor, count = 5)
        }
    }

    private fun updateBotAi(bot: Serpent, deltaTime: Float) {
        bot.aiStateTimer += deltaTime

        // 1. Check for immediate obstacles / danger ahead (collision avoidance)
        val lookAheadDist = 70f + (if (bot.isBoosting) 40f else 0f)
        val lookAheadPoint = GameMath.movePoint(bot.head, bot.angle, lookAheadDist)

        var obstacleDetected = false
        var steerAwayAngle = bot.angle

        // Check arena boundary
        if (GameMath.distance(lookAheadPoint, worldCenter) > worldRadius - 60f) {
            obstacleDetected = true
            steerAwayAngle = GameMath.angleTo(bot.head, worldCenter)
        } else {
            // Check nearby serpent bodies
            val allSerpents = mutableListOf<Serpent>()
            if (player.isAlive) allSerpents.add(player)
            for (b in bots) if (b.isAlive && b.id != bot.id) allSerpents.add(b)

            for (other in allSerpents) {
                for (seg in other.segments) {
                    val safeDist = bot.bodyRadius + other.bodyRadius + 30f
                    if (GameMath.distanceSquared(lookAheadPoint, seg) < safeDist * safeDist) {
                        obstacleDetected = true
                        // Steer perpendicular or opposite
                        val angleToSeg = GameMath.angleTo(bot.head, seg)
                        steerAwayAngle = GameMath.normalizeAngle(angleToSeg + kotlin.math.PI.toFloat())
                        break
                    }
                }
                if (obstacleDetected) break
            }
        }

        if (obstacleDetected) {
            bot.targetAngle = steerAwayAngle
            bot.isBoosting = false
            return
        }

        // 2. Goal Seeking behavior every 0.3s
        if (bot.aiStateTimer > 0.35f) {
            bot.aiStateTimer = 0f

            when (bot.aiPersonality) {
                AiPersonality.AGGRESSIVE -> {
                    // Try to cut off player or other snakes if nearby
                    val target = if (player.isAlive && GameMath.distance(bot.head, player.head) < 400f) {
                        player
                    } else {
                        bots.filter { it.isAlive && it.id != bot.id }
                            .minByOrNull { GameMath.distance(bot.head, it.head) }
                    }

                    if (target != null && GameMath.distance(bot.head, target.head) < 350f) {
                        // Aim slightly ahead of target's movement
                        val intercept = GameMath.movePoint(target.head, target.angle, 60f)
                        bot.targetAngle = GameMath.angleTo(bot.head, intercept)
                        bot.isBoosting = bot.mass > 35f && Random.nextFloat() < 0.6f
                    } else {
                        steerTowardsNearestFood(bot)
                    }
                }

                AiPersonality.SCAVENGER -> {
                    // Prioritize death orbs
                    val bigFood = foods.filter { it.isDeathOrb }
                        .minByOrNull { GameMath.distance(bot.head, it.position) }
                    if (bigFood != null && GameMath.distance(bot.head, bigFood.position) < 500f) {
                        bot.targetAngle = GameMath.angleTo(bot.head, bigFood.position)
                        bot.isBoosting = bot.mass > 40f
                    } else {
                        steerTowardsNearestFood(bot)
                    }
                }

                AiPersonality.BALANCED, AiPersonality.TIMID -> {
                    steerTowardsNearestFood(bot)
                    bot.isBoosting = false
                }
            }
        }
    }

    private fun steerTowardsNearestFood(bot: Serpent) {
        val nearbyFood = foods.minByOrNull { GameMath.distanceSquared(bot.head, it.position) }
        if (nearbyFood != null) {
            bot.targetAngle = GameMath.angleTo(bot.head, nearbyFood.position)
        } else {
            // Wander
            bot.targetAngle += (Random.nextFloat() - 0.5f) * 0.8f
        }
    }

    fun getLeaderboard(): List<LeaderboardEntry> {
        val list = mutableListOf<LeaderboardEntry>()
        if (player.isAlive) {
            list.add(LeaderboardEntry(player.name, player.score, player.mass.toInt(), true, player.skin.primaryColor))
        }
        for (b in bots) {
            if (b.isAlive) {
                list.add(LeaderboardEntry(b.name, b.score, b.mass.toInt(), false, b.skin.primaryColor))
            }
        }
        return list.sortedByDescending { it.score }.take(5)
    }
}

data class LeaderboardEntry(
    val name: String,
    val score: Int,
    val mass: Int,
    val isPlayer: Boolean,
    val color: Color
)
