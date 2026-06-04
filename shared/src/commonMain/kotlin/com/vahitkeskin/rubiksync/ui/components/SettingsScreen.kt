package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.vahitkeskin.rubiksync.ui.icons.ArrowBackIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.ThemeMode
import com.vahitkeskin.rubiksync.getCurrentYear
import com.vahitkeskin.rubiksync.BindBackHandler
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.vahitkeskin.rubiksync.ui.strings.AppLanguage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun SettingsScreen(
    appState: RubikAppState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    BindBackHandler(enabled = true) {
        appState.showSettingsScreen = false
    }

    // Tema bazlı renkler
    val bgPrimary = RubikTheme.colors.backgroundPrimary
    val bgSecondary = RubikTheme.colors.backgroundSecondary
    val bgTertiary = RubikTheme.colors.backgroundTertiary
    val textPrimary = RubikTheme.colors.textPrimary
    val textSecondary = RubikTheme.colors.textSecondary
    val borderColor = RubikTheme.colors.borderPrimary
    val cardBorder = RubikTheme.colors.cardBorder

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RubikTheme.colors.bgGradient1,
                        RubikTheme.colors.bgGradient2,
                        RubikTheme.colors.bgGradient3,
                        RubikTheme.colors.bgGradient4
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geri butonu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgSecondary)
                        .border(0.5.dp, cardBorder, RoundedCornerShape(12.dp))
                        .clickable { appState.showSettingsScreen = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ArrowBackIcon,
                        contentDescription = "Geri",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = appState.strings.settingsTitle,
                        color = textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = appState.strings.settingsSubtitle,
                        color = textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Tema Modu Bölümü
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgSecondary)
                    .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Bölüm başlığı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🎨", fontSize = 16.sp)
                    Column {
                        Text(
                            text = appState.strings.themeMode,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appState.strings.themeSubtitle,
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tema seçenekleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionCard(
                        emoji = "☀️",
                        label = appState.strings.themeLight,
                        description = appState.strings.themeOptionLightDesc,
                        isSelected = appState.themeMode == ThemeMode.LIGHT,
                        onClick = { appState.updateThemeMode(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        emoji = "🌙",
                        label = appState.strings.themeDark,
                        description = appState.strings.themeOptionDarkDesc,
                        isSelected = appState.themeMode == ThemeMode.DARK,
                        onClick = { appState.updateThemeMode(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        emoji = "📱",
                        label = appState.strings.themeSystem,
                        description = appState.strings.themeOptionSystemDesc,
                        isSelected = appState.themeMode == ThemeMode.SYSTEM,
                        onClick = { appState.updateThemeMode(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dil Seçimi Bölümü
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgSecondary)
                    .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Bölüm başlığı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🌐", fontSize = 16.sp)
                    Column {
                        Text(
                            text = appState.strings.languageTitle,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appState.strings.languageSubtitle,
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modern Seçici Buton
                var isExpanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgTertiary)
                        .border(0.5.dp, cardBorder, RoundedCornerShape(12.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = appState.appLanguage.flag, fontSize = 16.sp)
                            Text(
                                text = "${appState.appLanguage.displayName} (${appState.appLanguage.code.uppercase()})",
                                color = textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            color = textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .background(bgTertiary)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, cardBorder, RoundedCornerShape(12.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AppLanguage.values().toList()) { lang ->
                                val isSelected = appState.appLanguage == lang
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) (if (isDarkTheme) Color(0xFF1A1510) else Color(0xFFFFF7ED)) else Color.Transparent)
                                        .clickable {
                                            appState.updateLanguage(lang)
                                            isExpanded = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = lang.flag, fontSize = 16.sp)
                                        Text(
                                            text = lang.displayName,
                                            color = if (isSelected) Color(0xFFFF8A00) else textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "✓",
                                            color = Color(0xFFFF8A00),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (lang != AppLanguage.values().last()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(0.5.dp)
                                            .background(borderColor.copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Uygulama bilgisi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgSecondary)
                    .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "ℹ️", fontSize = 16.sp)
                    Column {
                        Text(
                            text = appState.strings.aboutTitle,
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appState.strings.aboutSubtitle,
                            color = textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appState.strings.versionTitle,
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgTertiary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "1.0.0",
                            color = textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appState.strings.platformTitle,
                        color = textSecondary,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgTertiary)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Kotlin Multiplatform",
                            color = textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Alt bilgi
            Text(
                text = "${appState.strings.copyright}${getCurrentYear()}",
                color = textSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    emoji: String,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgUnselected = RubikTheme.colors.backgroundTertiary
    val textPrimary = RubikTheme.colors.textPrimary
    val textSecondary = RubikTheme.colors.textSecondary

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (RubikTheme.colors.isDark) Color(0xFF1A1510) else Color(0xFFFFF7ED)
        } else bgUnselected,
        animationSpec = tween(300)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(listOf(Color(0xFFFF8A00), Color(0xFFFF5252)))
                } else {
                    Brush.linearGradient(
                        listOf(
                            if (RubikTheme.colors.isDark) Color(0x0AFFFFFF) else Color(0x1A000000),
                            if (RubikTheme.colors.isDark) Color(0x0AFFFFFF) else Color(0x1A000000)
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp
        )
        Text(
            text = label,
            color = if (isSelected) Color(0xFFFF8A00) else textPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = description,
            color = textSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )

        // Seçili göstergesi
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF8A00), Color(0xFFFF5252))
                        )
                    )
            )
        }
    }
}
