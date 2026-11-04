package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.components.ActivePowerUpBadge
import com.example.ui.components.GameCanvas
import com.example.ui.components.LeaderboardCard
import com.example.ui.components.MiniMap
import com.example.ui.components.StatsBar
import com.example.ui.components.TurboBoostButton
import com.example.ui.components.VirtualJoystick
import com.example.ui.theme.AbyssDark
import com.example.ui.theme.AbyssNavy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameState
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val lastMatchStats by viewModel.lastMatchStats.collectAsState()
    val engine = viewModel.gameEngine

    BackHandler {
        if (gameState == GameState.PLAYING) {
            viewModel.pauseGame()
        } else {
            onBackToMenu()
        }
    }

    // 60FPS Game Loop powered by withFrameNanos
    var lastFrameTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            lastFrameTimeNanos = 0L
            while (isActive) {
                withFrameNanos { currentFrameTime ->
                    if (lastFrameTimeNanos != 0L) {
                        val deltaTime = ((currentFrameTime - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        viewModel.updateGameLoop(deltaTime)
                    }
                    lastFrameTimeNanos = currentFrameTime
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Full-screen Game Canvas Rendering
        GameCanvas(
            engine = engine,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top HUD Bar (Stats, Radar, Leaderboard, Pause)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: Player Live Stats + Active Power-up timer
                Column {
                    StatsBar(
                        score = engine.player.score,
                        length = engine.player.segments.size,
                        kills = engine.player.kills
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ActivePowerUpBadge(
                        shieldDuration = engine.player.shieldDuration,
                        speedBoostDuration = engine.player.speedBoostDuration
                    )
                }

                // Right: Pause Button & Radar MiniMap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.pauseGame() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AbyssDark.copy(alpha = 0.7f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = NeonCyan
                        )
                    }

                    MiniMap(
                        player = engine.player,
                        bots = engine.bots,
                        bionicPlanktons = engine.bionicPlanktons,
                        worldRadius = engine.worldRadius
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Live Leaderboard Pill in Top-Right
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                if (leaderboard.isNotEmpty()) {
                    LeaderboardCard(entries = leaderboard)
                }
            }
        }

        // 3. Bottom Controls HUD (Virtual Joystick + Turbo Boost)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Bottom-Left: Virtual Joystick
                VirtualJoystick(
                    baseColor = engine.player.skin.primaryColor,
                    onMove = { x, y ->
                        viewModel.onJoystickMoved(x, y)
                    }
                )

                // Bottom-Right: Turbo Dash Button
                TurboBoostButton(
                    color = engine.player.skin.secondaryColor,
                    currentMass = engine.player.mass,
                    onBoostChange = { boosting ->
                        viewModel.onBoostChanged(boosting)
                    }
                )
            }
        }

        // 4. Pause Dialog
        if (gameState == GameState.PAUSED) {
            Dialog(onDismissRequest = { viewModel.resumeGame() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("pause_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AbyssNavy),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "اللعبة متوقفة مؤقتاً",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.resumeGame() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = AbyssDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("متابعة السباحة", fontWeight = FontWeight.Bold, color = AbyssDark)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = NeonCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة البدء", color = NeonCyan)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.returnToMenu()
                                onBackToMenu()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الخروج للقائمة", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // 5. Game Over Dialog
        if (gameState == GameState.GAME_OVER) {
            Dialog(onDismissRequest = {}) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("game_over_dialog"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AbyssNavy),
                    border = androidx.compose.foundation.BorderStroke(2.dp, NeonMagenta)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.game_over),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonMagenta,
                            textAlign = TextAlign.Center
                        )

                        if (lastMatchStats.isNewHighScore) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonGold.copy(alpha = 0.2f))
                                    .border(1.dp, NeonGold, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = NeonGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "رقم قياسي جديد!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Match Results Breakdown Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AbyssDark.copy(alpha = 0.7f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                ResultRow("النقاط المحققة", "${lastMatchStats.score}", NeonCyan)
                                ResultRow("أقصى طول للثعبان", "${lastMatchStats.length}", NeonGreen)
                                ResultRow("عدد الافتراس", "${lastMatchStats.kills} خصوم", NeonMagenta)
                                ResultRow("زمن البقاء", "${lastMatchStats.survivalSeconds} ثانية", TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons
                        Button(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("restart_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = AbyssDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.restart),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AbyssDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.returnToMenu()
                                onBackToMenu()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("back_to_menu_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.back_to_menu),
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
