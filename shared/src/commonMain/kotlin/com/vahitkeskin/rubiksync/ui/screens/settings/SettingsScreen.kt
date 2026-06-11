package com.vahitkeskin.rubiksync.ui.screens.settings

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.vahitkeskin.rubiksync.ui.components.RubikToolbar
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import com.vahitkeskin.rubiksync.ui.strings.AppLanguage
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.ui.screens.settings.components.ThemeOptionCard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.rubiksync.ui.navigation.Screen
import com.vahitkeskin.rubiksync.utils.RubikConstants

@Composable
fun SettingsScreen(
    appState: RubikAppState,
    isDarkTheme: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BindBackHandler(enabled = true) {
        navController.popBackStack()
    }

    val bgSecondary = RubikTheme.colors.backgroundSecondary
    val uriHandler = LocalUriHandler.current
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
            RubikToolbar(
                title = appState.strings.settingsTitle,
                subtitle = appState.strings.settingsSubtitle,
                onBackClick = { navController.popBackStack() },
                titleFontSize = 20.sp,
                subtitleFontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Tema Modu Bölümü
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

                    var isExpanded by remember { mutableStateOf(false) }
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f,
                        animationSpec = tween(300)
                    )

                    val lazyListState = rememberLazyListState()
                    val density = LocalDensity.current
                    var lastExpanded by remember { mutableStateOf(false) }

                    LaunchedEffect(isExpanded, appState.appLanguage) {
                        if (isExpanded) {
                            val selectedIndex = AppLanguage.entries.indexOf(appState.appLanguage)
                            if (selectedIndex >= 0) {
                                val itemHeightPx = with(density) { 44.dp.roundToPx() }
                                val viewportHeightPx = with(density) { 180.dp.roundToPx() }
                                val centerOffsetPx = -(viewportHeightPx / 2 - itemHeightPx / 2)

                                if (!lastExpanded) {
                                    lazyListState.scrollToItem(selectedIndex, centerOffsetPx)
                                    lastExpanded = true
                                } else {
                                    lazyListState.animateScrollToItem(selectedIndex, centerOffsetPx)
                                }
                            }
                        } else {
                            lastExpanded = false
                        }
                    }

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
                                text = "▼",
                                color = textSecondary,
                                fontSize = 10.sp,
                                modifier = Modifier.rotate(rotationAngle)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(
                            animationSpec = tween(
                                300
                            )
                        ),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(
                            animationSpec = tween(
                                300
                            )
                        )
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgTertiary)
                                    .border(0.5.dp, cardBorder, RoundedCornerShape(12.dp))
                            ) {
                                LazyColumn(
                                    state = lazyListState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    items(AppLanguage.values().toList()) { lang ->
                                        val isSelected = appState.appLanguage == lang
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (isSelected) (if (isDarkTheme) SelectionDarkOrange else AccentOrangeSoftBg) else Color.Transparent)
                                                .clickable {
                                                    appState.updateLanguage(lang)
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
                                                    color = if (isSelected) AccentOrange else textPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                            }
                                            if (isSelected) {
                                                Text(
                                                    text = "✓",
                                                    color = AccentOrange,
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Uygulama Bilgisi Bölümü
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgSecondary)
                        .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
                        .animateContentSize()
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 4.dp),
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
                                modifier = Modifier
                                    .clickable {
                                        uriHandler.openUri(RubikConstants.WEBSITE_URL)
                                    },
                                text = "vahitkeskin",
                                color = textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = appState.appLanguage == AppLanguage.TR,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = appState.strings.readmeScreenTitle,
                                color = textSecondary,
                                fontSize = 12.sp
                            )
                            androidx.compose.material3.Button(
                                onClick = { navController.navigate(Screen.Readme.route) },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = bgTertiary,
                                    contentColor = RubikTheme.colors.accentBlue
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 2.dp
                                ),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = appState.strings.showReadmeButton,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        SettingsScreen(
            appState = rememberPreviewRubikAppState(),
            isDarkTheme = true,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    PreviewRubikTheme(isDark = false) {
        SettingsScreen(
            appState = rememberPreviewRubikAppState(),
            isDarkTheme = false,
            navController = rememberNavController()
        )
    }
}
