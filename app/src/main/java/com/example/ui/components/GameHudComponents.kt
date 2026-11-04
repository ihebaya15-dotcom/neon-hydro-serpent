package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.LeaderboardEntry
import com.example.game.Serpent
import com.example.ui.theme.AbyssDark
import com.example.ui.theme.AbyssNavy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta

@Composable
fun MiniMap(
    modifier: Modifier = Modifier,
    player: Serpent,
    bots: List<Serpent>,
    worldRadius: Float
) {
    val mapSize = 78.dp

    Box(
        modifier = modifier
            .size(mapSize)
            .clip(CircleShape)
            .background(AbyssDark.copy(alpha = 0.75f))
            .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), CircleShape)
            .testTag("radar_minimap")
    ) {
        Canvas(modifier = Modifier.size(mapSize)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radarRadius = (size.width / 2f) - 3f
            val scale = radarRadius / worldRadius

            // Arena boundary ring
            drawCircle(
                color = NeonCyan.copy(alpha = 0.35f),
                radius = radarRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Center cross
            drawLine(
                color = NeonCyan.copy(alpha = 0.15f),
                start = Offset(center.x - 4f, center.y),
                end = Offset(center.x + 4f, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = NeonCyan.copy(alpha = 0.15f),
                start = Offset(center.x, center.y - 4f),
                end = Offset(center.x, center.y + 4f),
                strokeWidth = 1f
            )

            // Draw Bots blips
            val worldCenter = Offset(worldRadius, worldRadius)
            for (bot in bots) {
                if (!bot.isAlive) continue
                val rel = bot.head - worldCenter
                val blipPos = Offset(center.x + rel.x * scale, center.y + rel.y * scale)
                drawCircle(
                    color = bot.skin.primaryColor.copy(alpha = 0.75f),
                    radius = 2.2f,
                    center = blipPos
                )
            }

            // Draw Player blip (pulsing brighter cyan)
            if (player.isAlive) {
                val rel = player.head - worldCenter
                val playerBlip = Offset(center.x + rel.x * scale, center.y + rel.y * scale)
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.4f),
                    radius = 5.5f,
                    center = playerBlip
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.8f,
                    center = playerBlip
                )
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    modifier: Modifier = Modifier,
    entries: List<LeaderboardEntry>
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AbyssNavy.copy(alpha = 0.8f))
            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("leaderboard_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = NeonGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "المتصدرون",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonGold
            )
        }

        entries.forEachIndexed { index, entry ->
            val rankIcon = when (index) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> "${index + 1}."
            }
            Row(
                modifier = Modifier
                    .width(130.dp)
                    .padding(vertical = 1.5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rankIcon,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (entry.isPlayer) "أنت" else entry.name,
                        fontSize = 11.sp,
                        fontWeight = if (entry.isPlayer) FontWeight.Bold else FontWeight.Normal,
                        color = if (entry.isPlayer) NeonCyan else Color(0xFFD1D5DB),
                        maxLines = 1
                    )
                }
                Text(
                    text = "${entry.score}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = entry.color
                )
            }
        }
    }
}

@Composable
fun StatsBar(
    modifier: Modifier = Modifier,
    score: Int,
    length: Int,
    kills: Int
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AbyssNavy.copy(alpha = 0.82f))
            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("stats_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Score
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = NeonGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$score",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Length
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Straighten,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$length",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }

        // Kills
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = NeonMagenta,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$kills",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonMagenta
            )
        }
    }
}
