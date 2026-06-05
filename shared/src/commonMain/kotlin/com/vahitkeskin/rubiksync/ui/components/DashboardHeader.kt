package com.vahitkeskin.rubiksync.ui.components

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
fun DashboardHeader(
    cubeState: RubikCubeState,
    appState: RubikAppState,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Row — Brand & Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title Area
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stylized Mini 3D-looking Rubik Cube Badge
                Column(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RubikTheme.colors.cardBackground)
                        .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(6.dp))
                        .padding(3.dp),
                    verticalArrangement = Arrangement.spacedBy(1.5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(1.5.dp)).background(CubeOrange))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(1.5.dp)).background(CubeGreen))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(1.5.dp)).background(CubeBlue))
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(1.5.dp)).background(CubeYellow))
                    }
                }

                Column {
                    Text(
                        text = appState.strings.appTitle,
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = appState.strings.appSubtitle,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        maxLines = 1
                    )
                }
            }

            // Quick Control Actions Cluster (Glassmorphic Buttons)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Küp düzenleme ikonu — kilit/açık durumuna göre
                val editableBgColor by animateColorAsState(
                    targetValue = if (appState.isCubeEditable) {
                        RubikTheme.colors.accentGreen.copy(alpha = 0.12f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.12f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "editableBg"
                )
                val editableBorderColor by animateColorAsState(
                    targetValue = if (appState.isCubeEditable) {
                        RubikTheme.colors.accentGreen.copy(alpha = 0.3f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.3f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "editableBorder"
                )
                 Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(editableBgColor)
                        .border(0.8.dp, editableBorderColor, RoundedCornerShape(10.dp))
                        .onGloballyPositioned { coords ->
                            if (appState.showcaseStep == 1 && !appState.isShowcaseCompleted) {
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                appState.targetBounds = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                appState.targetCornerRadius = 10.dp
                            }
                        }
                        .clickable(
                            enabled = !cubeState.isAnimating
                        ) {
                            appState.updateCubeEditable(!appState.isCubeEditable)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appState.isCubeEditable) "🔓" else "🔒",
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                    AuraBalloon(
                        text = appState.strings.showcaseEditableText,
                        isVisible = appState.showcaseStep == 1 && !appState.isShowcaseCompleted,
                        isBelow = true,
                        onDismiss = {
                            appState.showcaseStep = 2
                        }
                    )
                }

                // Zeka küpü döndürme ses ikonu — hoparlör/sessiz
                val soundBgColor by animateColorAsState(
                    targetValue = if (!appState.isCubeEditable) {
                        RubikTheme.colors.cardBackground.copy(alpha = 0.4f)
                    } else if (appState.isSoundEnabled) {
                        RubikTheme.colors.accentBlue.copy(alpha = 0.12f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.12f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "soundBg"
                )
                val soundBorderColor by animateColorAsState(
                    targetValue = if (!appState.isCubeEditable) {
                        RubikTheme.colors.cardBorder.copy(alpha = 0.2f)
                    } else if (appState.isSoundEnabled) {
                        RubikTheme.colors.accentBlue.copy(alpha = 0.3f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.3f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "soundBorder"
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(soundBgColor)
                        .border(0.8.dp, soundBorderColor, RoundedCornerShape(10.dp))
                        .onGloballyPositioned { coords ->
                            if (appState.showcaseStep == 2 && !appState.isShowcaseCompleted) {
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                appState.targetBounds = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                appState.targetCornerRadius = 10.dp
                            }
                        }
                        .clickable(
                            enabled = appState.isCubeEditable && !cubeState.isAnimating
                        ) {
                            appState.updateSoundEnabled(!appState.isSoundEnabled)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appState.isSoundEnabled) "🔊" else "🔇",
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.alpha(if (appState.isCubeEditable) 1f else 0.35f)
                    )
                    AuraBalloon(
                        text = appState.strings.showcaseSoundText,
                        isVisible = appState.showcaseStep == 2 && !appState.isShowcaseCompleted,
                        isBelow = true,
                        onDismiss = {
                            appState.showcaseStep = 3
                        }
                    )
                }

                // Ayarlar butonu
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(RubikTheme.colors.cardBackground)
                        .border(0.8.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                        .onGloballyPositioned { coords ->
                            if (appState.showcaseStep == 3 && !appState.isShowcaseCompleted) {
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                appState.targetBounds = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                appState.targetCornerRadius = 10.dp
                            }
                        }
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    AuraBalloon(
                        text = appState.strings.showcaseSettingsText,
                        isVisible = appState.showcaseStep == 3 && !appState.isShowcaseCompleted,
                        isBelow = true,
                        onDismiss = {
                            appState.showcaseStep = 4
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status & Stats — Professional Dashboard Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Move Counter Card
            StatChip(
                emoji = "🎯",
                value = "${cubeState.moveHistory.size}",
                label = appState.strings.movesLabel,
                accentColor = RubikTheme.colors.accentBlue,
                modifier = Modifier.weight(1f)
            )

            // Solved Status Card
            val solved = appState.isSolved
            StatChip(
                emoji = if (solved) "✅" else "🔄",
                value = if (solved) appState.strings.solvedStatus else appState.strings.scrambledStatus,
                label = appState.strings.statusLabel,
                accentColor = if (solved) RubikTheme.colors.accentGreen else RubikTheme.colors.accentOrange,
                modifier = Modifier.weight(1f)
            )
        }

        // Move History — scrollable chips with refined styling
        if (cubeState.moveHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RubikTheme.colors.cardBackground)
                    .border(0.5.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 10.dp)
                    .horizontalScroll(rememberScrollState(initial = 10000)),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝",
                    fontSize = 10.sp,
                    maxLines = 1
                )

                cubeState.moveHistory.takeLast(14).forEachIndexed { idx, move ->
                    val isLast = idx == cubeState.moveHistory.takeLast(14).lastIndex

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isLast) {
                                    Brush.horizontalGradient(listOf(AccentOrange, AccentRedCoral))
                                } else {
                                    Brush.horizontalGradient(listOf(RubikTheme.colors.backgroundTertiary, RubikTheme.colors.backgroundTertiary))
                                }
                            )
                            .then(
                                if (!isLast) Modifier.border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = move.label,
                            color = if (isLast) Color.White else RubikTheme.colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isLast) FontWeight.ExtraBold else FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun StatChip(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RubikTheme.colors.cardBackground)
            .border(0.8.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(12.dp))
            .padding(start = 0.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Vertical indicator stripe on the left side of the dashboard card
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                .background(accentColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = emoji,
            fontSize = 12.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                lineHeight = 13.sp
            )
            Text(
                text = label.uppercase(),
                color = RubikTheme.colors.textSecondary,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                lineHeight = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}
