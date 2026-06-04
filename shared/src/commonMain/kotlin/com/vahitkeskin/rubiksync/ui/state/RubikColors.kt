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
    val accentOrange: Color = Color_FFFF8A00,
    val accentBlue: Color = Color_FF448AFF,
    val accentGreen: Color = Color_FF30D158,
    val accentRed: Color = Color_FFFF453A,

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
    backgroundPrimary = Color_FF0A0E18,
    backgroundSecondary = Color_FF141B28,
    backgroundTertiary = Color_FF1C2536,
    backgroundPanel = Color_FF111827,

    bgGradient1 = Color_FF0F1724,
    bgGradient2 = Color_FF0A0E18,
    bgGradient3 = Color_FF0D1220,
    bgGradient4 = Color_FF0A0E18,

    textPrimary = Color.White,
    textSecondary = Color_FF8A99AD,
    textMuted = Color_FF4A5568,

    borderPrimary = Color_FF2A3548,
    borderSubtle = Color_15FFFFFF,
    borderFaint = Color_0AFFFFFF,

    cardBackground = Color_FF141B28,
    cardBorder = Color_0CFFFFFF,

    tabActive = Color_FF1C2536,
    tabInactive = Color.Transparent,
    tabActiveBorder = Color_15FFFFFF,

    buttonDisabledBg = Color_FF0A0E18,
    buttonDisabledText = Color_FF4A5568,
    buttonBorder = Color_FF2A3548,

    speedTrack = Color_FF1C2536,
    speedLabel = Color_FF6B7A8D,
    progressTrack = Color_FF1C2536,

    glowOrange = Color_0CFF8A00,
    glowBlue = Color_06448AFF,

    isDark = true
)

val LightRubikColors = RubikColors(
    backgroundPrimary = Color_FFF5F7FA,
    backgroundSecondary = Color_FFFFFFFF,
    backgroundTertiary = Color_FFF0F2F5,
    backgroundPanel = Color_FFF8F9FC,

    bgGradient1 = Color_FFF8F9FC,
    bgGradient2 = Color_FFF5F7FA,
    bgGradient3 = Color_FFF0F2F5,
    bgGradient4 = Color_FFF5F7FA,

    textPrimary = Color_FF1A1A2E,
    textSecondary = Color_FF6B7280,
    textMuted = Color_FF9CA3AF,

    borderPrimary = Color_FFE5E7EB,
    borderSubtle = Color_FFD1D5DB,
    borderFaint = Color_FFE5E7EB,

    cardBackground = Color_FFFFFFFF,
    cardBorder = Color_FFE5E7EB,

    tabActive = Color_FFFFFFFF,
    tabInactive = Color.Transparent,
    tabActiveBorder = Color_FFD1D5DB,

    buttonDisabledBg = Color_FFF3F4F6,
    buttonDisabledText = Color_FF9CA3AF,
    buttonBorder = Color_FFD1D5DB,

    speedTrack = Color_FFE5E7EB,
    speedLabel = Color_FF6B7280,
    progressTrack = Color_FFE5E7EB,

    glowOrange = Color_08FF8A00,
    glowBlue = Color_04448AFF,

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
