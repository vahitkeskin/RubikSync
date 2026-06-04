package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Proje genelinde kullanılan tema renkleri.
 * Tüm UI bileşenleri bu renkleri kullanarak light/dark uyumlu hale gelir.
 */
@Immutable
data class RubikColors(
    // Ana arka plan renkleri
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val backgroundPanel: Color,

    // Arka plan gradient
    val bgGradient1: Color,
    val bgGradient2: Color,
    val bgGradient3: Color,
    val bgGradient4: Color,

    // Metin renkleri
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,

    // Kenarlık renkleri
    val borderPrimary: Color,
    val borderSubtle: Color,
    val borderFaint: Color,

    // Kart & yüzey
    val cardBackground: Color,
    val cardBorder: Color,

    // Tab / seçim renkleri
    val tabActive: Color,
    val tabInactive: Color,
    val tabActiveBorder: Color,

    // Buton renkleri
    val buttonDisabledBg: Color,
    val buttonDisabledText: Color,
    val buttonBorder: Color,

    // Vurgu (accent) renkleri — tüm temalarda aynı
    val accentOrange: Color = Color(0xFFFF8A00),
    val accentBlue: Color = Color(0xFF448AFF),
    val accentGreen: Color = Color(0xFF30D158),
    val accentRed: Color = Color(0xFFFF453A),

    // Özel bileşen renkleri
    val speedTrack: Color,
    val speedLabel: Color,
    val progressTrack: Color,

    // Radial glow alpha
    val glowOrange: Color,
    val glowBlue: Color,

    // Tema modunun karanlık olup olmadığı
    val isDark: Boolean
)

val DarkRubikColors = RubikColors(
    backgroundPrimary = Color(0xFF0A0E18),
    backgroundSecondary = Color(0xFF141B28),
    backgroundTertiary = Color(0xFF1C2536),
    backgroundPanel = Color(0xFF111827),

    bgGradient1 = Color(0xFF0F1724),
    bgGradient2 = Color(0xFF0A0E18),
    bgGradient3 = Color(0xFF0D1220),
    bgGradient4 = Color(0xFF0A0E18),

    textPrimary = Color.White,
    textSecondary = Color(0xFF8A99AD),
    textMuted = Color(0xFF4A5568),

    borderPrimary = Color(0xFF2A3548),
    borderSubtle = Color(0x15FFFFFF),
    borderFaint = Color(0x0AFFFFFF),

    cardBackground = Color(0xFF141B28),
    cardBorder = Color(0x0CFFFFFF),

    tabActive = Color(0xFF1C2536),
    tabInactive = Color.Transparent,
    tabActiveBorder = Color(0x15FFFFFF),

    buttonDisabledBg = Color(0xFF0A0E18),
    buttonDisabledText = Color(0xFF4A5568),
    buttonBorder = Color(0xFF2A3548),

    speedTrack = Color(0xFF1C2536),
    speedLabel = Color(0xFF6B7A8D),
    progressTrack = Color(0xFF1C2536),

    glowOrange = Color(0x0CFF8A00),
    glowBlue = Color(0x06448AFF),

    isDark = true
)

val LightRubikColors = RubikColors(
    backgroundPrimary = Color(0xFFF5F7FA),
    backgroundSecondary = Color(0xFFFFFFFF),
    backgroundTertiary = Color(0xFFF0F2F5),
    backgroundPanel = Color(0xFFF8F9FC),

    bgGradient1 = Color(0xFFF8F9FC),
    bgGradient2 = Color(0xFFF5F7FA),
    bgGradient3 = Color(0xFFF0F2F5),
    bgGradient4 = Color(0xFFF5F7FA),

    textPrimary = Color(0xFF1A1A2E),
    textSecondary = Color(0xFF6B7280),
    textMuted = Color(0xFF9CA3AF),

    borderPrimary = Color(0xFFE5E7EB),
    borderSubtle = Color(0xFFD1D5DB),
    borderFaint = Color(0xFFE5E7EB),

    cardBackground = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE5E7EB),

    tabActive = Color(0xFFFFFFFF),
    tabInactive = Color.Transparent,
    tabActiveBorder = Color(0xFFD1D5DB),

    buttonDisabledBg = Color(0xFFF3F4F6),
    buttonDisabledText = Color(0xFF9CA3AF),
    buttonBorder = Color(0xFFD1D5DB),

    speedTrack = Color(0xFFE5E7EB),
    speedLabel = Color(0xFF6B7280),
    progressTrack = Color(0xFFE5E7EB),

    glowOrange = Color(0x08FF8A00),
    glowBlue = Color(0x04448AFF),

    isDark = false
)

val LocalRubikColors = staticCompositionLocalOf { DarkRubikColors }

/**
 * RubikTheme CompositionLocal sağlayıcısı.
 * Bu sarmalayıcı ile tüm alt bileşenler LocalRubikColors'a erişebilir.
 */
@Composable
fun ProvideRubikColors(
    colors: RubikColors,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalRubikColors provides colors) {
        content()
    }
}

/**
 * Herhangi bir composable içinden kısa yoldan tema renklerine erişmek için.
 * Kullanım: val colors = RubikTheme.colors
 */
object RubikTheme {
    val colors: RubikColors
        @Composable
        get() = LocalRubikColors.current
}
