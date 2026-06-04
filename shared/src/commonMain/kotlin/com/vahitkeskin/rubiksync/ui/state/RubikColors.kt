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
    val accentOrange: Color = AccentOrange,
    val accentBlue: Color = AccentBlue,
    val accentGreen: Color = AccentGreen,
    val accentRed: Color = AccentRed,

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
    backgroundPrimary = DarkBgPrimary,
    backgroundSecondary = DarkBgTertiary,
    backgroundTertiary = DarkBgQuaternary,
    backgroundPanel = DarkPanelBg,

    bgGradient1 = DarkGradientBg1,
    bgGradient2 = DarkBgPrimary,
    bgGradient3 = DarkBgSecondary,
    bgGradient4 = DarkBgPrimary,

    textPrimary = Color.White,
    textSecondary = DarkTextPrimary,
    textMuted = DarkTextSecondary,

    borderPrimary = DarkCardBorder,
    borderSubtle = WhiteAlpha08,
    borderFaint = WhiteAlpha04,

    cardBackground = DarkBgTertiary,
    cardBorder = WhiteAlpha05,

    tabActive = DarkBgQuaternary,
    tabInactive = Color.Transparent,
    tabActiveBorder = WhiteAlpha08,

    buttonDisabledBg = DarkBgPrimary,
    buttonDisabledText = DarkTextSecondary,
    buttonBorder = DarkCardBorder,

    speedTrack = DarkBgQuaternary,
    speedLabel = DarkTextDisabled,
    progressTrack = DarkBgQuaternary,

    glowOrange = AccentOrangeAlpha05,
    glowBlue = AccentBlueAlpha04,

    isDark = true
)

val LightRubikColors = RubikColors(
    backgroundPrimary = LightBgPrimary,
    backgroundSecondary = White,
    backgroundTertiary = LightBgTertiary,
    backgroundPanel = LightBgSecondary,

    bgGradient1 = LightBgSecondary,
    bgGradient2 = LightBgPrimary,
    bgGradient3 = LightBgTertiary,
    bgGradient4 = LightBgPrimary,

    textPrimary = DarkThemePrimary,
    textSecondary = LightTextPrimary,
    textMuted = LightTextSecondary,

    borderPrimary = LightBorderPrimary,
    borderSubtle = LightBorderSubtle,
    borderFaint = LightBorderPrimary,

    cardBackground = White,
    cardBorder = LightBorderPrimary,

    tabActive = White,
    tabInactive = Color.Transparent,
    tabActiveBorder = LightBorderSubtle,

    buttonDisabledBg = LightPanelBg,
    buttonDisabledText = LightTextSecondary,
    buttonBorder = LightBorderSubtle,

    speedTrack = LightBorderPrimary,
    speedLabel = LightTextPrimary,
    progressTrack = LightBorderPrimary,

    glowOrange = AccentOrangeAlpha03,
    glowBlue = AccentBlueAlpha02,

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
