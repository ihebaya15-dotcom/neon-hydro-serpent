package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GameRecord
import com.example.firebase.CloudLeaderboardEntry
import com.example.game.SkinTheme
import com.example.ui.theme.AbyssDark
import com.example.ui.theme.AbyssNavy
import com.example.ui.theme.AbyssSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.auth.FirebaseUser

@Composable
fun MenuScreen(
    selectedSkin: SkinTheme,
    highestScore: Int?,
    topScores: List<GameRecord>,
    currentUser: FirebaseUser?,
    globalLeaderboard: List<CloudLeaderboardEntry>,
    authError: String?,
    isSyncing: Boolean,
    onSelectSkin: (SkinTheme) -> Unit,
    onStartGame: () -> Unit,
    onSignInWithGoogle: (activityContext: android.content.Context) -> Unit,
    onSignOut: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AbyssDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // User Cloud Status Bar
            CloudStatusBar(
                currentUser = currentUser,
                isSyncing = isSyncing,
                onSignIn = { onSignInWithGoogle(context) },
                onSignOut = onSignOut
            )

            if (authError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onClearError() },
                    colors = CardDefaults.cardColors(containerColor = NeonMagenta.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = authError,
                        color = Color(0xFFFF80AB),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Waves,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NEON SERPENT",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = NeonCyan
                )
            }

            Text(
                text = "ثعابين النيون المائية في الأعماق",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Best Score Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AbyssNavy.copy(alpha = 0.8f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "أعلى رقم قياسي",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "${highestScore ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGold
                    )
                }
            }

            // Big Start Game Button
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .scale(pulseScale)
                    .testTag("play_game_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AbyssDark,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.play_now),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AbyssDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Skin Selector Section
            Text(
                text = stringResource(R.string.select_skin),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(SkinTheme.ALL_SKINS) { skin ->
                    val isSelected = skin.id == selectedSkin.id
                    val borderBrush = if (isSelected) {
                        Brush.linearGradient(listOf(skin.glowColor, skin.primaryColor))
                    } else {
                        Brush.linearGradient(listOf(AbyssNavy, AbyssNavy))
                    }

                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                brush = borderBrush,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectSkin(skin) }
                            .testTag("skin_selector_${skin.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AbyssSurface else AbyssNavy.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(skin.primaryColor, skin.glowColor)
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = skin.glowColor.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = skin.nameAr,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) skin.glowColor else TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // How To Play Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AbyssNavy.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "كيف تلتحم في الأعماق؟",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "عصا التحكم الافتراضية: توجيه سباحة الثعبان بنعومة ثلاثية الأبعاد.",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "زر الاندفاع السريع: تسارع نفاث مع استهلاك تدريجي لكتلة الثعبان.",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "قاعدة الافتراس: اقطع طريق رؤوس الخصوم بجسدك لتنفجر إلى طاقة وتلتهمها!",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "عوالق البايونيك الخارقة: تمنحك درعاً 🛡️ يصد الاصطدامات القاتلة أو سرعة تيربو خارقة مجانية ⚡!",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dual Tab Leaderboard: Global Cloud vs Local Records
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AbyssNavy.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = NeonCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = NeonCyan
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (selectedTab == 0) NeonCyan else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "المتصدرون عالمياً (سحابي)",
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "أفضل معاركي محلياً",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedTab == 0) {
                        // Global Firestore Leaderboard
                        if (currentUser == null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "سجل دخولك بحساب Google لمزامنة إنجازاتك والمنافسة في لائحة الصدارة العالمية!",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onSignInWithGoogle(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = AbyssDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تسجيل الدخول مع Google", color = AbyssDark, fontSize = 12.sp)
                                }
                            }
                        } else if (globalLeaderboard.isEmpty()) {
                            Text(
                                text = "لا توجد أرقام مسجلة بعد في لائحة الصدارة العالمية. العب الآن لتكن الأول!",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )
                        } else {
                            globalLeaderboard.take(10).forEachIndexed { idx, entry ->
                                val isMe = entry.userId == currentUser.uid
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isMe) NeonCyan.copy(alpha = 0.15f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (idx) {
                                                0 -> "🥇 #1"
                                                1 -> "🥈 #2"
                                                2 -> "🥉 #3"
                                                else -> "#${idx + 1}"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (idx) {
                                                0 -> NeonGold
                                                1 -> Color(0xFFC0C0C0)
                                                2 -> Color(0xFFCD7F32)
                                                else -> TextSecondary
                                            },
                                            modifier = Modifier.width(38.dp)
                                        )
                                        Text(
                                            text = entry.displayName + if (isMe) " (أنت)" else "",
                                            fontSize = 12.sp,
                                            fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isMe) NeonCyan else TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "${entry.score} نقطة  (${entry.kills} افتراس)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isMe) NeonGold else TextPrimary
                                    )
                                }
                            }
                        }
                    } else {
                        // Local Room History
                        if (topScores.isEmpty()) {
                            Text(
                                text = "لم تخض أي معركة بعد. انطلق الآن!",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            topScores.take(5).forEachIndexed { idx, record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${idx + 1}  ${record.score} نقطة  (${record.kills} افتراس)",
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${record.survivalSeconds}ث",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CloudStatusBar(
    currentUser: FirebaseUser?,
    isSyncing: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentUser != null) AbyssSurface else AbyssNavy.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (currentUser != null) NeonCyan.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = NeonCyan
                    )
                } else if (currentUser != null) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = if (currentUser != null) {
                            currentUser.displayName?.takeIf { it.isNotBlank() } ?: "أفعى النيون السحابية"
                        } else {
                            "المزامنة السحابية غير مفعلة"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentUser != null) TextPrimary else TextSecondary
                    )
                    Text(
                        text = if (currentUser != null) "حسابك متصل بقاعدة بيانات Firebase" else "سجل دخولك لحفظ درجاتك عالمياً",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            if (currentUser != null) {
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "تسجيل الخروج",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onSignIn,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("دخول Google", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
