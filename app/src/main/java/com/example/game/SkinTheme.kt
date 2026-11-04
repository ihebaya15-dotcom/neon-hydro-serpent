package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonCoral
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple

data class SkinTheme(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val eyeColor: Color = Color.White,
    val trailColor: Color = primaryColor.copy(alpha = 0.6f)
) {
    companion object {
        val CYAN_STORM = SkinTheme(
            id = "cyan_storm",
            nameAr = "عاصفة السيان",
            nameEn = "Cyan Storm",
            primaryColor = NeonCyan,
            secondaryColor = Color(0xFF00B4D8),
            glowColor = Color(0x8800F0FF),
            eyeColor = Color(0xFFE0FFFF)
        )

        val MAGENTA_FURY = SkinTheme(
            id = "magenta_fury",
            nameAr = "الغضب الوردي",
            nameEn = "Magenta Fury",
            primaryColor = NeonMagenta,
            secondaryColor = Color(0xFFFF4081),
            glowColor = Color(0x88FF007A),
            eyeColor = Color(0xFFFFD1DC)
        )

        val EMERALD_VIPER = SkinTheme(
            id = "emerald_viper",
            nameAr = "أفعى الزمرد",
            nameEn = "Emerald Viper",
            primaryColor = NeonGreen,
            secondaryColor = Color(0xFF00E676),
            glowColor = Color(0x8800FFA3),
            eyeColor = Color(0xFFE8F5E9)
        )

        val SOLAR_EEL = SkinTheme(
            id = "solar_eel",
            nameAr = "الأنقليس الشمسي",
            nameEn = "Solar Eel",
            primaryColor = NeonGold,
            secondaryColor = Color(0xFFFF9100),
            glowColor = Color(0x88FFB800),
            eyeColor = Color(0xFFFFF8E1)
        )

        val VOID_PLASMA = SkinTheme(
            id = "void_plasma",
            nameAr = "بلازما الأعماق",
            nameEn = "Void Plasma",
            primaryColor = NeonPurple,
            secondaryColor = Color(0xFF7C4DFF),
            glowColor = Color(0x889D00FF),
            eyeColor = Color(0xFFEDE7F6)
        )

        val CORAL_STRIKE = SkinTheme(
            id = "coral_strike",
            nameAr = "شعبان المرجان",
            nameEn = "Coral Strike",
            primaryColor = NeonCoral,
            secondaryColor = Color(0xFFFF5252),
            glowColor = Color(0x88FF4D4D),
            eyeColor = Color(0xFFFFEBEE)
        )

        val ALL_SKINS = listOf(
            CYAN_STORM,
            MAGENTA_FURY,
            EMERALD_VIPER,
            SOLAR_EEL,
            VOID_PLASMA,
            CORAL_STRIKE
        )
    }
}
