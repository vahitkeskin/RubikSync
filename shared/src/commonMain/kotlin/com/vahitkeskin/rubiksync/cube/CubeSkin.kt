package com.vahitkeskin.rubiksync.cube

import androidx.compose.ui.graphics.Color

enum class CubeSkin(
    val id: String,
    val emoji: String
) {
    CLASSIC("classic", "🎨"),
    SHINY_GOLD("shiny_gold", "✨"),
    SHINY_SILVER("shiny_silver", "💿"),
    NEON("neon", "🌈"),
    PASTEL("pastel", "🌸");

    fun getStickerColor(cubeColor: CubeColor): Color {
        return when (this) {
            CLASSIC -> Color(cubeColor.rgb)
            SHINY_GOLD -> when (cubeColor) {
                CubeColor.ORANGE -> Color(0xFFFFA834) // Gold Orange
                CubeColor.RED -> Color(0xFFD13F24)    // Copper Crimson
                CubeColor.YELLOW -> Color(0xFFFFD700) // Pure Gold Yellow
                CubeColor.WHITE -> Color(0xFFF5E6C8)  // Champagne White Gold
                CubeColor.GREEN -> Color(0xFF8FA33B)  // Brass Green Gold
                CubeColor.BLUE -> Color(0xFF3C72A8)   // Slate Blue Gold
                CubeColor.INTERNAL -> Color(0xFF1F1A0F)
            }
            SHINY_SILVER -> when (cubeColor) {
                CubeColor.ORANGE -> Color(0xFFE08B5C) // Silver Orange
                CubeColor.RED -> Color(0xFFB85A5A)    // Silver Red
                CubeColor.YELLOW -> Color(0xFFEAD875) // Platinum Lemon Yellow
                CubeColor.WHITE -> Color(0xFFE5E5E8)  // Bright Chrome White
                CubeColor.GREEN -> Color(0xFF7CA38C)  // Sage Green Silver
                CubeColor.BLUE -> Color(0xFF5A8EBA)   // Steel Blue Silver
                CubeColor.INTERNAL -> Color(0xFF1A1F26)
            }
            NEON -> when (cubeColor) {
                CubeColor.ORANGE -> Color(0xFFFF5F1F) // Neon Orange
                CubeColor.RED -> Color(0xFFFF007F)    // Neon Pink
                CubeColor.YELLOW -> Color(0xFFFFE600) // Neon Yellow
                CubeColor.WHITE -> Color(0xFF00FFFF)  // Neon Cyan
                CubeColor.GREEN -> Color(0xFF39FF14)  // Neon Green
                CubeColor.BLUE -> Color(0xFF8A2BE2)   // Neon Purple
                CubeColor.INTERNAL -> Color(0xFF05050A)
            }
            PASTEL -> when (cubeColor) {
                CubeColor.ORANGE -> Color(0xFFFFB7B2) // Pastel Peach
                CubeColor.RED -> Color(0xFFFFC6FF)    // Pastel Pink
                CubeColor.YELLOW -> Color(0xFFFDFFB6) // Pastel Yellow
                CubeColor.WHITE -> Color(0xFFF8F9FA)  // Pastel Soft White
                CubeColor.GREEN -> Color(0xFFCAFFBF)  // Mint Green
                CubeColor.BLUE -> Color(0xFF9BF6FF)   // Pastel Sky Blue
                CubeColor.INTERNAL -> Color(0xFF242830)
            }
        }
    }

    fun getBodyColor(isDarkTheme: Boolean): Color {
        return when (this) {
            CLASSIC -> {
                if (isDarkTheme) Color(0xFF141B28) else Color(0xFF1F2937)
            }
            SHINY_GOLD -> {
                if (isDarkTheme) Color(0xFF251F14) else Color(0xFF1F1A0F)
            }
            SHINY_SILVER -> {
                if (isDarkTheme) Color(0xFF1F2228) else Color(0xFF1A1F26)
            }
            NEON -> {
                if (isDarkTheme) Color(0xFF05050C) else Color(0xFF000000)
            }
            PASTEL -> {
                if (isDarkTheme) Color(0xFF2E323A) else Color(0xFF242830)
            }
        }
    }

    fun getOutlineColor(isDarkTheme: Boolean): Color {
        return when (this) {
            CLASSIC -> {
                if (isDarkTheme) Color(0xFF1E1E1E) else Color(0x33FFFFFF)
            }
            SHINY_GOLD -> {
                Color(0xFFFFD700)
            }
            SHINY_SILVER -> {
                Color(0xFFE5E4E2)
            }
            NEON -> {
                Color(0xFF8A2BE2)
            }
            PASTEL -> {
                Color(0xFFE2E8F0)
            }
        }
    }
}
